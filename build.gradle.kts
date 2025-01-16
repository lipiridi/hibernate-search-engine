import java.util.*

fun properties(key: String) = project.findProperty(key)?.toString() ?: ""

plugins {
    `java-library`
    `maven-publish`
    signing

    id("com.diffplug.spotless") version "7.0.1"
    id("tech.yanand.maven-central-publish") version "1.3.0"
}

group = "io.github.lipiridi"
version = "1.1.1"

java {
    withSourcesJar()
    withJavadocJar()

    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val springBootVersion = "3.4.1"

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${springBootVersion}")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:${springBootVersion}")
    implementation("org.springframework.boot:spring-boot-starter-validation:${springBootVersion}")
}

tasks.withType<Javadoc> {
    (options as CoreJavadocOptions).addStringOption("Xdoclint:-missing", "-quiet")
}

publishing {
    publications {
        create<MavenPublication>("lib") {
            from(components["java"])
            pom {
                packaging = "jar"

                name.set("Hibernate Search Engine")
                url.set("https://github.com/lipiridi/hibernate-search-engine")
                description.set("Hibernate Search Engine simplifies the process of querying databases by any field, offering convenient pagination support")

                developers {
                    developer {
                        name = "Dimitrii Lipiridi"
                        email = "lipirididi@gmail.com"
                        organizationUrl = "https://github.com/lipiridi"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/lipiridi/hibernate-search-engine.git"
                    developerConnection = "scm:git:ssh://github.com:lipiridi/hibernate-search-engine.git"
                    url = "https://github.com/lipiridi/hibernate-search-engine/tree/main"
                }

                licenses {
                    license {
                        name = "MIT License"
                        url = "https://www.opensource.org/licenses/mit-license.php"
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}

mavenCentral {
    // Token for Publisher API calls obtained from Sonatype official,
    // it should be Base64 encoded of "username:password".
    val username = properties("mavenCentralUsername")
    val password = properties("mavenCentralPassword")
    val toEncode = "$username:$password"
    val encodedAuthToken = Base64.getEncoder().encodeToString(toEncode.toByteArray())

    authToken.set(encodedAuthToken)
    // Whether the upload should be automatically published or not. Use 'USER_MANAGED' if you wish to do this manually.
    // This property is optional and defaults to 'AUTOMATIC'.
    publishingType.set("USER_MANAGED")
}

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
                "Implementation-Version" to project.version))
    }
}

spotless {
    java {
        target("src/*/java/**/*.java")

        palantirJavaFormat()
        removeUnusedImports()
        importOrder()
    }
}
