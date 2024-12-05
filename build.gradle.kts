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

val runeLiteVersion = "1.10.45"

dependencies {
    implementation(files("lib/EthanVannPlugins-5.4.jar"))
    compileOnly("org.projectlombok:lombok:1.18.20")
    compileOnly("net.runelite:client:$runeLiteVersion")
    compileOnly("org.pf4j:pf4j:3.6.0")
    implementation("net.runelite:runelite-api:$runeLiteVersion")
    
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    annotationProcessor("net.runelite:client:$runeLiteVersion")
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
            attributes(mapOf(
                "Plugin-Version" to project.version,
                "Plugin-Id" to project.name.toLowerCase(),
                "Plugin-Provider" to project.group,
                "Plugin-Description" to "LucidPlugins Collection",
                "Plugin-License" to "3-Clause BSD License",
                "Plugin-Dependencies" to "",
                "Plugin-Requires" to ">=${runeLiteVersion}",
                "Injection-Type" to "safe"
            ))
        }
    }
    withType<ShadowJar> {
        archiveBaseName.set("LucidPlugins")
        exclude("com/lucidplugins/lucidfletching/")
        archiveClassifier.set("")
        configurations = listOf(project.configurations.runtimeClasspath.get())
        doLast {
            copy {
                from(archiveFile)
                into("C:/Users/Jeroen/.runelite/sideloaded-plugins")
            }
        }
    }
}
