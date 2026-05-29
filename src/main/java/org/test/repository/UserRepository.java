package org.test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.test.model.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, CustomUserRepository {
    Optional<User> findByEmails_Email(String email);
    Optional<User> findByPhones_Phone(String phone);
}
