package org.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.test.dto.security.JwtRequest;
import org.test.dto.security.JwtResponse;
import org.test.exception.UserAuthException;
import org.test.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Вход в систему по email/phone и паролю")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody JwtRequest request) throws UserAuthException {
        return ResponseEntity.ok(authService.login(request));
    }
}