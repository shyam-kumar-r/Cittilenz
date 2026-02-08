package com.civic_reporting.cittilenz.dto.request;

import jakarta.validation.constraints.Size;

public class PasswordChangeRequest {

    private String oldPassword;

    @Size(min = 8)
    private String newPassword;

    @Size(min = 8)
    private String confirmNewPassword;

    public String getOldPassword() { return oldPassword; }
    public String getNewPassword() { return newPassword; }
    public String getConfirmNewPassword() { return confirmNewPassword; }
}
