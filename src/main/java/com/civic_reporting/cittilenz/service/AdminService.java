package com.civic_reporting.cittilenz.service;

import com.civic_reporting.cittilenz.dto.request.AdminPasswordResetRequest;
import com.civic_reporting.cittilenz.dto.request.AdminUserCreateRequest;
import com.civic_reporting.cittilenz.dto.request.AdminUserUpdateRequest;
import com.civic_reporting.cittilenz.dto.response.UserResponse;
import com.civic_reporting.cittilenz.entity.User;

import java.util.List;

public interface AdminService {

    UserResponse createUser(AdminUserCreateRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUser(Integer id);

    void toggleStatus(Integer id);

    void resetPassword(Integer id, AdminPasswordResetRequest request);

    void deleteUser(Integer id);
    
    User updateUser(Integer userId, AdminUserUpdateRequest request);

}
