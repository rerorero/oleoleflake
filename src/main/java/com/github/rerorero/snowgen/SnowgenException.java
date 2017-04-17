package com.github.rerorero.snowgen;

public class SnowgenException extends Exception {
    public SnowgenException(String message) {
        super(message);
    }

    public SnowgenException(Throwable cause) {
        super(cause);
    }

    public SnowgenException(String message, Throwable cause) {
        super(message, cause);
    }
}
