package io.github.ardavanghaffari.myfancypdfinvoices.web.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginForm {

    @NotBlank
    @Size(min = 5, max = 20)
    private String username, password;
}
