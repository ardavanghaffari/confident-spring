plugins {
    application
    alias(libs.plugins.shadow)
}

application {
    mainClass.set("io.github.ardavanghaffari.mybank.ApplicationLauncher")
}

dependencies {
    implementation(libs.tomcat.embed.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
