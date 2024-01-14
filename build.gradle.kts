import java.net.URI

fun properties(key: String) = project.findProperty(key)?.toString() ?: ""

plugins {
    `java-library`
    `maven-publish`
    signing

    id("com.diffplug.spotless") version "6.23.3"
}

group = "io.github.lipiridi"
version = "0.0.1-SNAPSHOT"

java {
    withSourcesJar()
    withJavadocJar()
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
    val springBootVersion = "3.2.1"

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${springBootVersion}")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:${springBootVersion}")
    implementation("org.springframework.boot:spring-boot-starter-validation:${springBootVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
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
                    url = "http://github.com/lipiridi/hibernate-search-engine/tree/master"
                }

                licenses {
                    license {
                        name = "MIT License"
                        url = "http://www.opensource.org/licenses/mit-license.php"
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "SnapshotOSSRH"
            url = URI("https://s01.oss.sonatype.org/content/repositories/snapshots")
            credentials {
                username = properties("ossrhUsername")
                password = properties("ossrhPassword")
            }
        }

        maven {
            name = "OSSRH"
            url = URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = properties("ossrhUsername")
                password = properties("ossrhPassword")
            }
        }
    }
}

signing {
    sign(publishing.publications)
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
