# Notes

## JAR

Traditionally, web applications were packaged as WAR files (Web application Archive). To run the
application, you would deploy the generated WAR into an external servlet container such as Tomcat.
In modern setups, frameworks like Spring Boot produce a _fat jar_ or an _uber jar_ instead. This
single executable JAR bundles your application code along with all required third-party libraries
and, for web apps, an embedded servlet container (like Tomcat), allowing you to run the application
without installing a separate server.

Although [my-fancy-pdf-invoices](./my-fancy-pdf-invoices) is not a Spring Boot application, it is
still capable of producing a _fat jar_ with an embedded Tomcat through the use of Gradle's Shadow
plugin (equivalent to maven-shade-plugin). The application can be run directly from the root of
the project by:

```bash
./gradlew :my-fancy-pdf-invoices:clean :my-fancy-pdf-invoices:run
```

or by first producing a jar file:

```bash
./gradlew :my-fancy-pdf-invoices:clean :my-fancy-pdf-invoices:build # or shadowJar instead of build
java -jar 1-java-webapps-without-spring/my-fancy-pdf-invoices/build/libs/my-fancy-pdf-invoices-1.0-all.jar
```

Using the jar task and specifying the main class in the manifest will package only your
application's classes. It will not include any of its dependencies, which means running the JAR
will likely fail due to missing classes:

```text
// Had we used this instead of the Shadow plugin

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
}

java -jar 1-java-webapps-without-spring/my-fancy-pdf-invoices/build/libs/my-fancy-pdf-invoices-1.0.jar
Error: Unable to initialize main class io.github.ardavanghaffari.myfancypdfinvoices.model.ApplicationLauncher
Caused by: java.lang.NoClassDefFoundError: jakarta/servlet/Servlet
```

## Servlet API

In Java, most web development revolves around
the [Servlet API](https://en.wikipedia.org/wiki/Jakarta_Servlet).
To work with servlets, you need:

- A servlet container (e.g., Tomcat or Jetty) to run servlets and serve them.
- An `HttpServlet` class to handle requests and generate HTML responses.

## Jackson

Jackson lets you annotate your fields or getters with the `@JsonProperty` annotation. Its value
defines what the name of the field is going to be in the resulting JSON string.
