package org.test.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchResponseDto {
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private List<String> emails;
    private List<String> phones;
}
