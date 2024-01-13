plugins {
	`java-library`
	`maven-publish`

	id("com.diffplug.spotless") version "6.23.3"
}

group = "io.github.lipiridi"
version = "0.0.1-SNAPSHOT"

java {
	withSourcesJar()
	//withJavadocJar()
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
	val lombokVersion = "1.18.30"

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${springBootVersion}")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa:${springBootVersion}")
	implementation("org.springframework.boot:spring-boot-starter-validation:${springBootVersion}")

	compileOnly("org.projectlombok:lombok:${lombokVersion}")
	annotationProcessor("org.projectlombok:lombok:${lombokVersion}")

	testCompileOnly("org.projectlombok:lombok:${lombokVersion}")
	testAnnotationProcessor("org.projectlombok:lombok:${lombokVersion}")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

publishing {
	publications {
		create<MavenPublication>("lib") {
			from(components["java"])
			pom {
				packaging = "jar"
				name.set(project.name)
				url.set("https://github.com/lipiridi/hibernate-search-engine")
			}
		}
	}
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
