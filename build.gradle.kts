import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.vaadin") version "25.1.5"
    id("org.openapi.generator") version "7.14.0"
    id("org.graalvm.buildtools.native") version "0.10.3" apply false
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
}

group = "com.kenjdavidson.golf"
version = (project.findProperty("release.version") as String?) ?: project.version.toString()

apply(from = "$rootDir/gradle/release.gradle")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.vaadin.com/vaadin-addons") }
}

// Enable production mode with: ./gradlew -Pproduction build
val isProduction = project.hasProperty("production")

vaadin {
    productionMode = isProduction
}

dependencyManagement {
    imports {
        mavenBom("com.vaadin:vaadin-bom:25.1.5")
        mavenBom("tools.jackson:jackson-bom:3.1.2")
    }
}

dependencies {
    // Spring Boot Web (embedded Tomcat)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Boot Security (restrict to localhost)
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Spring Data JPA + H2 in-memory database (no PII persisted after exit)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")

    // Vaadin Flow UI
    implementation("com.vaadin:vaadin-spring-boot-starter")
    implementation("com.vaadin:vaadin-dev-server")

    // Apache PDFBox – PDF scorecard parsing
    implementation("org.apache.pdfbox:pdfbox:3.0.3")

    // Lombok – boilerplate reduction
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // OpenAPI generated-client support
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.22")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Kotlin support
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

// ── GraalVM Native Image support ──────────────────────────────────────────────
// Activate with: ./gradlew -Pnative nativeCompile
if (project.hasProperty("native")) {
    apply(plugin = "org.graalvm.buildtools.native")
    extensions.configure<org.graalvm.buildtools.gradle.dsl.GraalVMExtension> {
        binaries {
            named("main") {
                imageName.set("HandicapCommitteeApp")
                mainClass.set("com.kenjdavidson.golf.handicap.HandicapApplication")
                buildArgs.addAll(
                    "--no-fallback",
                    "--initialize-at-build-time=org.slf4j",
                    "-H:+ReportExceptionStackTraces"
                )
            }
        }
    }
}

// ── OpenAPI client code generation ────────────────────────────────────────────
openApiGenerate {
    generatorName.set("java")
    library.set("resttemplate")
    inputSpec.set("$projectDir/src/main/resources/openapi/golf-canada.yaml")
    outputDir.set(layout.buildDirectory.dir("generated-sources/openapi").get().asFile.absolutePath)
    apiPackage.set("com.kenjdavidson.golf.handicap.golfcanada.api")
    modelPackage.set("com.kenjdavidson.golf.handicap.golfcanada.model")
    invokerPackage.set("com.kenjdavidson.golf.handicap.golfcanada.invoker")
    configOptions.set(
        mapOf(
            "useJakartaEe" to "true",
            "dateLibrary" to "java8"
        )
    )
    generateApiTests.set(false)
    generateModelTests.set(false)
    generateApiDocumentation.set(false)
    generateModelDocumentation.set(false)

    // Use LocalDateTime instead of OffsetDateTime as Golf Canada doesn't provide time zones
    typeMappings.set(mapOf(
        "DateTime" to "LocalDateTime"
    ))

    importMappings.set(mapOf(
        "LocalDateTime" to "java.time.LocalDateTime"
    ))
}

tasks.named("openApiGenerate") {
    doLast {
        val apiClientFile = layout.buildDirectory.file(
            "generated-sources/openapi/src/main/java/com/kenjdavidson/golf/handicap/golfcanada/invoker/ApiClient.java"
        ).get().asFile
        if (apiClientFile.exists()) {
            var content = apiClientFile.readText()
            content = content
                .replace("defaultHeaders.containsKey(", "defaultHeaders.containsHeader(")
                .replace("headers.entrySet()", "headers.headerSet()")
                .replace("UriComponentsBuilder.fromHttpUrl(", "UriComponentsBuilder.fromUriString(")
            apiClientFile.writeText(content)
        }
    }
}

// Add the generated sources to the main source set
sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated-sources/openapi/src/main/java"))
        }
    }
}

// Ensure OpenAPI sources are generated before compilation
tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}
tasks.withType<KotlinCompile> {
    dependsOn("openApiGenerate")
}

// ── Test configuration ─────────────────────────────────────────────────────────
tasks.named<Test>("test") {
    useJUnitPlatform()
}

// ── Spring Boot fat JAR ────────────────────────────────────────────────────────
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    layered {
        enabled.set(true)
    }
}

tasks.register<org.springframework.boot.gradle.tasks.run.BootRun>("runApp") {
    group = "application"
    description = "Runs the Spring Boot application."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.kenjdavidson.golf.handicap.HandicapApplication")
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    })
}

tasks.register<org.springframework.boot.gradle.tasks.run.BootRun>("debugApp") {
    group = "application"
    description = "Runs the Spring Boot application with remote debugging on port 5005."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.kenjdavidson.golf.handicap.HandicapApplication")
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    })
    jvmArgs = listOf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005")
}
