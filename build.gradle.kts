import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
	`java-library`
	`maven-publish`

	id("com.diffplug.spotless") version "6.23.3"
	id("com.google.cloud.artifactregistry.gradle-plugin") version "2.2.1"
	id("org.springframework.boot") version "3.2.1" apply false
	id("io.spring.dependency-management") version "1.1.4"
}

group = "io.github.lipiridi"
version = "0.0.1-SNAPSHOT"

the<DependencyManagementExtension>().apply {
	imports {
		mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
	}
}

java {
	withSourcesJar()
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
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.fasterxml.jackson.core:jackson-databind")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
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
