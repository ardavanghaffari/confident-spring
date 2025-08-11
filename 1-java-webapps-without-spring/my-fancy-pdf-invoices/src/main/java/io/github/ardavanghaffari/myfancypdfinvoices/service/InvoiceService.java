package io.github.ardavanghaffari.myfancypdfinvoices.service;

import io.github.ardavanghaffari.myfancypdfinvoices.model.Invoice;
import io.github.ardavanghaffari.myfancypdfinvoices.model.User;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
public class InvoiceService {

    private final UserService userService;

    private final List<Invoice> invoices = new CopyOnWriteArrayList<>();

    public List<Invoice> findAll() {
        return invoices;
    }

    public Invoice create(String userId, Integer amount) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new IllegalStateException();
        }

        Invoice invoice = new Invoice(userId, amount, "https://pdfobject.com/pdf/sample.pdf");
        invoices.add(invoice);
        return invoice;
    }
}
