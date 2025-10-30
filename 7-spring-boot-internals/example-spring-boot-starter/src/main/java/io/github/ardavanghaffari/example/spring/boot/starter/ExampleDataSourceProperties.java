package io.github.ardavanghaffari.example.spring.boot.starter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "vibure.datasource")
public class ExampleDataSourceProperties {

    private String username;
    private String password;
    private String url;
    private String driverClassName;

}
