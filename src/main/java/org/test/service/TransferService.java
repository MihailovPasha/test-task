package org.test.service;

import org.test.dto.request.TransferRequest;
import org.test.dto.response.TransferResponseDto;

public interface TransferService {
    TransferResponseDto transfer(Long fromUserId, TransferRequest request);
}
