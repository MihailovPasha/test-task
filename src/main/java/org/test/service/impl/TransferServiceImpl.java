package org.test.service.impl;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional
    public TransferResponseDto transfer(Long fromUserId, TransferRequest request) {
        log.info("Старт перевода - от: {}, к: {}, сумма: {}",
                fromUserId, request.getToUserId(), request.getValue());

        userAccessService.checkUser(fromUserId);

        validateTransfer(fromUserId, request);

        Account fromAccount = accountRepository.findByUserId(fromUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Аккаунт отправителя не найден"));

        Account toAccount = accountRepository.findByUserId(request.getToUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Аккаунт получателя не найден"));

        if (fromAccount.getBalance().compareTo(request.getValue()) < 0) {
            throw new InsufficientFundsException(
                    String.format("Недостаточно средств: баланс=%s, требуется=%s",
                            fromAccount.getBalance(), request.getValue()));
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getValue()));
        toAccount.setBalance(toAccount.getBalance().add(request.getValue()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        log.info("Перевод успешно завершён: транзакция от {} к {}, сумма {}",
                fromUserId, request.getToUserId(), request.getValue());

        return transferMapper.toTransferResponseDto(fromUserId, request);
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
