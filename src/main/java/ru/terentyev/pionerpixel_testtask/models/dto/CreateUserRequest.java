package ru.terentyev.pionerpixel_testtask.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CreateUserRequest extends AbstractDTO {
    @NotBlank
    @Size(max = 500)
    private String name;
    @NotBlank
    private String dateOfBirth;
    @NotBlank
    @Size(min = 8, max = 500)
    private String password;
    private BigDecimal initialBalance;
    private Set<EmailDTO> emails;
    private Set<PhoneDTO> phones;
}
