package ru.terentyev.pionerpixel_testtask.models.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AccountDTO extends AbstractDTO {
    private Long id;
    private BigDecimal balance;
}
