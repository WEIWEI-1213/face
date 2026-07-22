package com.sike.service;

import com.sike.entity.User;
import com.sike.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public int addUser(User user){
        return userMapper.addUser(user);
    }

    public User findUserByName(String username){
        return userMapper.findUserByName(username);
    }
}
