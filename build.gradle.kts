import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    kotlin("jvm") version "1.2.51"
    maven
}

group = "com.github.agilecontent"
version = "1.0"

repositories {
    jcenter()
    mavenCentral()
    maven("https://jitpack.io" )
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.google.code.gson:gson:2.8.2")
    implementation("com.squareup.okhttp3:okhttp:3.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.22.5")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:0.22.5")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.jvmTarget = "1.8"
}
kotlin {
    experimental.coroutines = Coroutines.ENABLE
}