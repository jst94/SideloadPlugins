plugins {
    `java-library`
    
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.runelite.net")
    }
    mavenCentral()
}

dependencies {
    compileOnly("net.runelite:client:1.11.2.1")
    compileOnly("org.projectlombok:lombok:1.18.22")
    implementation("org.pf4j:pf4j:3.6.0")
    annotationProcessor("org.projectlombok:lombok:1.18.22")
    implementation(files("lib/EthanVannPlugins-5.4.jar"))
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    jar {
        manifest {
            attributes(mapOf(
                "Plugin-Version" to "6.6.8",
                "Plugin-Id" to project.name.toLowerCase(),
                "Plugin-Provider" to "com.lucidplugins",
                "Plugin-Description" to "LucidPlugins Collection",
                "Plugin-License" to "3-Clause BSD License",
                "Plugin-Requires" to ">=1.11.2.1"
            ))
        }
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }
}
