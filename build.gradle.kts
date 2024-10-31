import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.runelite.net")
    }
    gradlePluginPortal()
    mavenCentral()
}

val runeLiteVersion = "latest.release"

dependencies {
    compileOnly(group = "com.example", name = "example", version = "5.4")
    compileOnly("org.projectlombok:lombok:1.18.20")
    compileOnly("net.runelite:client:$runeLiteVersion")
    compileOnly("org.pf4j:pf4j:3.6.0")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    testImplementation("junit:junit:4.13.1")
    implementation("org.benf:cfr:0.151")
    implementation("org.roaringbitmap:RoaringBitmap:0.9.0")
}

group = "com.lucidplugins"
version = "6.6.8"

val javaMajorVersion = JavaVersion.VERSION_11.majorVersion

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaMajorVersion
        targetCompatibility = javaMajorVersion
    }
    withType<Jar> {
        manifest {

        }
    }
    withType<ShadowJar> {
        baseName = "LucidPlugins"
        exclude("com/lucidplugins/lucidfletching/")
    }
}

