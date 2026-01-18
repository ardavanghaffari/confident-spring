package io.github.ardavanghaffari.mybank.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
@Table("transactions")
public class Transaction {

    @Id
    private String id;
    private BigDecimal amount;
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd'T'HH:mm'Z'", timezone = "Europe/Amsterdam")
    private Instant timestamp;
    private String reference;
    private String bankSlogan;
    private String receivingUser;

    public Transaction() {
    }

    public Transaction(BigDecimal amount,
                       Instant timestamp,
                       String reference,
                       String bankSlogan,
                       String receivingUser) {

        this.amount = amount;
        this.timestamp = timestamp;
        this.reference = reference;
        this.bankSlogan = bankSlogan;
        this.receivingUser = receivingUser;
    }

}
