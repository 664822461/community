package com.nowcoder.community.service;


import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {


    @Autowired
    UserMapper userMapper;


    public User selectById(int userId){
       return  userMapper.selectById(userId);
    }
}
