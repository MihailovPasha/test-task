package org.test.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponseDto {
    private Long transactionId;
    private Long fromUserId;
    private Long toUserId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String status;
}
