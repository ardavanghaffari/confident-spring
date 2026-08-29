# Spring Boot

In this module, we're going to learn about what Spring Boot offers compared to what we've been
building manually using plain Spring.

## spring-boot-starter-parent

Creating a new Spring Boot project from scratch is easiest via
the [initializer](https://start.spring.io/). It's possible to choose the build tool among other
things, i.e. Maven or Gradle. In case of Maven, the project inherits from a
`spring-boot-starter-parent` project, like this:

```xml

<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.0</version>
</parent>
```

This entails:

- `spring-boot-starter-parent` acts as a _parent pom_. Our project is now inheriting its
  configuration from Spring Boot's parent pom.
- Provides default plugin and dependency management. It preconfigures a lot of Maven details so we
  don't have to. Versions for common dependencies are automatically managed, so we can omit versions
  when declaring dependencies.
- It also manages versions for plugins like
    - maven-compiler-plugin
    - maven-surefire-plugin
    - spring-boot-maven-plugin

The initializer declares only two dependencies and one Maven plugin. `spring-boot-starter-web`
sets up Spring, Spring MVC and more. `spring-boot-starter-test` for Spring Boot testing
facilities. `spring-boot-maven-plugin` which, among other things, will make sure that after building
your project with `mvn clean package`, you'll get a correctly working, executable fat JAR which
looks so simple, but is a fair amount of work behind the scenes. You can think of it, for now, as
being somewhat equivalent to the shade/shadow plugin, though it works entirely differently, under
the hood.

Gradle doesn't have a _parent_ concept like Maven, so we can't use `spring-boot-starter-parent`
directly. Spring Boot does however provide the _Gradle equivalent_ through plugins and dependency
management.

```kotlin
plugins {
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("java")
}
```

These two plugins together play the same role as the Spring Boot parent POM in Maven.
`org.springframework.boot` adds among other things tasks like bootRun, bootJar, bootBuildImage and
`io.spring.dependency-management` imports the same managed dependency versions that
`spring-boot-starter-parent` would in Maven, so we can declare dependencies without specifying their
versions.

Run `./gradlew :my-fancy-pdf-invoices-spring-boot:dependencies` to see all the transitive
dependencies. You'll find all the dependencies we manually specified in the previous modules:

- The embedded Tomcat dependency: tomcat-embed-core.
- Spring dependencies like spring-webmvc and spring-context.
- jackson-databind for JSON conversions.
- A bunch of other libraries, e.g. logback-classic for logging.

This partially explains why so much stuff works out of the box with Spring Boot, because its
spring-boot-starter-web pulls in a ton of other third-party libraries by default.

## Bootify the project

These were the changes needed to Bootify the project:

- The Spring Boot project created by the initializer declares `spring-boot-starter-web`,
  `spring-boot-starter-test` and `junit-platform-launcher` as dependencies. Putting aside the test
  dependencies, `spring-boot-starter-web` only pulls in some of the dependencies we had to manually
  add in the previous modules.
  See [the build file](my-fancy-pdf-invoices-spring-boot/build.gradle.kts) for the complete list but
  most prominently, we still had to add `spring-boot-starter-jdbc`,
  `spring-boot-starter-validation`, `jackson-dataformat-xml` and H2 to make the code working.
- Add a main class annotated with `@SpringBootApplication`. No need to tell it where to scan for
  components (configuration, bean definitions, etc.) because it will by default look in its own
  package and sub-packages. `@SpringBootApplication` is a meta-annotation that combines
  `@Configuration`, `@EnableAutoConfiguration` and `@ComponentScan`. By default, `@ComponentScan`
  uses the package of the class it's declared on as its base package.
- Stop specifying the main class in build.gradle.kts file. We're now using the Spring Boot Gradle
  plugin which automatically looks for a class with a main method annotated with
  `@SpringBootApplication`. We can therefore also remove the `ApplicationLauncher` class.
- Remove `@ComponentScan(basePackageClasses = ApplicationLauncher.class)` from
  `ApplicationConfiguration`.
- Apart from having to manually add `spring-boot-starter-validation` as a dependency, validation
  works out of the box. We can remove the `MethodValidationPostProcessor` bean form the
  configuration.
- Same as in the previous modules, add the `jackson-dataformat-xml` to the list of dependencies to
  be able to receive XML next to JSON from the REST endpoints. Specifying a version should not be
  necessary as it is pre-defined by the _spring-dependency-management_ Gradle plugin (or Spring
  Boot's parent pom when using Maven).
- For database connectivity in Spring Boot:
    - Add the `spring-boot-starter-jdbc` dependency, again without a version. This adds Spring
      framework's JDBCTemplate support. Not to be confused with `spring-data` (module 8).
    - Add the H2 dependency.
    - These two dependencies are enough for Spring Boot to automatically create an embedded H2
      database whenever you start up your application.
    - Add relevant DataSource config to `application.properties` file. Before, we created the
      DataSource bean in Java code. Here Spring Boot uses the configuration and creates it for us.
    - Spring Boot automatically looks for a file named `schema.sql` and executes it when the
      application starts. Independent of the database and any H2 specific connection strings. This
      is Spring Boot's poor man's version of _Flyway_ or _Liquibase_. So we no longer have to add
      the INIT script to the `datasource.url` like we did previously (in Java code).
    - Remove `DataSource`, `JdbcTemplate` and `TransactionManager` beans.
    - Remove the following annotations from `ApplicationConfiguration`:
      ```java
      @PropertySource("classpath:/application.properties")
      @PropertySource(value = "classpath:/application-${spring.profiles.active}.properties",
          ignoreResourceNotFound = true)
      @EnableWebMvc
      @EnableTransactionManagement
      ```
    - For HTML support:
        - Replace `expressly` and `thymeleaf-spring6` dependencies with
          `spring-boot-starter-thymeleaf`.
        - Remove `ThymeleafViewResolver`, `SpringTemplateEngine` and
          `SpringResourceTemplateResolver` beans from the configuration. _ApplicationConfiguration_
          class can be removed all together.
        - Spring Boot _autoconfigures_ Thymeleaf with sane defaults. No need for manual bean
          definitions. Use properties to override the defaults if needed.
- When Bootifying the _MyBank_ application, we see that accepting `application/json` from the Rest
  controller works out of the box, however asking the data in `application/xml` format throws an
  exception that `Java 8 date/time type java.time.Instant is not supported by default` and that we
  should `register the com.fasterxml.jackson.datatype:jackson-datatype-jsr310 module` to enable
  handling. The reason that JSON works by default and XML not, seems to be that jackson-databind
  which is pulled in through spring-boot-starter-web uses a mapper for JSON that is already
  configured with a default InstantSerializer, while for XML a different mapper is used that doesn't
  have the same built-in serializer. Having the `jackson-datatype-jsr310` dependency on the
  classpath ensures correct configuration for XML as well.

## Spring Boot's autoconfiguration

If you specify properties like `spring.datasource.xxx` in your _application.properties_, then Spring
Boot will actually create a DataSource @Bean for you, even if you don't see it. In the same way, it
will also create a JdbcTemplate, so that you can inject it into any class you want. It will also
automatically look for and run that `schema.sql` script. It will automatically enable
TransactionManagement features, so that `@Transactional` works out of the box. This leads to the
conclusion that with Spring Boot you define a ton of beans through specifying properties, instead
of writing Java classes. And that Spring Boot, with these properties, creates a ton of @beans and
other stuff you never directly see. This is what's called Spring Boot's autoconfiguration.

When comparing the Spring Boot version of the code with the Spring Web MVC version from the
previous modules, we see that Spring Boot only takes plain Spring framework features, and configures
them _for us_, behind the scenes:

- It boots up an embedded Tomcat when you run the SpringBootApplication main method.
- It automatically generates beans from `application.properties`.
- It makes dependency management much nicer. We don't need to worry about versions anymore and can
  import starter libraries like `spring-boot-starter-jdbc`, that will automatically pull in all
  other necessary dependencies.

Spring Boot is hiding exactly what we did in the previous module (starting Tomcat, registering
DispatcherServlet, creating DataSources, using the _Shadow_ plugin to produce a fat JAR, etc.).
It still has to happen behind the scenes. There's no way around it!

## Thoughts On Spring

- Use the [topical guides](https://spring.io/guides) to get started quickly with a certain topic.
- Use the [Spring Data](https://spring.io/projects/spring-data)
  and [Spring MVC / WebFlux](https://docs.spring.io/spring-framework/reference/) projects if your
  app uses persistence and HTTP API respectively.
- Two projects for learning
  purposes: [PetClinic](https://github.com/spring-projects/spring-petclinic)
  and [COVID-19 contact tracking application](https://github.com/quarano/quarano-application).

## Commands used in this module

```bash
./gradlew :my-fancy-pdf-invoices-spring-boot:clean :my-fancy-pdf-invoices-spring-boot:build
java -Dspring.profiles.active=dev -jar 6-spring-boot/my-fancy-pdf-invoices-spring-boot/build/libs/my-fancy-pdf-invoices-spring-boot-1.0.jar
curl -X GET "http://localhost:8080/invoices"
curl -X POST "http://localhost:8080/invoices" -H "Content-Type: application/json" -d '{"amount":"20","user_id":"ari"}'
curl -X POST "http://localhost:8080/invoices" -H "Accept: application/xml" -H "Content-Type: application/json" -d '{"amount":"20","user_id":"ari"}'

./gradlew :mybank-spring-boot:clean :mybank-spring-boot:build
java -jar 6-spring-boot/mybank-spring-boot/build/libs/mybank-spring-boot-1.0-all.jar
curl -X GET "http://localhost:8080/transactions" -H "Accept: application/json"
curl -X POST "http://localhost:8080/transactions" -H "Accept: application/json" -H "Content-Type: application/json" -d '{"amount":2000,"reference":"book of the year!","receivingUser":"ardavan123"}'
```