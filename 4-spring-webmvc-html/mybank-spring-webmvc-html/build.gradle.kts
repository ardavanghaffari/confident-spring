plugins {
    application
    alias(libs.plugins.shadow)
}

application {
    mainClass.set("io.github.ardavanghaffari.mybank.ApplicationLauncher")
}

dependencies {
    implementation(libs.spring.context)
    implementation(libs.spring.webmvc)
    implementation(libs.tomcat.embed.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.dataformat.xml)
    implementation(libs.jackson.datatype)
    implementation(libs.hibernate.validator)
    implementation(libs.expressly)
    implementation(libs.thymeleaf.spring6)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
