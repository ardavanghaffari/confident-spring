# Spring Data JDBC

_Spring Data JDBC_ is a project in the _Spring Data_ umbrella project. Not to be confused with:

- spring-jdbc: This is the library we used in the _5-spring-database_ module to work with
  Spring's `JDBCTemplate` and `@Transactional`. A low-level library and a decades old.
  _Spring Data JDBC_ actually builds on top of this library.
- spring-data-jpa: This is a sibling project and adds a convenience layer on top of JPA/Hibernate,
  which itself again is built on top of JDBC.

Why not cover spring-data-jpa, instead? JPA/Hibernate is a huge and complex topic. No matter how
easy it might seem to get started with Spring Data JPA, you need a solid understanding of Hibernate
sooner or later, to be able to work correctly with it.

In this module, we're going to swap out the database layer we built in the previous modules. Instead
of using Spring's raw JdbcTemplate, we are going to use _Spring Data JDBC_. Replace the
`spring-boot-starter-jdbc` dependency with `spring-boot-starter-data-jdbc`. Running the dependency
tree shows that `spring-boot-starter-data-jdbc` depends on `spring-data-jdbc`, `spring-jdbc` as well
as `spring-tx`.

Spring Data JDBC's main benefit is to get rid of the low-level JdbcTemplate calls we had to write
before to get basic CRUD operations working. See `InvoiceService` from the previous modules.
In short:

- We had to construct SQL queries as strings.
- And create RowMappers or at least do some manual plumbing to map between SQL and our Invoice
  objects.

Spring Data helps get rid of some of this verbosity, by providing CrudRepository classes. See
`InvoiceRepository` for example. The class is an interface, which extends from Spring Data's Common
`CrudRepository` interface. It comes with a ton of default methods like `findById`, `findAll`,
`delete`, `save` that we can call to generate appropriate SQL statements, without having to write
any SQL. Spring Data will create runtime proxies for these interfaces, which will create and run SQL
statements for us.

Starting the application and querying the invoices results in an error that the _INVOICE_ table is
not found. That is true because our table is called _INVOICES_ and Spring Data, by default, tries to
query a table that has the same name as the class, in our case _INVOICE_.

Add `logging.level.org.springframework.jdbc.core.JdbcTemplate  = DEBUG` to `application.properties`
to enable SQL logging in the console, so we can see the generated SQL statements by Spring Data (not
only when we receive errors).

Add the following annotations to the `Invoice` class:

```java
// This is the Spring Data JDBC specific @Table annotation, that has nothing to do with JPA's or 
// Hibernate's @Table annotation. It lets your repository know that the corresponding table ends 
// with s, and it needs to generate corresponding SQL statements.
@Table("invoices")
public class Invoice {
}

// @Id tells Spring Data JDBC what the primary key of the class is so it can automatically set the
// generated id on `Invoice` objects.
@Id
private String id;
```

Restarting the application and trying another GET results in another error. This time an H2 specific
error: `org.h2.jdbc.JdbcSQLSyntaxErrorException: Table "invoices" not found; ...`

H2 converts unquoted SQL identifiers to upper case, by default. I.e., when you create a new table
called "invoices", it will actually create a table called "INVOICES" and also expect you to call
that case-sensitive table in your SQL queries.

In your @Table("invoices") annotation, you are specifying everything as lower-case, though. So, you
have a couple of choices now:

- When creating the table in your schema.sql, "quote" the table name to disable upper-case
  conversion.
- Change the H2 connection string (;DATABASE_TO_UPPER=false), where you can disable the upper-case
  conversion globally.
- Change the @Table annotation to read INVOICES.

We'll go with the first option. Quote the table name in schema.sql.

## Custom SQL Queries

The `CrudRepository` interface already comes with pre-defined methods to manipulate objects. To
write custom SQL queries, use the `@Query` annotation. Note that here too, we've put the table name
in quotes to disable H2's default upper-case conversion:

```java

@Query("SELECT id, pdf_url, user_id, amount FROM \"invoices\" where user_id = :userId")
Iterable<Invoice> findByUserId(@Param("userId") String userId);
```

Spring Data JDBC supports automatic queries-by-method names like Spring Data JPA. So, having a
method findUserById without the @Query annotation will work as well, as long as you get
the [naming of your methods](https://docs.spring.io/spring-data/relational/reference/jdbc/query-methods.html)
right!

## How does it work?

Spring Data JDBC consists of a ton of layers, but in the end it uses the JDBCTemplate under the
hood, just like you did previously.

Spring Data JDBC is smart enough to pick up your custom repositories (the ones that implement the
CrudRepository interface) and provide a default implementation of that interface, depending on the
generic types you passed in, i.e. an invoice with a string id column. As already mentioned, this is
done by runtime proxy generation.

That default implementation of these proxies does nothing more in the end, than instantiate and call
a JDBCTemplate with a pre-configured RowMapper (check out the DefaultDataAccessStrategy class for
more info). The RowMapper works with a NamingStrategy, which can convert between Java property names
and SQL columns, or fall back to specific annotations that you provided.

Even though they all run under the Spring Data banner, spring-data-jdbc, spring-data-jpa,
spring-data-ldap and others are all different from each other, because the underlying data storage
technologies are different. They also support slightly different database features.

In the end, however, they all try to allow you easy data access by providing repositories on top of
the underlying technology. Be that the JdbcTemplate, Hibernate or Active Directories.

This means, you’ll need to learn the underlying technology before blindly jumping right into the
corresponding Spring Data project.

## Commands used in this module

```bash
./gradlew :my-fancy-pdf-invoices-spring-data-jdbc:dependencies
./gradlew :my-fancy-pdf-invoices-spring-data-jdbc:clean :my-fancy-pdf-invoices-spring-data-jdbc:build
java -Dspring.profiles.active=dev -jar 8-spring-data-jdbc/my-fancy-pdf-invoices-spring-data-jdbc/build/libs/my-fancy-pdf-invoices-spring-data-jdbc-1.0.jar
curl -X GET "http://localhost:8080/invoices"
curl -X GET "http://localhost:8080/invoices/user/someUserId"
curl -X POST "http://localhost:8080/invoices" -H "Content-Type: application/json" -d '{"amount":"20","user_id":"ari"}'
curl -X POST "http://localhost:8080/invoices" -H "Accept: application/xml" -H "Content-Type: application/json" -d '{"amount":"20","user_id":"ari"}'
```