# Spring Database

This module is about how you connect to databases with _plain Spring_ and a library called
`spring-jdbc`. Do not confuse this with _Spring Data JDBC_ or _Spring Data JPA_. All utilities
covered in this module are available in core Spring framework and are about a decade old.
They form the basis for every other, more advanced Spring Data project later on.

To connect to a database with Java and Spring, we need two things:

- A database, e.g. MySQL, PostgreSQL, SQL Server, Oracle or any other database.
- A matching JDBC driver for that database.

We're going to use an in-memory [H2 database](https://www.h2database.com/html/main.html). No
installation is needed. It is available as a dependency along with the necessary JDBC driver.
This is why it is used, by default, as a testing database in Spring Boot. Note that Spring boot
(or e.g. spring-boot-starter-test) doesn't include it as a dependency. It just detects if it's
on the classpath (or any other embedded database driver) and automatically configures it as
an in-memory database if there is no existing DataSource bean defined. So we have to explicitly
specify it as a dependency.

For an overview of all popular database libraries & APIs in Java, refer
to [this article](https://www.marcobehler.com/guides/java-databases).

## Creating A DataSource

In Java, you connect to databases by configuring a DataSource object with the help of a JDBC driver.
In real-life projects, you are likely going to use connection pool libraries on top of your
database, which come with their own DataSource classes. You can read more on that
in [this article](https://www.marcobehler.com/guides/jdbc).

We're setting the H2 database url in the configuration to `jdbc:h2:~/myFirstH2Database`. This is H2
specific, and it means: Open (and create if it does not yet exist) a database in a file called
`myfirsth2database.mv.db` in user home.

Using `sa/sa` for username/password is somewhat of a convention when it comes to H2.

Add `INIT=RUNSCRIPT FROM 'classpath:schema.sql'` to the DataSource URL in the configuration so that
H2 executes the schema.sql file upon connecting to the database. schema.sql files are also used in
Spring Boot apps, but there we don't have to specify an _init_ script in the JDBC URL. Reason being
that Spring Boot offers its own script loading feature, that works with any database, not just H2.

## JDBCTemplate

In Spring, the low-level way to write and execute SQL queries, is to use a JDBCTemplate in
combination with your DataSource. It is a tiny wrapper class around Java's plain JDBC facilities and
allows you to conveniently execute SQL statements. It is a thread-safe class, and can thus be used
by many multiple different threads at the same time to execute SQL against your database.

JDBCTemplate's `query` method takes an SQL query and a RowMapper. The row mapper lets you map every
returned SQL row into a Java object.

## @Transactional

See [this article](https://www.marcobehler.com/guides/spring-transaction-management-transactional-in-depth)
for an in-depth look into how Spring's transaction management works. Spring Boot enables it
automatically for you, but in a non-boot setting you'd need to do two things:

- Annotate your Spring configuration with the `@EnableTransactionManagement` annotation. It allows
  you to use the `@Transactional` annotation, to declare transactions programmatically.
- You also need to configure a `TransactionManager` bean. The TransactionManager bean is responsible
  for actually opening up and committing transactions on database connections. Spring offers
  multiple TransactionManager implementations, because transaction handling is slightly different,
  whenever you are using a DataSource (like in our case), or another library like Hibernate or jOOQ.

Using transactions isn't needed if you only have 1-line SQL statements. That's a bit misleading.
Let's dive a bit deeper to see what that means.

A transaction groups one or more SQL statements into a single unit of work. Either all succeed, or
all fail (atomicity). Transactions also ensure isolation (concurrent sessions don't see
half-finished results).

If you execute a single INSERT, UPDATE, or DELETE:

- Most relational databases (Postgres, MySQL, Oracle, SQL Server, etc.) automatically treat each
  statement as its own transaction if you don't explicitly start one.
- If the statement succeeds → the DB commits it automatically.
- If the statement fails → the DB rolls it back automatically.

This is often called autocommit mode, and it's on by default in many drivers and clients.

So, in this narrow sense, you don't need to wrap a single statement in an explicit BEGIN/COMMIT,
because the database is already doing that for you.

Even with _1-line statements_, transactions can still matter:

- Read consistency: If you SELECT data and then act on it, you might want a transaction so the read
  view stays consistent until you finish.
- Multiple dependent statements: As soon as you do more than one statement that should succeed or
  fail together (e.g., insert into two tables), you need a transaction.
- Performance: Autocommit means each single statement has the overhead of starting and committing a
  transaction. In bulk inserts/updates, wrapping them in an explicit transaction can be much faster.
- Concurrency & locking: A transaction lets you hold locks across multiple statements, controlling
  how concurrent users interact.

So the saying _transactions aren't needed for single-line SQL_ is true only in the trivial sense
that the DB is already wrapping it in an implicit transaction. But it's incomplete, because whether
you _need_ explicit transactions, depends on the semantics of what you're trying to achieve.

## Spring Data

The `spring-jdbc` library is one of the oldest available core Spring framework modules. It is very
lightweight and is just a tiny wrapper around Java's plain JDBC. We saw that Mapping between
ResultSets and Java objects is a fair amount of plumbing work. How does Spring Data fit into this
picture? Spring Data is an umbrella project consisting of several subprojects like _Spring Data
JDBC_ or _Spring Data JPA_ or _Spring Data LDAP_. In short, they all provide extra convenience on
top of either JDBC, or JPA/Hibernate, or LDAP.

## Commands used in this module

```bash
./gradlew :my-fancy-pdf-invoices-spring-database:clean :my-fancy-pdf-invoices-spring-database:build
java -Dspring.profiles.active=dev -jar 5-spring-database/my-fancy-pdf-invoices-spring-database/build/libs/my-fancy-pdf-invoices-spring-database-1.0-all.jar
curl -X GET "http://localhost:8080/invoices"
curl -X POST "http://localhost:8080/invoices" -H "Accept: application/xml" -H "Content-Type: application/json" -d '{"amount":"20","user_id":"ari"}'

./gradlew :mybank-spring-database:clean :mybank-spring-database:build
java -jar 5-spring-database/mybank-spring-database/build/libs/mybank-spring-database-1.0-all.jar
curl -X GET "http://localhost:8080/transactions" -H "Accept: application/json"
curl -X POST "http://localhost:8080/transactions" -H "Accept: application/json" -H "Content-Type: application/json" -d '{"amount":2000,"reference":"book of the year!","receivingUser":"ardavan123"}'
```