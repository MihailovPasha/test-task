package org.test.service.impl;

import javax.transaction.Transactional;
import brave.Span;
import brave.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.test.dto.request.TransferRequest;
import org.test.dto.response.TransferResponseDto;
import org.test.exception.InsufficientFundsException;
import org.test.exception.ResourceNotFoundException;
import org.test.exception.ValidationException;
import org.test.mapper.TransferMapper;
import org.test.model.Account;
import org.test.repository.AccountRepository;
import org.test.service.TransferService;
import org.test.util.UserAccessService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final AccountRepository accountRepository;
    private final TransferMapper transferMapper;
    private final UserAccessService userAccessService;
    private final Tracer tracer;

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public TransferResponseDto transfer(Long fromUserId, TransferRequest request) {
        Span span = tracer.nextSpan()
                .name("money-transfer")
                .tag("from.user.id", fromUserId.toString())
                .tag("to.user.id", request.getToUserId().toString())
                .tag("amount", request.getValue().toString())
                .start();

        try (Tracer.SpanInScope spanInScope = tracer.withSpanInScope(span)) {
            log.info("Старт перевода - от: {}, к: {}, сумма: {}",
                    fromUserId, request.getToUserId(), request.getValue());

            userAccessService.checkUser(fromUserId);

            validateTransfer(fromUserId, request);

            Long firstUserId = fromUserId < request.getToUserId() ? fromUserId : request.getToUserId();
            Long secondUserId = fromUserId < request.getToUserId() ? request.getToUserId() : fromUserId;

            Account firstAccount = accountRepository.findByUserId(firstUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Аккаунт не найден"));
            Account secondAccount = accountRepository.findByUserId(secondUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Аккаунт не найден"));

            Account fromAccount = fromUserId.equals(firstUserId) ? firstAccount : secondAccount;
            Account toAccount = fromUserId.equals(firstUserId) ? secondAccount : firstAccount;

            if (fromAccount.getBalance().compareTo(request.getValue()) < 0) {
                span.tag("error", "insufficient_funds");
                throw new InsufficientFundsException(
                        String.format("Недостаточно средств: баланс=%s, требуется=%s",
                                fromAccount.getBalance(), request.getValue()));
            }

            fromAccount.setBalance(fromAccount.getBalance().subtract(request.getValue()));
            toAccount.setBalance(toAccount.getBalance().add(request.getValue()));

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            span.tag("status", "success");

            log.info("Перевод успешно завершён: транзакция от {} к {}, сумма {}",
                    fromUserId, request.getToUserId(), request.getValue());

            return transferMapper.toTransferResponseDto(fromUserId, request);
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }

    }

    private void validateTransfer(Long fromUserId, TransferRequest request) {
        if (fromUserId.equals(request.getToUserId())) {
            throw new ValidationException("Невозможно перевести себе");
        }

        if (request.getValue() == null || request.getValue().signum() <= 0) {
            throw new ValidationException("Сумма перевода должна быть больше 0");
        }
    }
}
