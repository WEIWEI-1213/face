package com.sike.mapper;

import com.sike.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    int addUser(User user); //添加新用户
    User findUserByName(String username); //查找用户是否存在
}
