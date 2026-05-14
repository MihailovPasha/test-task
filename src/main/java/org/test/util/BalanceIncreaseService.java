package org.test.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.test.repository.AccountRepository;
import org.test.model.Account;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceIncreaseService {

    private final AccountRepository accountRepository;
    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.10");
    private static final BigDecimal MAX_INCREASE_RATIO = new BigDecimal("2.07");

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void increaseBalances() {
        log.info("Старт запланированного увеличения баланса пользователей");

        List<Account> accounts = accountRepository.findAll();
        int updatedCount = 0;

        for (Account account : accounts) {
            BigDecimal currentBalance = account.getBalance();

            BigDecimal maxAllowedBalance = account.getInitialBalance().multiply(MAX_INCREASE_RATIO);

            BigDecimal potentialNewBalance = currentBalance.multiply(BigDecimal.ONE.add(INTEREST_RATE))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal newBalance = potentialNewBalance.compareTo(maxAllowedBalance) <= 0
                    ? potentialNewBalance
                    : maxAllowedBalance;

            if (newBalance.compareTo(currentBalance) != 0) {
                account.setBalance(newBalance);
                accountRepository.save(account);
                updatedCount++;

                log.debug("Баланс обновлён для пользователя {}: {} -> {} (max: {})",
                        account.getUser().getId(), currentBalance, newBalance, maxAllowedBalance);
            }
        }

        log.info("Пополнение баланса завершено. Обновлено {} аккаунтов", updatedCount);
    }
}
