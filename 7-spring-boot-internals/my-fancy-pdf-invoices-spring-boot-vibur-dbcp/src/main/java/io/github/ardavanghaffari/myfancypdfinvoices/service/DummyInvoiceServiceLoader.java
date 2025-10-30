package io.github.ardavanghaffari.myfancypdfinvoices.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
@RequiredArgsConstructor
public class DummyInvoiceServiceLoader {

    private final InvoiceService invoiceService;

    @PostConstruct
    public void setup() {
        invoiceService.create("someUserId", 50);
        invoiceService.create("someOtherUserId", 100);
    }

}
