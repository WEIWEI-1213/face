package com.sike.controller;

import com.baidu.aip.face.AipFace;
import com.sike.entity.User;
import com.sike.service.UserService;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.HashMap;

@Controller
public class FaceController {
    private static final String APP_ID = "124015552";
    private static final String API_KEY = "nwNSoVad1BSKclsfUMbaYPw1";
    private static final String SECRET_KEY = "PvNnk87cmyAKtDoFfVIRaWoyUWHYFOky";

    @Autowired
    private UserService userService;

    @RequestMapping(value = "register",method = RequestMethod.POST)
    @ResponseBody
    public String register(String userName,String faceBase) throws IOException {
        if(!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(faceBase)) {
            String upPath = ResourceUtils.getURL("classpath:").getPath()+"static\\photo";
            String fileName = userName+"_"+System.currentTimeMillis() + ".jpg";
            String path=upPath+"\\"+fileName;
            
            AipFace client = new AipFace(APP_ID, API_KEY, SECRET_KEY);
            
            User user = new User();
            user.setUsername(userName);
            user.setPhoto(fileName);
            // ✅ 设置默认密码（因为前端没有传递密码字段）
            user.setPassword("123456"); // 默认密码
            
            User exitUser = userService.findUserByName(userName);
            if(exitUser != null) {
                return "2";
            }

            HashMap<String, String> options = new HashMap<String, String>();
            options.put("quality_control", "LOW");
            options.put("liveness_control", "LOW");
            String imageType = "BASE64";
            String groupId = "1001";
            
            JSONObject res = client.addUser(faceBase, imageType, groupId, userName, options);
            System.out.println(res.toString(2));
            
            if (res != null && !res.isNull("result")) {
                userService.addUser(user);
                
                byte[] bytes = Base64.getDecoder().decode(faceBase);
                OutputStream out = new FileOutputStream(path);
                out.write(bytes);
                out.close();
                
                return "1";
            } else {
                String errorMsg = res != null ? res.optString("error_msg", "注册失败") : "注册失败";
                System.out.println("人脸注册失败: " + errorMsg);
                return "0";
            }
        }
        return "0";
    }

    @RequestMapping(value = "login",method = RequestMethod.POST)
    @ResponseBody
    public String login(String faceBase, HttpServletRequest request) {
        String faceData = faceBase;
        AipFace client = new AipFace(APP_ID,API_KEY,SECRET_KEY);
        JSONObject user = verifyUser(faceData,client);
        
        if (user == null) {
            return "{\"num\":\"2\"}";
        }
        
        Double score = (Double) user.get("score");
        if(score != null && score>95) {
            return "{\"num\":\"1\",\"username\":\""+user.getString("user_id")+"\"}";
        }else {
            return "{\"num\":\"2\"}";
        }
    }

    public JSONObject verifyUser(String imgBash64, AipFace client) {
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("quality_control", "LOW");
        options.put("liveness_control", "LOW");
        JSONObject res = client.search(imgBash64, "BASE64", "1001", options);
        System.out.println(res.toString(2));
        
        if (res == null || !res.has("result") || res.isNull("result")) {
            System.out.println("百度API返回结果为空或没有result字段");
            return null;
        }
        
        JSONObject result = res.getJSONObject("result");
        if (result == null || !result.has("user_list") || result.isNull("user_list")) {
            System.out.println("result中没有user_list字段");
            return null;
        }
        
        JSONArray userList = result.getJSONArray("user_list");
        if (userList == null || userList.length() == 0) {
            System.out.println("user_list为空");
            return null;
        }
        
        JSONObject user = userList.getJSONObject(0);
        System.out.println("百度返回的user对象："+user.toString());
        System.out.println("username:"+user.getString("user_id"));
        return user;
    }
}