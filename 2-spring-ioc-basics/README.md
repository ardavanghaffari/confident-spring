# Spring IOC Basics

This module is about _Spring core_ and concepts like @Beans, different styles of dependency
injection, bean lifecycles and even Spring's environment support. We're going to _Springify_
the projects from the previous module. We will learn among other things how to add Spring framework
dependencies (**not Spring Boot**) to our project.

You only need one Maven dependency to add Spring framework to your project: the `spring-context`
dependency. This results in the most minimalistic Spring setup you can have.

## @Bean and @Scope

- By default, all `@Bean` methods produce singletons.
- If you are using singletons, you need to make sure that all your singletons are thread-safe, or
  else you will run into race-conditions.
- If you don't want to deal with that, you can also change Spring's default singleton behavior, with
  the `@Scope` annotation.
- What is the difference between singleton and prototype beans? Instead of creating just one
  instance of your bean, Spring will create and return you a completely new instance every time
  you call ctx.getBean(). Furthermore, prototypes always need to be constructed anew, whereas Spring
  only has to construct singletons once. So, there is a speed difference in usage.
- The issue with prototypes: you will always get the same prototype bean back when accessed through
  a singleton bean. Imagine having a singleton bean that depends on a prototype bean. When asking
  for the prototype bean from the application context, you'll always get a different instance, but
  when asked through the singleton, you'll always end up getting the same instance back. That's
  because a singleton is always created once at the start along with all its dependencies. There are
  workarounds for this, mainly _Scoped Proxies_ and _Method Injection_. See
  [this article](https://www.baeldung.com/spring-inject-prototype-bean-into-singleton).
- Additional Scopes: only valid for web-aware ApplicationContexts.
    - request: an instance of your object per http request.
    - session: an instance of your object per http session.
    - application: an instance of your object per servlet context.
    - websocket: an instance of your object per web socket.

## @Component and @ComponentScan

Instead of defining beans with `@Bean` factory methods inside a `@Configuration` class, we can also
use annotations to let Spring discover and manage them automatically.

- Annotating a class with `@Component` is essentially a replacement for declaring it with a `@Bean`
  method. Spring will detect the class and register it as a bean. This approach only works for
  classes we control. For third-party classes, such as ObjectMapper from Jackson, we cannot
  modify the source code to add annotations. In those cases, we still need to declare them using
  `@Bean` factory methods.
- Spring needs to scan the classpath to find the `@Component` annotated classes which it doesn't do
  by default. We enable that behavior by adding the `@ComponentScan` annotation to the Configuration
  class. `@ComponentScan`, by default (when used without value), only scans the package and all the
  sub-packages of its annotated class (ApplicationConfiguration in our example). This will likely
  result in _No qualifying bean of type_ kind of errors as `@Component` annotated classes usually
  reside in other packages. We should instead specify the root package. We can do that using
  `@ComponentScan(basePackageClasses = ApplicationLauncher.class)`. ApplicationLauncher lives inside
  the root package, and specifying it as base class will tell Spring to scan that package and all
  the sub-packages the class lives in. We could have also specified the package itself as String,
  via the `basePackages` attribute on the annotation, but that wouldn't have been type-safe.
- If you are used to working with Spring Boot, you might have never used `@ComponentScan` directly.
  The `@SpringBootApplication` annotation, which every Spring Boot application needs, is a meta
  annotation which includes the `@ComponentScan` annotation. What is important to understand is that
  `@SpringBootApplication` is (also) a `@ComponentScan`.

## @Autowired, Constructor, Field and Setter Injection

- `@Autowired` becomes required when the class has multiple constructors. In that case, you need to
  annotate the desired constructor with `@Autowired`. It's otherwise optional.
- One of the drawbacks of field injection is that it basically hides what dependencies your class
  needs, and you cannot easily instantiate your class outside of a Spring context anymore.
- Use constructor injection for mandatory dependencies of your class.
- Use field injection/setter injection for optional dependencies and safeguard their access with
  if-null checks.
- Note that worrying about optional dependencies is primarily essential when using Spring classes
  outside a Spring context, as `@Autowired` will always make sure to inject the dependency, be it
  through constructors, fields or setters - or throw an exception if it fails to do so.

## @Bean Lifecycles

Spring offers `@PostConstruct` and `@PreDestroy` among other bean lifecycle callbacks. `@PreDestory`
has caveats. It only gets executed when the application context is _gracefully_ shutdown. This
happens when the application is asked to stop in an orderly way:

- Sending SIGTERM (default when you do kill <pid> in Linux, without -9).
- CTRL+C in console (Spring Boot registers a shutdown hook).
- Calling SpringApplication.exit(context, …) in code.

In this case:

- Spring's shutdown hook runs.
- ApplicationContext is closed.
- All beans with lifecycle callbacks have their destroy methods called.
- Embedded servers (Tomcat, Jetty, Netty) stop gracefully — finishing in-flight requests before
  shutting down.
- Thread pools (like @Async executors or schedulers) are shut down properly.

Spring doesn't run the shutdown hooks when the application is terminated:

- kill -9 on Linux
- System crash
- JVM crash
- Most IDEs do not shut down your application gracefully. Instead, they terminate it.

Spring Boot ensures by default that shutdown hooks are all executed. That is only when the
ApplicationContext is gracefully shutdown and not terminated. This doesn't happen by default
if you're not using Spring Boot and are just, for example, using spring-core. In that case,
you'll have to register them for shutdown manually:

```java

@Override
public void init() throws ServletException {
    AnnotationConfigApplicationContext ctx
            = new AnnotationConfigApplicationContext(MyFancyPdfInvoicesApplicationConfiguration.class);

    ctx.registerShutdownHook();

    this.userService = ctx.getBean(UserService.class);
    this.objectMapper = ctx.getBean(ObjectMapper.class);
    this.invoiceService = ctx.getBean(InvoiceService.class);
}
```

Read more about Bean lifecycle callbacks
in [the official Spring doc](https://docs.spring.io/spring-framework/reference/core/beans/factory-nature.html).

## Resources, Properties and Profiles

- To make Spring read in properties files, use the `@PropertySources` annotation. Any
  Spring-Resources specific string can be used, like `file:/`, `classpath:/` or even `https:/`.
  See [this link](https://www.marcobehler.com/guides/spring-framework#spring-resources)
  for more information. You can also provide multiple `@PropertySources` in case you want to read in
  from multiple locations.

```java

@Configuration
@ComponentScan(basePackageClasses = ApplicationLauncher.class)
@PropertySource("classpath:/application.properties")
@PropertySource(value = "classpath:/application-${spring.profiles.active}.properties"
        , ignoreResourceNotFound = true)
public class ApplicationConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
} 
```

- Using the `@Value` annotation, we can inject any of the properties specified by `@PropertySource`.
- Properties are one part of Spring's environment, another part is `@Profiles`. You can use profiles
  if you want different properties or even completely different Spring Beans in different
  environments. Annotating a Bean with e.g. `@Profile("dev")` results in that Bean only being
  created when the application is started with that profile. For example
  `java -Dspring.profiles.active=dev -jar app.jar`.
- When using multiple `@PropertySources`, the order is important because the one's coming later
  have higher precedence and will overwrite the same properties from previous `@PropertySources`.

## Bootstrapping Spring Context

In a non-Spring Boot application (e.g. when using only `spring-context`), we have to create the
application context ourselves. There is no auto bootstrap. Spring Boot adds that magic for us
(via `SpringApplication.run(...)`), but in plain Spring we do that manually. The class with
the `main` method usually bootstraps Spring. Note that this class cannot itself be constructed via
Spring injection because when `main` executes, Spring context hasn't been initialized yet and so
there is no container yet to inject it. It's like a chicken-egg problem: Spring context doesn't
exist until `main` creates it.
See [ApplicationLauncher](./mybank-spring/src/main/java/io/github/ardavanghaffari/mybank/ApplicationLauncher.java)
for example.

## Thoughts On Spring

- Spring Data Neo4j, Neo4j-OGM, SDN-RX, including an OpenCypher DSL.
- Core Spring Dependency Injection, Spring's Testing framework, one of the web layers, either
  standard MVC or reactive.
- Would you still invest time in learning Spring based on servlets and JDBC, or go straight into
  reactive Spring? You can do imperative web applications with Spring without ever having to look
  into the fact that servlets are behind it. So, the question should be: Would you still invest
  time into imperative programming or go straight to reactive? Imperative or _blocking_ programming
  model is far from dead, there is absolutely no need to make reactive the default today. There are
  good reasons for some use cases, but reactive programming is not a silver bullet.
