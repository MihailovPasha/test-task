package org.test.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.test.dto.security.JwtRequest;
import org.test.dto.security.JwtResponse;
import org.test.exception.UserAuthException;
import org.test.model.User;
import org.test.repository.UserRepository;
import org.test.security.JwtService;
import org.test.service.AuthService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public JwtResponse login(JwtRequest authRequest) throws UserAuthException {
        log.info("Попытка входа пользователя: {}", authRequest.getLogin());

        User user = userRepository.findByEmails_Email(authRequest.getLogin())
                .or(() -> userRepository.findByPhones_Phone(authRequest.getLogin()))
                .orElseThrow(() -> new UserAuthException("Неверный логин или пароль"));

        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            log.warn("Неверный пароль для пользователя: {}", authRequest.getLogin());
            throw new UserAuthException("Неверный пароль");
        }

        String accessToken = jwtService.generateAccessToken(user.getId());

        log.info("Пользователь успешно аутентифицирован: id={}", user.getId());
        return new JwtResponse(accessToken);
    }
}
