// ResourceNotFoundException.java
package com.zjgsu.ms.hxy.catalog.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}