package com.civic_reporting.cittilenz.dto.request;

import jakarta.validation.constraints.Email;

public class UserUpdateRequest {

    private String fullName;

    @Email
    private String email;

    private String mobile;

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
}
