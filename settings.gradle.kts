rootProject.name = "confident-spring"

include(":my-fancy-pdf-invoices")
include(":mybank")
include(":my-fancy-pdf-invoices-spring")
include(":mybank-spring")
include(":my-fancy-pdf-invoices-spring-webmvc")
include(":mybank-spring-webmvc")
include(":my-fancy-pdf-invoices-spring-webmvc-html")
include(":mybank-spring-webmvc-html")
include(":my-fancy-pdf-invoices-spring-database")
include(":mybank-spring-database")
include(":my-fancy-pdf-invoices-spring-boot")
include(":mybank-spring-boot")
include(":example-spring-boot-starter")
include(":my-fancy-pdf-invoices-spring-boot-vibur-dbcp")
include(":my-fancy-pdf-invoices-spring-data-jdbc")

project(":my-fancy-pdf-invoices").projectDir =
    file("1-java-webapps-without-spring/my-fancy-pdf-invoices")

project(":mybank").projectDir =
    file("1-java-webapps-without-spring/mybank")

project(":my-fancy-pdf-invoices-spring").projectDir =
    file("2-spring-ioc-basics/my-fancy-pdf-invoices-spring")

project(":mybank-spring").projectDir =
    file("2-spring-ioc-basics/mybank-spring")

project(":my-fancy-pdf-invoices-spring-webmvc").projectDir =
    file("3-spring-webmvc-rest-services/my-fancy-pdf-invoices-spring-webmvc")

project(":mybank-spring-webmvc").projectDir =
    file("3-spring-webmvc-rest-services/mybank-spring-webmvc")

project(":my-fancy-pdf-invoices-spring-webmvc-html").projectDir =
    file("4-spring-webmvc-html/my-fancy-pdf-invoices-spring-webmvc-html")

project(":mybank-spring-webmvc-html").projectDir =
    file("4-spring-webmvc-html/mybank-spring-webmvc-html")

project(":my-fancy-pdf-invoices-spring-database").projectDir =
    file("5-spring-database/my-fancy-pdf-invoices-spring-database")

project(":mybank-spring-database").projectDir =
    file("5-spring-database/mybank-spring-database")

project(":my-fancy-pdf-invoices-spring-boot").projectDir =
    file("6-spring-boot/my-fancy-pdf-invoices-spring-boot")

project(":mybank-spring-boot").projectDir =
    file("6-spring-boot/mybank-spring-boot")

project(":example-spring-boot-starter").projectDir =
    file("7-spring-boot-internals/example-spring-boot-starter")

project(":my-fancy-pdf-invoices-spring-boot-vibur-dbcp").projectDir =
    file("7-spring-boot-internals/my-fancy-pdf-invoices-spring-boot-vibur-dbcp")

project(":my-fancy-pdf-invoices-spring-data-jdbc").projectDir =
    file("8-spring-data-jdbc/my-fancy-pdf-invoices-spring-data-jdbc")
