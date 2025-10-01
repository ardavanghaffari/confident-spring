package io.github.ardavanghaffari.mybank.web.forms;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionForm {

    @NotNull
    @DecimalMin("0.01")
    @Max(100)
    private BigDecimal amount;

    @NotBlank
    @Size(min = 1, max = 25)
    private String reference;

    @NotBlank
    private String receivingUser;
}
