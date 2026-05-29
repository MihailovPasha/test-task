package org.test.exception;

import javax.security.auth.message.AuthException;

public class UserAuthException extends AuthException {
    public UserAuthException(String message) {
        super(message);
    }
}
