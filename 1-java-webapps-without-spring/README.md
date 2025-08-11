# Java Webapps Without Spring

## WAR vs. Fat JAR

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

Using the jar task and specifying the main class in the manifest will package only the application's
classes. It will not include any of its dependencies, which means running the JAR will fail due to
missing classes:

```text
// This won't package Tomcat

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
defines what the name of the field is going to be in the resulting JSON string. See, for example,
[Invoice](./my-fancy-pdf-invoices/src/main/java/io/github/ardavanghaffari/myfancypdfinvoices/model/Invoice.java).

## Exercises

We are asked to implement a _mybank_ application that's similar to _my-fany-pdf-invoices_ in
structure which will be used to create and query bank transactions. A transaction has a timestamp
and the API's JSON response should format it as `yyyy-MM-dd'T'HH:mm'Z'`. We are additionally asked
to make the server port configurable from the command line and when not specified default to 8080.

### Formatted Timestamp

First, we'll need an additional Jackson library `jackson-datatype-jsr310` to be able to handle
Java 8+ date/time datatypes in JSON. The first question that comes into mind is which datatype
to use for the timestamp field? I ended up using `Instant` while the author went for
`ZonedDateTime`. Which one is better and how are they different? Although the application currently
stores transactions only in memory, for the purposes of this note we'll assume they are persisted in
a PostgreSQL database.

```java

@JsonFormat(shape = STRING, pattern = "yyyy-MM-dd'T'HH:mm'Z'", timezone = "Europe/Amsterdam")
private Instant timestamp;

@JsonFormat(shape = STRING, pattern = "yyyy-MM-dd'T'HH:mm'Z'")
private ZonedDateTime timestamp;
```

#### Instant

- Represents an absolute moment in time as seconds/nanoseconds from the epoch in UTC. Basically just
  a timestamp. A point in time.
- No timezone information, always stored as an offset from the epoch `1970-01-01T00:00:00Z`. So it
  doesn't carry any notion of _year_, _month_, _day_, etc. Those fields are calculated based on a
  timezone. That's why specifying `timezone` in the annotation above is necessary. We'll otherwise
  get an exception like
  `com.fasterxml.jackson.databind.JsonMappingException: Unsupported field: YearOfEra`. Jackson (or
  the formatter used internally by Jackson) tries to fetch the `YearOfEra` field from the `Instant`,
  but since `Instant` doesn't have it, it throws that exception. Note that even if we wanted our
  timestamp to be formatted in UTC, we should have still specified `timezone = "UTC"` in the
  annotation. Jackson doesn't assume because it's an `Instant` then it can default to UTC and it
  will throw the same exception in absence of the timezone value.
- We say on the one hand that `Instant` is in UTC but we also say that it has no timezone which may
  sound contradictory since UTC is a timezone after all. When we say that it has no timezone, we're
  merely emphasizing the fact that it has an offset of exactly `+00:00` and that it's not tied to
  any particular region or city like `Europe/Amsterdam`.
- Use `Instant` when you want a time that is unambiguous and consistent across servers/timezones.
  Ideal for backend systems and auditing timestamps (e.g., `created_at`, `updated_at` and also
  in this case the `timestamp` field).
- Maps to PostgreSQL's `TIMESTAMP WITH TIME ZONE`.

#### ZonedDateTime

- Represents a full date-time with a region-based timezone (e.g., `Europe/Amsterdam`) and an
  offset. Example `2025-08-05T14:30:00+02:00[Europe/Amsterdam]`.
- Already contains all the local date/time fields (year, month, day, hour, etc.) computed from the
  stored zone.
- Jackson doesn't need any extra information to apply our pattern — it just uses the zone that's
  already stored in the object.
- Use `ZonedDateTime` when you need to preserve the exact timezone in which the timestamp was
  created. Useful for user-facing events, scheduling systems, or calendar apps where knowing the
  original timezone matters. Example: A meeting scheduled in `America/New_York` should stay in
  that zone even if viewed from Europe.
- Maps to PostgreSQL's `TIMESTAMP WITH TIME ZONE`.

`ZonedDateTime` is already a _ready-to-print_ date/time. `Instant` is _just a point in time_ and
needs a zone before you can print it in a human-readable pattern. This was all so far about the
transformation that Jackson does on the Java object. Let's see how the Java objects are mapped
to/from the database.

#### PostgreSQL

Internally, date and time values in PostgreSQL are persisted as microseconds since January 1st,
2000 UTC. They are a specific moment in time and all calculations and conversions are based on that.
**PostgreSQL doesn't persist the original timezone information**. In spite of that, PostgreSQL has
a.o. two misleadingly named types `TIMESTAMP WITH TIME ZONE` and `TIMESTAMP WITHOUT TIME ZONE`.

##### TIMESTAMP WITHOUT TIME ZONE

Whatever value you insert is stored exactly as you wrote it. PostgreSQL does no timezone related
conversion on input or output. If the value was entered into the database as `2011-07-01 06:30:30`,
then no mater in what timezone you display it later, it will still say year 2011, month 07, day 01,
06 hours, 30 minutes, and 30 seconds. Also, any offset or timezone you specify in the input is
ignored by PostgreSQL, so `2011-07-01 06:30:30+00` and `2011-07-01 06:30:30+05` are the same as just
`2011-07-01 06:30:30`.

```sql
-- Session timezone: UTC
CREATE TABLE ts_no_tz (ts TIMESTAMP);
INSERT INTO ts_no_tz VALUES ('2025-08-10 15:00:00');
SELECT ts FROM ts_no_tz;

