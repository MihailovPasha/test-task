package org.test.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.test.dto.request.TransferRequest;
import org.test.dto.response.TransferResponseDto;
import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class, LocalDateTime.class})
public interface TransferMapper {

    @Mapping(target = "transactionId", expression = "java(generateTransactionId())")
    @Mapping(target = "fromUserId", source = "fromUserId")
    @Mapping(target = "toUserId", source = "request.toUserId")
    @Mapping(target = "amount", source = "request.value")
    @Mapping(target = "timestamp", expression = "java(LocalDateTime.now())")
    @Mapping(target = "status", constant = "SUCCESS")
    TransferResponseDto toTransferResponseDto(Long fromUserId, TransferRequest request);

    default Long generateTransactionId() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }
}