package ru.terentyev.pionerpixel_testtask.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EmailDTO extends AbstractDTO {

    private Long id;
    @NotBlank
    @Email
    @Size(max = 200)
    private String email;
}