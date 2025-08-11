rootProject.name = "confident-spring"

include(":my-fancy-pdf-invoices")
include(":mybank")

project(":my-fancy-pdf-invoices").projectDir =
    file("1-java-webapps-without-spring/my-fancy-pdf-invoices")
project(":mybank").projectDir =
    file("1-java-webapps-without-spring/mybank")
