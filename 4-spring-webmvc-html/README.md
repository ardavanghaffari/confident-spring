# Spring WebMVC HTML Pages

In this module, we're going to learn:

- How to render HTML with Spring WebMVC.
- How to work with a templating library called Thymeleaf.
- How to work with form submissions.

As with converting between Java and XML or JSON, Spring cannot write HTML pages itself. It relies
heavily on third-party libraries, more specifically HTML templating libraries, to do the heavy
lifting. The Spring documentation itself uses Thymeleaf.

## HTML @Controller

Every public method in a controller annotated with `@RestController` will (more or less) write its
result directly to the Http response body, _without_ going through an HTML templating library. So
a method like this in a Rest Controller:

```java

@GetMapping("/")
public String homepage() {
    return "index.html";
}
```

Won't try to render a template named `index.html` and return its HTML to the browser. It will
instead return the literal string "index.html". If we want to serve HTML instead of JSON/XML, we
need to use a separate class with the `@Controller` annotation. `@Controllers` and
`@RestControllers` can just co-exist. One will handle HTML pages and the other JSON/XML responses.

For Spring MVC and Thymeleaf to work together, we need to register a couple of beans.
See [the configuration](./my-fancy-pdf-invoices-spring-webmvc-html/src/main/java/io/github/ardavanghaffari/myfancypdfinvoices/context/ApplicationConfiguration.java).

Whenever a controller returns a template as in `return index.html`, Spring will ask all the view
resolvers it knows about to find and render a template named "index.html". We'd therefore need a
`ThymeleafViewResolver` bean. It's possible to use multiple templating libraries in a project. For
example, make Thymeleaf render only `.html`, Freemarker `.ftl` and Velocity `.vm` templates. That's
why ViewResolvers can be ordered and each can be configured with specific view names.

The ViewResolver needs a `SpringTemplateEngine` which in turn needs a `TemplateResolver`.
TemplateResolver is the one actually responsible for finding the template. There are several ways
to tell the TemplateResolver where to look for the templates. One way is to prefix every template
name with a Spring resources classifier. Here we're saying that all templates are under the
`/templates/` directory on the class path (i.e. src/main/resources/ during development, later on in
the .jar file). Furthermore, we're not caching the templates which makes sense for development but
not for production.

When using Spring Boot, we don't have to create these beans manually. They're automatically created.

## Variables in Templates

- Thymeleaf templates are valid HTML files.
- The templates become dynamic through additional _HTML attributes_.
- Variables are passed from the controller to the template through a _Model_. The Model parameter
  is essentially a map, containing all the variables that you want to be able to access in your
  templates. Spring is smart enough to automatically inject a Model into every @Controller method,
  if you specify it as a parameter.
- Custom `th:` attributes and variable references `${}` make the template more dynamic. `th:text`,
  for example, replaces the tag's contents with whatever you specify. `th:remove="tag"` removes
  the tag itself from the HTML output. Normal Java functions `${username.startsWith('z')}` and
  Thymeleaf's helper functions `${#temporals.format(currentDate, 'dd-MM-yyyy HH:mm')}` are also
  available throughout the `${}` notations.
  See [the index.html template](./my-fancy-pdf-invoices-spring-webmvc-html/src/main/resources/templates/index.html).

## From Submissions

GET:

- `th:action="@{/login}"` is the action of the form, i.e. the url the form is submitted against.
  Must match the corresponding @Controller method.
- `th:field="*{username}"` and `th:field="*{password}"` specify the form fields. Note the special
  syntax `*{}`. If you are referencing a field that does not exist, you will get an exception.
- `th:object="${loginForm}"` specifies the _backing bean_. The java object to which the form fields
  will be mapped. Form fields are mapped to properties of the _backing bean_.
- To pre-fill the username field, we can set a value in the _backing bean_ in the @GetMapping, and
  it will be displayed when opening the HTML page.

POST:

- `@ModelAttribute LoginForm loginForm` in the PostMapping ensures that the form fields are mapped
  to loginForm which is the _backing bean_. It also makes sure that loginForm is put in the model
  and is therefore passed to the HTML template. It is pretty much equivalent to
  `model.put("loginForm", loginForm)`.
- `return "redirect:/"` doesn't refer to a template view but Spring rather will issue a HTTP 302
  redirect to "/".

Form Validation:

- Add appropriate validations such as `@NotBlank`, `@Size`, etc. to the _backing bean_.
- Add `@Valid` to the Controller method (or `@Validated` on the Controller itself for validating
  `@RequestParams`).
- The problem with this as you might remember from the Rest Services module earlier is that invalid
  input will result in an exception which would be caught by the `GlobalExceptionHandler`. This is
  not what we want in this case. We'd instead want to re-render the login form and display the
  validation errors. That's why we have to use the `BindingResult` parameter. It is basically a
  container for all validation errors.
- The `BindingResult` must immediately follow the `@Valid` argument.
- On validation errors, Spring does not throw an exception — instead it populates the BindingResult.
- You can access the binding result through special Thymeleaf syntax, with
  `#fields.hasErrors(yourFieldName)`. If the field has errors, you again are using special
  Thymeleaf syntax to display the error message that is bound to a field of your backing bean.
  See [login](./my-fancy-pdf-invoices-spring-webmvc-html/src/main/resources/templates/login.html).

## Commands used in this module

```bash
./gradlew :my-fancy-pdf-invoices-spring-webmvc-html:clean :my-fancy-pdf-invoices-spring-webmvc-html:build
java -Dspring.profiles.active=dev -jar 4-spring-webmvc-html/my-fancy-pdf-invoices-spring-webmvc-html/build/libs/my-fancy-pdf-invoices-spring-webmvc-html-1.0-all.jar
curl -X GET "http://localhost:8080/?username=ardavan"
curl -X GET "http://localhost:8080/?username="
curl -X GET "http://localhost:8080/?username=zara"

./gradlew :mybank-spring-webmvc-html:clean :mybank-spring-webmvc-html:build
java -jar 4-spring-webmvc-html/mybank-spring-webmvc-html/build/libs/mybank-spring-webmvc-html-1.0-all.jar
curl -X POST "http://localhost:8080/transactions" -H "Accept: application/json" -H "Content-Type: application/json" -d '{"amount":2000,"reference":"book of the year!","receivingUser":"ardavan123"}'
```