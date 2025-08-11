plugins {
    application
    alias(libs.plugins.shadow)
}

application {
    mainClass.set("io.github.ardavanghaffari.myfancypdfinvoices.ApplicationLauncher")
}

dependencies {
    implementation(libs.tomcat.embed.core)
    implementation(libs.jackson.databind)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
