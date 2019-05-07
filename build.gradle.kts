import groovy.sql.Sql
import org.h2.Driver
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc

plugins {
    kotlin("jvm") version "1.3.31"
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jooq:jooq-codegen:3.11.0")
        classpath("com.h2database:h2:1.4.199")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk7"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jooq:jooq:3.11.0")

    implementation(kotlin("test"))
    implementation(kotlin("test-junit"))
    testImplementation("com.h2database:h2:1.4.199")
}


Sql(Driver().connect("jdbc:h2:mem:test-jooq-tools", null)).use { sql ->
    sql.execute(File("src/test/resources/db.sql").readText())
    val configuration = Configuration().apply {
        generator = Generator().apply {
            jdbc = Jdbc().apply {
                driver = "org.h2.Driver"
                url = "jdbc:h2:mem:test-jooq-tools"
            }
            database = Database().apply {
                name = "org.jooq.meta.h2.H2Database"
                inputSchema = "PUBLIC"
            }
            target = org.jooq.meta.jaxb.Target().apply {
                packageName = "org.kotlin.everywhere.kejooq.database"
                directory = "src/test/java"
            }
        }
    }
    GenerationTool.generate(configuration)
}
