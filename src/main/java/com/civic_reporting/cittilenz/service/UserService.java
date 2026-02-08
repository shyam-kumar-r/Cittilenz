package com.civic_reporting.cittilenz.service;

import com.civic_reporting.cittilenz.dto.request.PasswordChangeRequest;
import com.civic_reporting.cittilenz.dto.request.UserRegisterRequest;
import com.civic_reporting.cittilenz.dto.request.UserUpdateRequest;
import com.civic_reporting.cittilenz.entity.User;

import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

    User registerCitizen(UserRegisterRequest request);

    User getCurrentUser();

    User updateCurrentUser(UserUpdateRequest request);

    void changePassword(PasswordChangeRequest request);

    void deactivateCurrentUser();

    void deleteCurrentUser(HttpServletRequest request);
}
