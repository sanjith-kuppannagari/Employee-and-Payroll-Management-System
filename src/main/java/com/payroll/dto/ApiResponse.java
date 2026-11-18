package com.payroll.dto;

public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean success, String message) {
        this(success, message, null);
    }

    public boolean isSuccess() { return this.success; }
    public String getMessage() { return this.message; }
    public T getData() { return this.data; }

    public String toJson(String rawDataJson) {
        String safeMsg = message == null ? "" : message.replace("\"", "\\\"").replace("\n", "\\n");
        return "{\"success\":" + success + ",\"message\":\"" + safeMsg + "\",\"data\":" + (rawDataJson == null ? "null" : rawDataJson) + "}";
    }
}
