package org.test.service;

import org.springframework.data.domain.Pageable;
import org.test.dto.request.AddEmailRequest;
import org.test.dto.request.AddPhoneRequest;
import org.test.dto.request.UpdateEmailRequest;
import org.test.dto.request.UpdatePhoneRequest;
import org.test.dto.request.UserSearchRequest;
import org.test.dto.response.PageResponseDto;
import org.test.dto.response.UserResponseDto;
import org.test.dto.response.UserSearchResponseDto;

public interface UserService {
    UserResponseDto getUserById(Long id);
    PageResponseDto<UserSearchResponseDto> searchUsers(UserSearchRequest request, Pageable pageable);
    UserResponseDto addEmail(Long userId, AddEmailRequest request);
    UserResponseDto updateEmail(Long userId, Long emailId, UpdateEmailRequest request);
    UserResponseDto deleteEmail(Long userId, Long emailId);
    UserResponseDto addPhone(Long userId, AddPhoneRequest request);
    UserResponseDto updatePhone(Long userId, Long phoneId, UpdatePhoneRequest request);
    UserResponseDto deletePhone(Long userId, Long phoneId);
}
