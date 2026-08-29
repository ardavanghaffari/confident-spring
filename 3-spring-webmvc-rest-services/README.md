# Spring WebMVC Rest Services

Part of core _Spring Framework_ (and not Spring Boot). It is Spring's very own web framework. So far
we've just used Spring's _ApplicationContext_ for dependency injection. In the Servlet, we're doing
a fair amount of work i.e. parsing request URIs, manually handling JSON conversions, etc. Spring
MVC could take all that out of our hands. To add Spring WebMVC to a plain Spring project, we just
need one additional dependency `spring-webmvc`.

Spring MVC offers an abstraction on top of HttpServlets, called controllers.

## REST Controller

The controller class is a normal Java class, that does not need to implement any specific
interfaces or extend from another class. We do however need to mark it with the `@Controller`
annotation, so that Spring's `@ComponentScan` can find it and make Spring MVC understand: This
class can accept HTTP requests.

- Method names in the controller don't matter. What matters is using the right annotations.
- `@GetMapping("/")` will make sure that the method is called by Spring MVC in response to HTTP GET
  requests, if, and only if, the request URI is equal to `/`. This is similar to what the `doGet`
  method from `PdfInvoicesServlet` does.
- `@ResponseBody` makes sure to directly write, whatever your method returns, to the
  `HttpServletOutputStream`. Again, exactly what we did manually in the servlet.
- Replacing our own servlet with Spring MVC's own servlet, called `DispatcherServlet`: we don't
  need to create it. It comes bundled with Spring MVC. We just need to register it with Tomcat.
  What it does, is take the incoming HTTP request and forward it to the controller and then
  write whatever the controller returns to the `HttpServletResponse`. This is why it's called
  `DispatcherServlet`, because its job is literally to dispatch requests and responses to and from
  `@Controllers`.
  See [ApplicationLauncher](./my-fancy-pdf-invoices-spring-webmvc/src/main/java/io/github/ardavanghaffari/myfancypdfinvoices/ApplicationLauncher.java)
  for how it's done. In summary:
    - Because of `@ComponentScanning`, the application context we create will know about the
      `@Controller` and `@GetMapping`.
    - The `DispatcherServlet` knows about the application context because we pass it into its
      constructor.
    - And because of that, the `DispatcherServlet` is able to forward incoming requests to the
      controllers and back to the browser.
- `@RestController` is a meta-annotation, bundling multiple annotations including `@Controller` and
  `@ResponseBody`. `@Controller` is a marker annotation in Spring that signals Spring that your
  class contains methods that can return something HTTP related (HTML, JSON, XML) to the end-user
  or a browser. `@ResponseBody` tells Spring, that you want to write JSON or XML or plain text
  directly to the `HttpServletOutputstream`, but without going through an HTML templating framework,
  which Spring would assume by default if you don't annotate your method with `@ResponseBody`.
- Add `@EnableWebMvc` to the configuration, otherwise Spring wouldn't, for example, know what to do
  with the list of Invoice objects before sending it back to the browser/end-user, i.e. converting
  it to JSON. We will otherwise get an error like
  `The target resource does not have a current representation that would be acceptable to the user`.
  This annotation makes sure that Spring WebMVC gets initialized with a sane default configuration.
  Among other things, it automatically registers a JSON converter with Spring MVC, as long as you
  have the jackson dependency on your classpath. If that is the case, Spring WebMVC automatically
  enables JSON <-> Java object conversions for you, without you having to do anything! And Spring
  will assume that you want JSON returned, by default. Spring Boot does the same converter
  registration that the annotation does, _in the background_.
