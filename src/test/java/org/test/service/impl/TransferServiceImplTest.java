package org.test.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.test.dto.request.TransferRequest;
import org.test.dto.response.TransferResponseDto;
import org.test.exception.InsufficientFundsException;
import org.test.exception.ResourceNotFoundException;
import org.test.exception.ValidationException;
import org.test.mapper.TransferMapper;
import org.test.model.Account;
import org.test.model.User;
import org.test.repository.AccountRepository;
import org.test.util.UserAccessService;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferMapper transferMapper;

    @Mock
    private UserAccessService userAccessService;

    @InjectMocks
    private TransferServiceImpl transferService;

    private Account fromAccount;
    private Account toAccount;
    private TransferRequest request;
    private TransferResponseDto expectedResponse;

    @BeforeEach
    void setUp() {
        User fromUser = new User();
        fromUser.setId(1L);
        fromUser.setName("Иван");

        User toUser = new User();
        toUser.setId(2L);
        toUser.setName("Петр");

        fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setUser(fromUser);
        fromAccount.setBalance(new BigDecimal("1000.00"));
        fromAccount.setInitialBalance(new BigDecimal("1000.00"));

        toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setUser(toUser);
        toAccount.setBalance(new BigDecimal("500.00"));
        toAccount.setInitialBalance(new BigDecimal("500.00"));

        request = new TransferRequest();
        request.setToUserId(2L);
        request.setValue(new BigDecimal("100.00"));

        expectedResponse = new TransferResponseDto();
        expectedResponse.setTransactionId(123L);
        expectedResponse.setFromUserId(1L);
        expectedResponse.setToUserId(2L);
        expectedResponse.setAmount(new BigDecimal("100.00"));
        expectedResponse.setStatus("SUCCESS");
    }

    @Test
    void testSuccessfulTransfer() {
        doNothing().when(userAccessService).checkUser(1L);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.of(toAccount));
        when(transferMapper.toTransferResponseDto(1L, request)).thenReturn(expectedResponse);

        TransferResponseDto result = transferService.transfer(1L, request);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(new BigDecimal("900.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("600.00"), toAccount.getBalance());

        verify(userAccessService).checkUser(1L);
        verify(accountRepository).save(fromAccount);
        verify(accountRepository).save(toAccount);
    }

    @Test
    void testTransferToSelf() {
        request.setToUserId(1L);
        doNothing().when(userAccessService).checkUser(1L);

        assertThrows(ValidationException.class, () -> transferService.transfer(1L, request));

        verify(accountRepository, never()).findByUserId(any());
    }

    @Test
    void testTransferInsufficientFunds() {
        request.setValue(new BigDecimal("2000.00"));
        doNothing().when(userAccessService).checkUser(1L);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.of(toAccount));

        assertThrows(InsufficientFundsException.class, () -> transferService.transfer(1L, request));

        assertEquals(new BigDecimal("1000.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("500.00"), toAccount.getBalance());
    }

    @Test
    void testTransferSenderNotFound() {
        doNothing().when(userAccessService).checkUser(1L);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transferService.transfer(1L, request));
    }

    @Test
    void testTransferReceiverNotFound() {
        doNothing().when(userAccessService).checkUser(1L);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transferService.transfer(1L, request));
    }

    @Test
    void testTransferNegativeAmount() {
        request.setValue(new BigDecimal("-100.00"));
        doNothing().when(userAccessService).checkUser(1L);

        assertThrows(ValidationException.class, () -> transferService.transfer(1L, request));
    }

    @Test
    void testTransferZeroAmount() {
        request.setValue(BigDecimal.ZERO);
        doNothing().when(userAccessService).checkUser(1L);

        assertThrows(ValidationException.class, () -> transferService.transfer(1L, request));
    }

    @Test
    void testTransferAllBalance() {
        request.setValue(new BigDecimal("1000.00"));
        doNothing().when(userAccessService).checkUser(1L);
        when(accountRepository.findByUserId(1L)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.of(toAccount));
        when(transferMapper.toTransferResponseDto(1L, request)).thenReturn(expectedResponse);

        TransferResponseDto result = transferService.transfer(1L, request);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("1500.00"), toAccount.getBalance());
    }
}