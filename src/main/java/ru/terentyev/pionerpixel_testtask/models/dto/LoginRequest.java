package ru.terentyev.pionerpixel_testtask.models.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class LoginRequest extends AbstractDTO {

    @NotBlank
    private String identifier;
    @NotBlank
    private String password;
}
