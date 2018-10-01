import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.2.71"
    application
}

group = "spiderhttpRR"
version = "1.0.0"

repositories {
    mavenCentral()
    jcenter()
}

val myMainClassName = "com.github.maksimdvl.spiderhttpRR.AppKt"

application {
    mainClassName = myMainClassName
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "0.27.0")
    // compile("com.google.code.gson", "gson", "2.8.5")
   // compile("com.beust", "klaxon", "2.0.11")
    compile("com.github.kittinunf.fuel" ,"fuel", "1.15.0")
    compile("com.github.kittinunf.fuel" ,"fuel-coroutines", "1.15.0")
   // compile( "org.jsoup","jsoup", "1.11.3")
    testCompile("junit", "junit", "4.12")
}

tasks.withType<Jar>() {
    manifest {
        attributes["Main-Class"] = myMainClassName
    }
    configurations["compileClasspath"].forEach {
        from(zipTree(it.absoluteFile))
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}