package com.dev.ProductsAPI.exceptions;

public class NoContentException extends RuntimeException{
    public NoContentException(String message) {
        super(message);
    }
}
