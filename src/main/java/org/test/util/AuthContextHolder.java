package org.test.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.test.security.JwtAuthentication;

@Component
public class AuthContextHolder {

    public static Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthentication && auth.isAuthenticated()) {
            JwtAuthentication jwtAuth = (JwtAuthentication) auth;
            return jwtAuth.getUserId();
        }
        return null;
    }

    public static String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthentication && auth.isAuthenticated()) {
            JwtAuthentication jwtAuth = (JwtAuthentication) auth;
            return jwtAuth.getEmail();
        }
        return null;
    }

}
