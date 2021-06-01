package com.mytlogos.enterprise.tools;

import java.io.IOException;

public class NotEnoughSpaceException extends IOException {
    public NotEnoughSpaceException() {
    }

    public NotEnoughSpaceException(String message) {
        super(message);
    }

    public NotEnoughSpaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughSpaceException(Throwable cause) {
        super(cause);
    }
}
