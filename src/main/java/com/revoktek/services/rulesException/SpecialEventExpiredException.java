package com.revoktek.services.rulesException;

public class SpecialEventExpiredException extends RuntimeException {
    public SpecialEventExpiredException(String message) {
        super(message);
    }
}
