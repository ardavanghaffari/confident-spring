package io.github.ardavanghaffari.myfancypdfinvoices.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@Table("invoices")
public class Invoice {

    @Id
    private String id;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("pdf_url")
    private String pdfUrl;

    private Integer amount;

    public Invoice() {
    }

    public Invoice(String userId, Integer amount, String pdfUrl) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.pdfUrl = pdfUrl;
        this.amount = amount;
    }
}
