plugins {
    `java-library`
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.spring.boot.jdbc)
    compileOnly(libs.vibur.dbcp)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
