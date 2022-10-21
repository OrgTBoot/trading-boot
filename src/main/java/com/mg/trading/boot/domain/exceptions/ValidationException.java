package com.mg.trading.boot.domain.exceptions;

public class ValidationException extends RuntimeException {

    public ValidationException(String msg) {
        super(msg);
    }

    public ValidationException(Throwable e) {
        super(e);
    }
}
