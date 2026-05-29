package org.test.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.test.exception.AccessException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAccessService {

    public void checkUser(Object targetId) {
        Long currentUserId = AuthContextHolder.getUserId();

        if (currentUserId == null) {
            log.warn("Попытка доступа не аутентифицированного пользователя");
            throw new AccessException("Необходима аутентификация");
        }

        if (!currentUserId.equals(targetId)) {
            log.warn("Отказано в доступе: пользователь {} попытался получить доступ к {}",
                    currentUserId, targetId);
            throw new AccessException("Доступ запрещен: можно изменять только свои данные");
        }
    }
}