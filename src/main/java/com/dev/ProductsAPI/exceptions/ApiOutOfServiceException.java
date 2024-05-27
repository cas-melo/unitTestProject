package com.dev.ProductsAPI.exceptions;

public class ApiOutOfServiceException extends RuntimeException{
    public ApiOutOfServiceException(String message) {
        super(message);
    }

    public ApiOutOfServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
