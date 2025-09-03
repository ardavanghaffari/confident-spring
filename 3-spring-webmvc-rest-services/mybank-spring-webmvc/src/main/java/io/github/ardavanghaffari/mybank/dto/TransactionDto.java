package io.github.ardavanghaffari.mybank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionDto {

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String reference;
}
