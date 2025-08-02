plugins {
    java
}

subprojects {
    group = "io.github.ardavanghaffari"
    version = "1.0"

    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    dependencies {
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.test {
        useJUnitPlatform()
    }
}
