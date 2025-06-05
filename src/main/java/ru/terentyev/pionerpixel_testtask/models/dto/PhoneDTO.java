package ru.terentyev.pionerpixel_testtask.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PhoneDTO extends AbstractDTO {
    private Long id;
    @NotBlank
    @Pattern(regexp = "^7\\d{10}$")
    @Size(max = 11)
    private String phone;
}
