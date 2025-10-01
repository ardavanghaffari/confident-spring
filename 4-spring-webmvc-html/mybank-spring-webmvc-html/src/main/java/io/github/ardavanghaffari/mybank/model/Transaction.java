package io.github.ardavanghaffari.mybank.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
public class Transaction {

    private String id;
    private BigDecimal amount;
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd'T'HH:mm'Z'", timezone = "Europe/Amsterdam")
    private Instant timestamp;
    private String reference;
    private String bankSlogan;
    private String receivingUser;

    public Transaction(BigDecimal amount,
                       Instant timestamp,
                       String reference,
                       String bankSlogan,
                       String receivingUser) {

        this.id = UUID.randomUUID().toString();
        this.amount = amount;
        this.timestamp = timestamp;
        this.reference = reference;
        this.bankSlogan = bankSlogan;
        this.receivingUser = receivingUser;
    }

}