- `@RequestParam` specifies _mandatory_ request parameters and optionally gives them a name, in case
  the request parameter name differs from the method parameter in the Java code (user_id vs userId).
  Note that by default, the Java compiler does not include method parameter names in the `.class`
  files. It just stores them as arg0, arg1, etc. Spring on the other hand, relies on these names to
  infer the `@RequestParam` name if we don't explicitly specify a value. So, we have to options.
  Either specify a value explicitly in the annotation or compile the code with the `-parameters`
  option. That way, the method parameter names from the Java sourcecode will end up in the bytecode
  and Spring would be able to just infer the names. See [Gradle's build file](../build.gradle.kts)
  for how we've enabled that option.
- Spring will already do basic conversions for you, i.e. try to convert the amount to an Integer
  and complain if someone sends in a random String instead. This is not bullet-proof validation, but
  a start.
- Instead of being appended to the request URL with `?key=value`, or in the request body,
  `@PathVariables` are part of the URL. The annotation behaves just like `@RequestParam`,
  only that the parameter names must match the `{placeholders}` in the url.
- [This article](https://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api) covers
  RESTFul API design in detail including @PathVariables vs @RequestParams.
- In order to send JSON as part of POST request body, we need DTO objects to model the data between
  the frontend and the backend. The DTO objects can be used as method parameters annotated with
  `@RequestBody`. Spring will automatically do the conversion along with basic validation. See
  `InvoicesController` for an example.
  ```text
  POST http://localhost:8080/invoices
  Content-Type: application/json
  ####
  {
    "amount": 5000,
    "user_id": "helene"
  }
  ```

## Serving both XML and JSON from REST controller

Suppose our REST controller will also be used by another client that expected invoices in XML
instead of JSON. How would we additionally achieve XML conversion? And how would the client let
the server know which format it wants? Will that have impact on the code we write in the controller?

Earlier, we added the `jackson-databind` dependency for Java <-> JSON conversion. Having that
dependency on the classpath along with enabling the `@EnableWebMvc` annotation results in Spring
automatically creating a JSON converter. Adding the `jackson-dataformat-xml` dependency will have
the same result for Java <-> XML conversion. Our application now has two converters. The XML
converter has a higher default priority. That is why the application will return an XML response
to a request like `curl -X GET "http://localhost:8080/invoices" -H "Accept: */*"`. The Accept header
here is literally saying, _I accept anything, you decide_. As long as the client, specifies how it
wants to communicate with the server via the request headers, we don't have to change any of the
code in the controller. This is called _content negotiation_ and Spring supports it.

Spring understands what data format you send in and what you expect as a result, by looking at HTTP
headers. When sending in data to your Spring application, you need to specify the correct
_Content-Type_ header, to let Spring know it needs to convert from e.g. JSON to Java. (And Spring
needs to have a converter registered, for that content-type). When getting data back from your
Spring application, you need to have specified the correct _Accept_ header, to let Spring know it
needs to convert from Java to e.g. JSON. (Again, Spring needs to have a converter registered, for
that content-type). This means you can also do things like sending in a request with
`Content-Type=application/xml` and `Accept=application/json`, i.e., telling Spring you are sending
in XML, but want JSON back.

## Validating User Input

- Annotations like `@RequestParam` have rudimentary abilities to convert and validate incoming user
  input. For proper validation, we'll need additional libraries. There are many. We'll use
  `hibernate-validator`.
- The library provides annotations such as `@Min`, `@Max`, `@NotBlank` which we can use on our DTO
  fields. We should also tell Spring to actually perform the validation by adding the `@Valid`
  annotation to the method parameter in the controller. Upon sending invalid data, we'll get a
  Http 400 Bad Request from the server.
- Custom validation annotations can be made. Here is an overview of the ones provided by the
  framework:
    - @AssertFalse / @AssertTrue makes sure that a boolean field is set to false / true.
    - @DecimalMin / @DecimalMax makes sure that a number (BigDecimal, BigInteger, CharSequence,
      byte, short, int, long, etc.) is >= or <= a value. It's equivalent to @Min, @Max.
    - @Digits
    - @Email: the string needs to be a well-formed e-mail address.
    - @Future / @FutureOrPresent: a date (pre-Java 8 types and Java8+ types) needs to be in the
      future or present.
    - @Negative / @NegativeOrZero
    - @NotBlank / @NotEmpty: a string must not be blank or empty.
    - @Null / @NotNull
    - @Past / @PastOrPresent: a date (pre-Java 8 types and Java8+ types) needs to be in the past or
      present.
    - @Pattern: a string needs to match a regex pattern.
    - @Positive / @PositiveOrZero
    - @Size: the element size must be between a boundary. Valid for strings, collections, maps,
      arrays.
- In order to validate `@RequestParam` method arguments, we need:
    - `hibernate-validator` on the classpath.
    - Register `MethodValidationPostProcessor` as a bean.
    - Annotate the controller with `@Validated`.
- When validating `@RequestParams`, Spring MVC uses `hibernate-validator` via the
  `MethodValidationPostProcessor` and if validation fails, a `ConstraintViolationException` is
  thrown. By default, Spring doesn't map this exception to a 400 error code. It just bubbles up
  the stack, and the container returns a 500 internal server error. Spring Boot adds an
  `@ExceptionHandler` automatically (via `DefaultHandlerExceptionResolver`), which translates it
  into a 400 bad request. Spring MVC doesn't have that mapping out of the box. We need to instead
  add our own `@ControllerAdvice` and catch those exceptions:
  ```java
  @ControllerAdvice
  public class GlobalExceptionHandler {

      @ExceptionHandler(ConstraintViolationException.class)
      public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
          String errors = ex.getConstraintViolations().stream()
                  .map(v -> v.getPropertyPath() + " " + v.getMessage())
                  .collect(Collectors.joining(", "));

          return ResponseEntity
                  .status(HttpStatus.BAD_REQUEST) // 400 instead of 500
                  .body("Validation failed: " + errors);
      }
  }
  ```
- When using Spring Boot, we don't have to specify the `MethodValidationPostProcessor` bean. We
  don't even have to explicitly add `hibernate-validator` as a dependency to get validation working.
  Spring Boot does all of that _for you_. It simply registers the bean and manages the dependencies
  under the hood.

## Handling Exceptions

So far, in error scenarios, we've been getting HTML pages back from the server containing 400 or 500
status codes. Returning a JSON object or an XML object instead is more common. This is something
that Spring WebMVC cannot do completely by itself, because it does not know exactly what kind of
JSON/XML to return. Hence, we'll need to program an exception handler ourselves. Spring Boot, on the
other hand, offers a sane default error object out of the box.

See `GlobalExceptionHandler` for an example. The `@RestControllerAdvice` annotation will
make sure that Spring will apply whatever this class contains to all of `@Controllers` or
`@RestControllers` it knows. There is also a `@ControllerAdvice` annotation. The difference between
these two is not that they apply to only `@Controller` or only `@RestControllers`. The difference
is, that `@ControllerAdvice` will write HTML to the user, and `@RestControllerAdvice` will write
JSON/XML, or whatever you want, directly to the `@ResponseBody`.

With `@ExceptionHandler`, we specify which error we want to handle and `@ResponseStatus` sets the
appropriate status code in the response. Instead of String, we can return our own custom error
object from the method. Spring throws `MethodArgumentNotValidException` when method arguments
annotated with `@Valid` fail the validation. It throws `ConstraintViolationException` for
`@RequestParams` that fail the validation.

These controller advices could act as a central, last _catch-all_ barrier before a response gets
sent back to the client. They are not only about Spring's own exceptions but can also handle our
own domain exceptions.

## Thoughts On Spring

- Webflux and Reactive Spring but make sure to learn the traditional servlet stack first.
- When developing front-end what matters is a fast feedback loop - you need to be able to see the
  changes you're making in order to proceed further. With traditional server-side Java applications,
  it has always been a pain. Yes, Spring Boot has support for Thymeleaf live-reload, but everything
  else - CSS pre/post processors, frontend dependencies via WebJars - feels like gluing things
  together and hoping it's going to work. Modern front-end frameworks' programming models let
  developers build reusable components and avoid CSS clashes thanks to CSS-in-JS or CSS Modules.
  I believe that client-side framework used properly can significantly speed up the development.
  Thymeleaf has not been actively maintained since 2018. Wro4j which aimed to address some of these
  issues, is also pretty much dead. If Spring offered first-class support for building server-side
  rendered full-stack applications, I would be very happy to use it. Today, unfortunately, it's just
  pragmatic to use one of the client-side frameworks or Vaadin.
- Spring Data JDBC is simple, opinionated. There is not much magic. does not try to abstract too
  much, but on the downside, it's also limited in functionality. Use Spring Data JPA when running
  into the limitations of Spring Data JDBC.

## Commands used in this module

```bash
./gradlew :my-fancy-pdf-invoices-spring-webmvc:clean :my-fancy-pdf-invoices-spring-webmvc:build
java -Dspring.profiles.active=dev -jar 3-spring-webmvc-rest-services/my-fancy-pdf-invoices-spring-webmvc/build/libs/my-fancy-pdf-invoices-spring-webmvc-1.0-all.jar
curl -X GET "http://localhost:8080/invoices" -H "Accept: application/xml"
curl -X GET "http://localhost:8080/invoices" -H "Accept: application/json"
curl -X POST "http://localhost:8080/invoices?user_id=ardavan&amount=40" -H "Accept: application/json"
curl -X POST "http://localhost:8080/invoices" -H "Accept: application/xml" -H "Content-Type: application/json" -d '{"amount":"2000","user_id":"ari"}'

./gradlew :mybank-spring-webmvc:clean :mybank-spring-webmvc:build
java -Dserver.port=8090 -jar 3-spring-webmvc-rest-services/mybank-spring-webmvc/build/libs/mybank-spring-webmvc-1.0-all.jar
curl -X GET "http://localhost:8090/transactions" -H "Accept: application/json"
curl -X GET "http://localhost:8090/transactions" -H "Accept: application/xml"
curl -X POST "http://localhost:8090/transactions" -H "Accept: application/json" -H "Content-Type: application/json" -d '{"amount":2000,"reference":"book of the year!"}'
curl -X POST "http://localhost:8090/transactions" -H "Accept: application/xml" -H "Content-Type: application/json" -d '{"amount":2000,"reference":"ari"}'
curl -X POST "http://localhost:8090/transactions" -H "Accept: application/xml" -H "Content-Type: application/xml" -d '<request><amount>2000</amount><reference>book of the year!</reference></request>'
curl -X POST "http://localhost:8090/transactions" -H "Accept: application/json" -H "Content-Type: application/xml" -d '<request><amount>2000</amount><reference>book of the year!</reference></request>'
```