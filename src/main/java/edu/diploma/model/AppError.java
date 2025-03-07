package edu.diploma.model;

public class AppError {

    private final int code;

    private final String message;

    public AppError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
