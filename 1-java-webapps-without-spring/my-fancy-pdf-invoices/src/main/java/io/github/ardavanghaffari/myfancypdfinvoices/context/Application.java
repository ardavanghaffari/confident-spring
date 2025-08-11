package io.github.ardavanghaffari.myfancypdfinvoices.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ardavanghaffari.myfancypdfinvoices.service.InvoiceService;
import io.github.ardavanghaffari.myfancypdfinvoices.service.UserService;

public class Application {

    public static final UserService userService = new UserService();
    public static final InvoiceService invoiceService = new InvoiceService(userService);
    public static final ObjectMapper objectMapper = new ObjectMapper();
}
