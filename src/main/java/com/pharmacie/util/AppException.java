package com.pharmacie.util;

/**
 * Exception metier applicative (message destine a l'utilisateur).
 */
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
