package com.civic_reporting.cittilenz.dto.response;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private int status;
    private LocalDateTime timestamp;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message, T data, int status) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.setStatus(status);
        this.timestamp = LocalDateTime.now();
    }

    /* =========================
       Getters
    ========================= */

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public int getStatus() {
		return status;
	}

    /* =========================
       Setters
    ========================= */

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setStatus(int status) {
		this.status = status;
	}

    /* =========================
       Static Helper Methods
    ========================= */

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, 200);
    }

    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(true, msg, data, 200);
    }

    public static <T> ApiResponse<T> error(String msg, int status) {
        return new ApiResponse<>(false, msg, null, status);
    }
}
