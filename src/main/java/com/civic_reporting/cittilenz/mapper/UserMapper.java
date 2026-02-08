package com.civic_reporting.cittilenz.mapper;

import com.civic_reporting.cittilenz.dto.response.UserResponse;
import com.civic_reporting.cittilenz.entity.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setUsername(user.getUsername());
        r.setFullName(user.getFullName());
        r.setMobile(user.getMobile());   
        r.setEmail(user.getEmail());
        r.setRole(user.getRole());
        r.setWardId(user.getWardId());
        r.setDepartmentId(user.getDepartmentId());    
        return r;
    }
}

