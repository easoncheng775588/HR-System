package com.hr.service;

import com.hr.entity.User;

import java.util.List;

public interface UserService {
    
    List<User> getAllUsers();
    
    User getUserById(String userId);
    
    User getUserByUsername(String username);
    
    int createUser(User user);
    
    int updateUser(User user);
    
    int deleteUser(String userId);
    
    List<String> getRoleIdsByUserId(String userId);
    
    List<User> getUsersByPosition(String position);

    List<User> searchActiveUsers(String keyword);

    User getActiveTeamManagerByDepartment(String department);
}
