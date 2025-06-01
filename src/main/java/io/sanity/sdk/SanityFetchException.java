package io.sanity.sdk;

public class SanityFetchException extends RuntimeException {
    public SanityFetchException(String message) {
        super(message);
    }

    public SanityFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}