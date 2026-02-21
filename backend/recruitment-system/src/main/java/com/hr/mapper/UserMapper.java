package com.hr.mapper;

import com.hr.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    
    List<User> getAllUsers();
    
    User getUserById(@Param("userId") String userId);
    
    User getUserByUsername(@Param("username") String username);
    
    int insertUser(User user);
    
    int updateUser(User user);
    
    int deleteUser(@Param("userId") String userId);
    
    List<String> getRoleIdsByUserId(@Param("userId") String userId);
}