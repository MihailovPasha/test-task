package org.test.security;

import io.jsonwebtoken.Claims;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUtils {
    public static JwtAuthentication generate(Claims claims) {
        final JwtAuthentication jwtInfoToken = new JwtAuthentication();
        jwtInfoToken.setUserId(Long.valueOf(claims.getSubject()));
        jwtInfoToken.setEmail(getEmail(claims));
        return jwtInfoToken;
    }

    private static String getEmail(Claims claims) {
        return claims.get("email", String.class);
    }
}
