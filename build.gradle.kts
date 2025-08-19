plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.diffplug.spotless") version "6.25.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-tx")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

    // For WebClient and reactive support (used in HttpF1ProviderAdapter)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // For bean validation annotations (e.g., @NotNull, @NotBlank) in DTOs
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Optionally keep web starter if you have MVC controllers, otherwise can be removed
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation("org.springframework.boot:spring-boot-starter-test") // JUnit 5
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

spotless {
    java {
        googleJavaFormat("1.22.0")
        target("src/**/*.java")
    }
}

springBoot {
    mainClass.set("com.example.f1bet.bootstrap.F1BetApplication")
}


