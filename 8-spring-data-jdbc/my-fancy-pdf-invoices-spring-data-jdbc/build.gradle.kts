plugins {
    application
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.thymeleaf)
    implementation(libs.jackson.dataformat.xml)
    implementation(libs.h2)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
