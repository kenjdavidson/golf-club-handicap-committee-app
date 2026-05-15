import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.vaadin") version "24.3.13"
    id("org.openapi.generator") version "7.6.0"
    id("org.graalvm.buildtools.native") version "0.10.3" apply false
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
}

group = "com.golfclub"
version = "1.0.0-SNAPSHOT"

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
        mavenBom("com.vaadin:vaadin-bom:24.3.13")
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

    // Vaadin 24 Flow UI
    implementation("com.vaadin:vaadin-spring-boot-starter")

    // Apache PDFBox – PDF scorecard parsing
    implementation("org.apache.pdfbox:pdfbox:3.0.3")

    // Lombok – boilerplate reduction
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // OpenAPI generated-client support
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.22")

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
                mainClass.set("com.golfclub.handicap.HandicapApplication")
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
    apiPackage.set("com.golfclub.handicap.golfcanada.api")
    modelPackage.set("com.golfclub.handicap.golfcanada.model")
    invokerPackage.set("com.golfclub.handicap.golfcanada.invoker")
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

// ── jpackage installer ─────────────────────────────────────────────────────────
// Creates a self-contained Windows .exe installer.
// Activate with: ./gradlew -Pproduction jpackageInstaller
// Requires JDK 21+ with jpackage on PATH.
val jpackageAppName = project.findProperty("jpackage.app.name") as String? ?: "HandicapCommitteeApp"
val jpackageAppVersion = project.findProperty("jpackage.app.version") as String? ?: "1.0.0"
val jpackageVendor = project.findProperty("jpackage.vendor") as String? ?: "Golf Club Handicap Committee"

tasks.register<Exec>("jpackageInstaller") {
    description = "Creates a self-contained Windows .exe installer using jpackage."
    group = "distribution"
    dependsOn("bootJar")

    val bootJar = tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar").get()
    val inputDir = bootJar.archiveFile.get().asFile.parentFile
    val outputDir = layout.buildDirectory.dir("jpackage").get().asFile

    doFirst { outputDir.mkdirs() }

    commandLine(
        "jpackage",
        "--type", "exe",
        "--name", jpackageAppName,
        "--app-version", jpackageAppVersion,
        "--vendor", jpackageVendor,
        "--description", "Golf Club Handicap Committee Desktop Application",
        "--input", inputDir.absolutePath,
        "--main-jar", bootJar.archiveFileName.get(),
        "--main-class", "org.springframework.boot.loader.JarLauncher",
        "--dest", outputDir.absolutePath,
        "--win-shortcut",
        "--win-menu",
        "--win-menu-group", "Golf Club"
    )
}
