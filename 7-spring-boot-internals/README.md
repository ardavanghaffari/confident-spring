# Spring Boot Internals

All the magic that Spring Boot does, comes from a tiny module within `spring-boot-project` source
code called `spring-boot-autoconfigure`. Within that module, under resources, you'll find a
`spring.factories` file which looks like a _.properties_ file that contain among other things,
a list of autoconfiguration classes. They all live in packages under the source. There is one
package for every third party library that Spring Boot integrates with.

- When you run the main method of your `SpringBootApplication`, Spring will run through all of its
  known (100+) AutoConfiguration classes.
- Every AutoConfiguration is just a normal Spring configuration, but with a ton of added
  `@Conditionals`. If those `@Conditionals` match, then the autoconfigurations are evaluated and
  beans are automatically created for you.
- Mainly, `@Conditionals` check if specific properties are set or if dependencies are on the
  classpath. And that the user hasn't defined `@Beans` of specific types himself, already.
- This leads to a lot of functionality working out of the box, whenever you create a new Spring Boot
  project.

See the full video explanation [here](https://www.youtube.com/watch?v=-qO1Mm8DNOc&t=1102s).

## Build your own Autoconfiguration

In practical terms, this makes sense if you are working inside a company and want to create a
default library that all your company projects are supposed to include. If you were just working on
your own project, it wouldn't make too much sense. Spring Boot comes with plenty of
DataSourceAutoConfigurations, but there's an AutoConfiguration missing for a relatively new database
connection pool, called [vibur-dbcp](https://www.vibur.org/). We're going to build an
autoconfiguration for that database in this module.

- Import Spring Boot's BOM so we don't have to manually specify versions.
  See [the build file](example-spring-boot-starter/build.gradle.kts) for how
  it's done in Gradle. Equivalent to Maven's import:
  ```xml
  <dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>3.5.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
  </dependencyManagement>
  ```
- The spring-boot-autoconfigure dependency is needed and pulls in any relevant Spring Boot
  dependencies to write your own `@AutoConfigurations`. You can leave out the version, as you are
  importing the spring-boot-dependencies project.
- vibur-dbcp is the dependency to the vibur connection pool. Spring's dependency management doesn't
  know it, hence you need to specify its version here. When using Maven, you put the scope
  to <optional> because you want to let the end-user decide if he wants to use Vibur or not. Think:
  classpath conditions. Using `<optional>true</optional>` in Maven means I need this dependency to
  compile or test myself, but projects that depend on me should not automatically inherit it. One
  would, for example, make lombok optional so that if someone depends on your code, Lombok won't be
  pulled into their project transitively. Gradle models this concept using configurations (like api,
  implementation, compileOnly, etc.). Use `compileOnly` If your dependency is only needed to compile
  your code but not needed at runtime. It would be on your compile classpath. Won't be part of the
  runtime classpath and wouldn't be transitively inherited by downstream projects.
- We want to configure the `ViburDataSource` using the properties in `application.properties`, like
  `vibure.datasource.url`, `vibure.datasource.username`, etc. We'd need a backing class containing
  a `@ConfigurationProperties(prefix = "vibure.datasource")`. This will make sure properties get
  automatically mapped from the `application.properties` file.
- The AutoConfiguration class contains the following annotations:
  ```java
  @AutoConfiguration
  @ConditionalOnClass(ViburDataSource.class) // Proceed only if ViburDataSource dependency is on the classpath.
  @ConditionalOnMissingBean(DataSource.class) // Only if the user hasn't explicitly defined a DataSource bean himself.
  @AutoConfigureBefore(DataSourceAutoConfiguration.class) // Make sure this configuration gets processed before Spring Boot creates its own Hikari Datasource on startup.
  @EnableConfigurationProperties(ExampleDataSourceProperties.class) // Enable the configuration property mapping onto our own class.
  ```
- Simply putting an autoconfiguration class into a library is not enough for Spring Boot projects to
  know about your AutoConfiguration. Spring Boot searches for a `META-INF/spring/` file, called
  `org.springframework.boot.autoconfigure.AutoConfiguration.imports` inside third-party libraries,
  containing references to autoconfigurations.
- Include the starter in the MyFancyPdfInvoices Spring Boot application as a dependency. To see if
  your autoconfiguration is working as expected, you can refactor your Spring application to print
  out the DataSource's type on application startup.
  See [the application file](my-fancy-pdf-invoices-spring-boot-vibur-dbcp/src/main/java/io/github/ardavanghaffari/myfancypdfinvoices/MyFancyPdfInvoicesApplication.java).
- Starting the application, we see `HikariDataSource (HikariPool-1)` as the DataSource type. That is
  because we haven't added the `vibur-dbcp` dependency to the project just yet and Spring Boot is
  autoconfiguring a default `HikariCP` datasource.
- Add `vibur-dbcp` and adjust `application.properties` to match the new prefix. Restart the
  application and see that `ViburDBCPDataSource` is now being used. Our AutoConfiguration got
  executed because Spring found the
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` file, and all
  the `@Conditionals` we put on our configuration matched. Hence, our @Bean method creating a new
  datasource got executed, preventing Spring Boot's default datasource to be configured.

If you want to see a list of all auto-configurations that Spring Boot applied when starting up your
project, you can start your application with the --debug or -Ddebug flag. It prints out a lot of raw
information, but is of help when debugging your own auto-configurations.

## Commands used in this module

```bash
./gradlew :example-spring-boot-starter:clean :example-spring-boot-starter:build
./gradlew :my-fancy-pdf-invoices-spring-boot-vibur-dbcp:clean :my-fancy-pdf-invoices-spring-boot-vibur-dbcp:build
java -Dspring.profiles.active=dev -jar 7-spring-boot-internals/my-fancy-pdf-invoices-spring-boot-vibur-dbcp/build/libs/my-fancy-pdf-invoices-spring-boot-vibur-dbcp-1.0.jar
```