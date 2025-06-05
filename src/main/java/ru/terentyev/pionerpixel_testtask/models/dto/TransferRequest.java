package ru.terentyev.pionerpixel_testtask.models.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TransferRequest extends AbstractDTO {

    @NotNull
    private Long toUserId;
    @NotNull
    @Positive
    private BigDecimal amount;
}
