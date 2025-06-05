package ru.terentyev.pionerpixel_testtask.models.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UpdateUserRequest extends AbstractDTO {

    private Set<EmailDTO> emails;
    private Set<PhoneDTO> phones;
}
