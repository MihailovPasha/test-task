package org.test.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateEmailRequest {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;
}