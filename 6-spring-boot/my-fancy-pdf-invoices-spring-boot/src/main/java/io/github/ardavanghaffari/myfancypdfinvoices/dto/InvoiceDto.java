package io.github.ardavanghaffari.myfancypdfinvoices.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceDto {

    @JsonProperty("user_id")
    @NotBlank
    private String userId;

    @Min(10)
    @Max(50)
    private Integer amount;

}
