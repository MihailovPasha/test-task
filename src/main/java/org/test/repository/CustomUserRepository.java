package org.test.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.test.dto.request.UserSearchRequest;
import org.test.model.User;

public interface CustomUserRepository {
    Page<User> searchUsers(UserSearchRequest request, Pageable pageable);
}
