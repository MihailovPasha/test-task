package org.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.test.dto.request.TransferRequest;
import org.test.dto.response.TransferResponseDto;
import org.test.service.TransferService;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Tag(name = "Переводы", description = "Операции перевода средств между пользователями")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @Operation(summary = "Перевод суммы по id пользователя")
    public ResponseEntity<TransferResponseDto> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(transferService.transfer(userId, request));
    }
}