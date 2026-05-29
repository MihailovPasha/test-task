package org.test.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.test.repository.AccountRepository;
import org.test.model.Account;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceIncreaseService {

    private final AccountRepository accountRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.10");
    private static final BigDecimal MAX_INCREASE_RATIO = new BigDecimal("2.07");

    @Scheduled(fixedRate = 30000)
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void increaseBalances() {
        log.info("Старт запланированного увеличения баланса пользователей");

        List<Account> accounts = accountRepository.findAllByOrderById();
        int updatedCount = 0;

        for (Account account : accounts) {
            entityManager.lock(account, LockModeType.PESSIMISTIC_WRITE);

            BigDecimal currentBalance = account.getBalance();
            BigDecimal initialBalance = account.getInitialBalance();

            BigDecimal maxAllowedBalance = account.getInitialBalance().multiply(MAX_INCREASE_RATIO)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal potentialNewBalance = currentBalance.multiply(BigDecimal.ONE.add(INTEREST_RATE))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal newBalance = potentialNewBalance.min(maxAllowedBalance);

            if (newBalance.compareTo(currentBalance) != 0) {
                account.setBalance(newBalance);
                accountRepository.save(account);
                updatedCount++;

                log.debug("Баланс обновлён для пользователя {}: {} -> {} (макс: {}, нач: {})",
                        account.getUser().getId(), currentBalance, newBalance, maxAllowedBalance, initialBalance);
            }
        }

        log.info("Пополнение баланса завершено. Обновлено {} аккаунтов", updatedCount);
    }
}
