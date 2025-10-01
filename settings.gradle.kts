rootProject.name = "confident-spring"

include(":my-fancy-pdf-invoices")
include(":mybank")
include(":my-fancy-pdf-invoices-spring")
include(":mybank-spring")
include(":my-fancy-pdf-invoices-spring-webmvc")
include(":mybank-spring-webmvc")
include(":my-fancy-pdf-invoices-spring-webmvc-html")
include(":mybank-spring-webmvc-html")

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
