package org.test.service;

import org.test.dto.security.JwtRequest;
import org.test.dto.security.JwtResponse;
import org.test.exception.UserAuthException;

public interface AuthService {
    JwtResponse login(JwtRequest authRequest) throws UserAuthException;
}
