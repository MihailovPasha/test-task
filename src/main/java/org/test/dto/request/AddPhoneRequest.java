package org.test.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddPhoneRequest {
    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^7\\d{10}$", message = "Телефон должен быть в формате 79008007060")
    private String phone;
}