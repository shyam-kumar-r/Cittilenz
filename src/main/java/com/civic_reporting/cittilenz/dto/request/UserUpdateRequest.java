package com.civic_reporting.cittilenz.dto.request;

import jakarta.validation.constraints.Email;

public class UserUpdateRequest {
	
	private String username;
	
    private String fullName;

    @Email
    private String email;

    private String mobile;

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
    
    
}
