package org.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.test.dto.request.AddEmailRequest;
import org.test.dto.request.AddPhoneRequest;
import org.test.dto.request.UpdateEmailRequest;
import org.test.dto.request.UpdatePhoneRequest;
import org.test.dto.response.PageResponseDto;
import org.test.dto.response.UserResponseDto;
import org.test.dto.response.UserSearchResponseDto;
import org.test.service.UserService;
import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Управление пользователями и их данными")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск пользователей с фильтрацией и пагинацией")
    public ResponseEntity<PageResponseDto<UserSearchResponseDto>> searchUsers(
            @Parameter(description = "Дата рождения (фильтр: больше чем)")
            @RequestParam(required = false) LocalDate dateOfBirth,

            @Parameter(description = "Телефон (100% совпадение)")
            @RequestParam(required = false) String phone,

            @Parameter(description = "Имя (поиск по началу строки)")
            @RequestParam(required = false) String name,

            @Parameter(description = "Email (100% совпадение)")
            @RequestParam(required = false) String email,

            @Parameter(description = "Номер страницы")
            @RequestParam(defaultValue = "0", name = "page") int page,

            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20", name = "size") int size,

            @Parameter(description = "Поле для сортировки")
            @RequestParam(defaultValue = "id", name = "sort") String sort,

            @Parameter(description = "Направление сортировки (asc/desc)")
            @RequestParam(defaultValue = "asc", name = "direction") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toLowerCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponseDto<UserSearchResponseDto> response =
                userService.searchUsers(dateOfBirth, phone, name, email, pageable);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/email")
    @Operation(summary = "Добавить email")
    public ResponseEntity<UserResponseDto> addEmail(
            @Valid @RequestBody AddEmailRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(userService.addEmail(userId, request));
    }

    @PutMapping("/email/{emailId}")
    @Operation(summary = "Обновить email")
    public ResponseEntity<UserResponseDto> updateEmail(
            @PathVariable Long emailId,
            @Valid @RequestBody UpdateEmailRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(userService.updateEmail(userId, emailId, request));
    }

    @DeleteMapping("/email/delete/{emailId}")
    @Operation(summary = "Удалить email")
    public ResponseEntity<UserResponseDto> deleteEmail(
            @PathVariable Long emailId,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(userService.deleteEmail(userId, emailId));
    }

    @PostMapping("/phone")
    @Operation(summary = "Добавить телефон")
    public ResponseEntity<UserResponseDto> addPhone(
            @Valid @RequestBody AddPhoneRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(userService.addPhone(userId, request));
    }

    @PutMapping("/phone/{phoneId}")
    @Operation(summary = "Обновить телефон")
    public ResponseEntity<UserResponseDto> updatePhone(
            @PathVariable Long phoneId,
            @Valid @RequestBody UpdatePhoneRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(userService.updatePhone(userId, phoneId, request));
    }

    @DeleteMapping("/phone/delete/{phoneId}")
    @Operation(summary = "Удалить телефон")
    public ResponseEntity<UserResponseDto> deletePhone(
            @PathVariable Long phoneId,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(userService.deletePhone(userId, phoneId));
    }
}
