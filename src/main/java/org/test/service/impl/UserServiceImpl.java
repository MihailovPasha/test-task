package org.test.service.impl;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.test.dto.request.AddEmailRequest;
import org.test.dto.request.AddPhoneRequest;
import org.test.dto.request.UpdateEmailRequest;
import org.test.dto.request.UpdatePhoneRequest;
import org.test.dto.request.UserSearchRequest;
import org.test.dto.response.PageResponseDto;
import org.test.dto.response.UserResponseDto;
import org.test.dto.response.UserSearchResponseDto;
import org.test.exception.ResourceNotFoundException;
import org.test.exception.ValidationException;
import org.test.mapper.UserMapper;
import org.test.model.EmailData;
import org.test.model.PhoneData;
import org.test.model.User;
import org.test.repository.UserRepository;
import org.test.util.UserAccessService;
import org.test.service.UserService;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserAccessService userAccessService;

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponseDto getUserById(Long id) {
        log.info("Получение пользователя с id: {}", id);
        User user = findUserById(id);
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public PageResponseDto<UserSearchResponseDto> searchUsers(LocalDate dateOfBirth, String phone, String name,
                                                              String email, Pageable pageable) {
        UserSearchRequest request = new UserSearchRequest();

        if (dateOfBirth != null) {
            request.setDateOfBirth(dateOfBirth);
        }
        request.setPhone(phone);
        request.setName(name);
        request.setEmail(email);

        log.info("Поиск пользователей с критериями: {}", request);

        Page<User> users = userRepository.searchUsers(request, pageable);
        return userMapper.toPageResponseDto(users);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserResponseDto addEmail(Long userId, AddEmailRequest request) {
        log.info("Добавление email '{}' для пользователя: {}", request.getEmail(), userId);
        userAccessService.checkUser(userId);

        User user = findUserById(userId);

        checkEmailNotUsed(request.getEmail(), userId);

        if (user.getEmails().stream().anyMatch(e -> e.getEmail().equalsIgnoreCase(request.getEmail()))) {
            throw new ValidationException("Email уже существует у данного пользователя: " + request.getEmail());
        }

        EmailData emailData = new EmailData();
        emailData.setEmail(request.getEmail());
        emailData.setUser(user);
        user.getEmails().add(emailData);

        User savedUser = userRepository.save(user);
        log.info("Email '{}' успешно добавлен пользователю: {}", request.getEmail(), userId);

        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserResponseDto updateEmail(Long userId, Long emailId, UpdateEmailRequest request) {
        log.info("Обновление email с id {} на '{}' для пользователя: {}", emailId, request.getEmail(), userId);
        userAccessService.checkUser(userId);

        User user = findUserById(userId);

        EmailData emailData = user.getEmails().stream()
                .filter(e -> e.getId().equals(emailId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Email не найден с id: " + emailId));

        if (emailData.getEmail().equalsIgnoreCase(request.getEmail())) {
            log.debug("Email не изменился, пропускаем обновление");
            return userMapper.toUserResponseDto(user);
        }

        checkEmailNotUsed(request.getEmail(), userId);

        emailData.setEmail(request.getEmail());

        User savedUser = userRepository.save(user);
        log.info("Email успешно обновлен для пользователя: {}", userId);

        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserResponseDto deleteEmail(Long userId, Long emailId) {
        log.info("Удаление email с id {} для пользователя: {}", emailId, userId);
        userAccessService.checkUser(userId);

        User user = findUserById(userId);

        if (user.getEmails().size() <= 1) {
            throw new ValidationException("Нельзя удалить последний email. " +
                    "У пользователя должен быть хотя бы один email");
        }

        EmailData emailData = user.getEmails().stream()
                .filter(e -> e.getId().equals(emailId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Email не найден с id: " + emailId));

        user.getEmails().remove(emailData);

        User savedUser = userRepository.save(user);
        log.info("Email с id {} успешно удален у пользователя: {}", emailId, userId);

        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserResponseDto addPhone(Long userId, AddPhoneRequest request) {
        log.info("Добавление телефона '{}' для пользователя: {}", request.getPhone(), userId);
        userAccessService.checkUser(userId);

        User user = findUserById(userId);

        checkPhoneNotUsed(request.getPhone(), userId);

        if (user.getPhones().stream().anyMatch(p -> p.getPhone().equals(request.getPhone()))) {
            throw new ValidationException("Телефон уже существует у данного пользователя: " + request.getPhone());
        }

        PhoneData phoneData = new PhoneData();
        phoneData.setPhone(request.getPhone());
        phoneData.setUser(user);
        user.getPhones().add(phoneData);

        User savedUser = userRepository.save(user);
        log.info("Телефон '{}' успешно добавлен пользователю: {}", request.getPhone(), userId);

        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserResponseDto updatePhone(Long userId, Long phoneId, UpdatePhoneRequest request) {
        log.info("Обновление телефона с id {} на '{}' для пользователя: {}", phoneId, request.getPhone(), userId);
        userAccessService.checkUser(userId);

        User user = findUserById(userId);

        PhoneData phoneData = user.getPhones().stream()
                .filter(p -> p.getId().equals(phoneId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Телефон не найден с id: " + phoneId));

        if (phoneData.getPhone().equals(request.getPhone())) {
            log.debug("Телефон не изменился, пропускаем обновление");
            return userMapper.toUserResponseDto(user);
        }

        checkPhoneNotUsed(request.getPhone(), userId);

        phoneData.setPhone(request.getPhone());

        User savedUser = userRepository.save(user);
        log.info("Телефон успешно обновлен для пользователя: {}", userId);

        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserResponseDto deletePhone(Long userId, Long phoneId) {
        log.info("Удаление телефона с id {} для пользователя: {}", phoneId, userId);
        userAccessService.checkUser(userId);

        User user = findUserById(userId);

        if (user.getPhones().size() <= 1) {
            throw new ValidationException("Нельзя удалить последний телефон. " +
                    "У пользователя должен быть хотя бы один телефон");
        }

        PhoneData phoneData = user.getPhones().stream()
                .filter(p -> p.getId().equals(phoneId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Телефон не найден с id: " + phoneId));

        user.getPhones().remove(phoneData);

        User savedUser = userRepository.save(user);
        log.info("Телефон с id {} успешно удален у пользователя: {}", phoneId, userId);

        return userMapper.toUserResponseDto(savedUser);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден с id: " + id));
    }

    private void checkEmailNotUsed(String email, Long excludeUserId) {
        userRepository.findByEmails_Email(email).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(excludeUserId)) {
                throw new ValidationException("Email уже используется другим пользователем: " + email);
            }
        });
    }

    private void checkPhoneNotUsed(String phone, Long excludeUserId) {
        userRepository.findByPhones_Phone(phone).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(excludeUserId)) {
                throw new ValidationException("Телефон уже используется другим пользователем: " + phone);
            }
        });
    }
}
