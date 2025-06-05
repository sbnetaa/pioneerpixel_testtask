package ru.terentyev.pionerpixel_testtask.models.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserDTO extends AbstractDTO {
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private Set<EmailDTO> emails;
    private Set<PhoneDTO> phones;
    private AccountDTO account;
}