// output
2025-08-10 15:00:00

// change session timezone
SET TIMEZONE TO 'America/New_York';
SELECT ts FROM ts_no_tz;

// output is still
2025-08-10 15:00:00
```

Stored exactly as typed. No interpretation.

##### TIMESTAMP WITH TIME ZONE

Stores a point on the UTC timeline. The input is internally converted to UTC, and that's how it's
stored. For that, the offset of the input must be known, so when the input contains no explicit
offset or timezone (like `2011-07-01 06:30:30`) it's assumed to be in the current timezone of the
PostgreSQL session, otherwise the explicitly specified offset or timezone is used (as in
`2011-07-01 06:30:30+05`). The output is displayed converted to the current timezone of the
PostgreSQL session.

```sql
-- Session timezone: UTC
CREATE TABLE ts_tz (ts TIMESTAMPTZ);
INSERT INTO ts_tz VALUES ('2025-08-10 15:00:00');
SELECT ts FROM ts_tz;

// output
2025-08-10 15:00:00+00

SET TIMEZONE TO 'America/New_York';
SELECT ts FROM ts_tz;

// output
2025-08-10 11:00:00-04
```

So putting it all together, they look like this in memory:

```java
Instant instant = Instant.parse("2025-08-11T10:00:00Z");
2025-08-11 10:00:00UTC
ZonedDateTime zoned = ZonedDateTime.of(2025, 8, 11, 12, 0, 0, 0, ZoneId.of("Europe/Amsterdam"));
2025-08-11 12:00:00+02:00
```

_ORM_ will:

- Convert both to UTC for database storage (because of `TIMESTAMP WITH TIME ZONE`).
- Send via JDBC as UTC timestamps.

```sql
insert into event (instant_time, zoned_time) 
values (timestamp with time zone '2025-08-11 10:00:00+00', 
        timestamp with time zone '2025-08-11 10:00:00+00');
Both end up stored with the same UTC value in the DB!
```

When reading back from the database, JDBC retrieves the UTC timestamp and

- For `Instant`, maps directly to `Instant` (no zone needed).
- For `ZonedDateTime`, uses the timezone in the code or falls back to system default zone (coming
  from JDBC driver or JVM default) to reconstruct the datetime.

Even though both `Instant` and `ZonedDateTime` end up stored the same way in PostgreSQL, it still
matters which one you choose in Java — because they behave differently in memory and at the API
level. If you only need the moment in time and will apply the timezone later when displaying, use
`Instant`. If you also want to carry zone info in your Java objects for calculations, use
`ZonedDateTime`.

### Server Port

When running the jar, note that the `-D` option must come before the `-jar` as anything after
`-jar myapp.jar` will be treated as a program argument instead of a JVM option. So we'll have to
run as:

```bash
java -Dserver.port=8090 -jar 1-java-webapps-without-spring/mybank/build/libs/mybank-1.0-all.jar
```

## Curl Commands

```bash
curl -X GET "http://localhost:8080/invoices" -H "Accept: application/json"
curl -X POST "http://localhost:8080/invoices?user_id=freddieFox&amount=50" -H "Accept: application/json"

curl -X GET "http://localhost:8090/transactions" -H "Accept: application/json"
curl -X POST "http://localhost:8090/transactions?amount=50&reference=book" -H "Accept: application/json"
```

## Sources

- https://stackoverflow.com/questions/5876218/difference-between-timestamps-with-without-time-zone-in-postgresql
- https://www.baeldung.com/java-postgresql-store-date-time
