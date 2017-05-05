package com.github.rerorero.oleoleflake;

public class OleOleFlakeException extends RuntimeException {
    public OleOleFlakeException(String message) {
        super(message);
    }

    public OleOleFlakeException(Throwable cause) {
        super(cause);
    }

    public OleOleFlakeException(String message, Throwable cause) {
        super(message, cause);
    }
}
