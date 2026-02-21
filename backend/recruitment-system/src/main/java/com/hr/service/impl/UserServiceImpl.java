package com.hr.service.impl;

import com.hr.entity.User;
import com.hr.mapper.UserMapper;
import com.hr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }
    
    @Override
    public User getUserById(String userId) {
        return userMapper.getUserById(userId);
    }
    
    @Override
    public User getUserByUsername(String username) {
        return userMapper.getUserByUsername(username);
    }
    
    @Override
    public int createUser(User user) {
        if (user.getCreateTime() == null) {
            user.setCreateTime(new Date());
        }
        if (user.getUpdateTime() == null) {
            user.setUpdateTime(new Date());
        }
        if (user.getCreateUserId() == null) {
            user.setCreateUserId("1001");
        }
        if (user.getCreateUserName() == null) {
            user.setCreateUserName("系统");
        }
        if (user.getUpdateUserId() == null) {
            user.setUpdateUserId("1001");
        }
        if (user.getUpdateUserName() == null) {
            user.setUpdateUserName("系统");
        }
        if (user.getStatus() == null) {
            user.setStatus("ACTIVE");
        }
        return userMapper.insertUser(user);
    }
    
    @Override
    public int updateUser(User user) {
        if (user.getUpdateTime() == null) {
            user.setUpdateTime(new Date());
        }
        if (user.getUpdateUserId() == null) {
            user.setUpdateUserId("1001");
        }
        if (user.getUpdateUserName() == null) {
            user.setUpdateUserName("系统");
        }
        return userMapper.updateUser(user);
    }
    
    @Override
    public int deleteUser(String userId) {
        return userMapper.deleteUser(userId);
    }
    
    @Override
    public List<String> getRoleIdsByUserId(String userId) {
        return userMapper.getRoleIdsByUserId(userId);
    }
}