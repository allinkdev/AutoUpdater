plugins {
    java
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("io.papermc.paperweight.userdev") version "1.3.7"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val minecraftVersion = project.property("minecraft_version").toString()
val paperVersion = "${minecraftVersion}-R0.1-SNAPSHOT"

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        url = uri("https://libraries.minecraft.net")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    paperDevBundle(paperVersion)

    implementation(project(":Common"))

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    compileOnly("io.papermc.paper:paper-api:${paperVersion}")
    compileOnly("com.mojang:brigadier:1.0.18")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    jar {
        archiveBaseName.set("AutoUpdater")
        archiveAppendix.set(project.name)

        from("LICENSE")
    }

    shadowJar {
        archiveBaseName.set("AutoUpdater")
        archiveAppendix.set(project.name)

        from("LICENSE")
    }

    reobfJar {
        dependsOn(shadowJar)

        outputJar.set(layout.buildDirectory.file("libs/AutoUpdater-${project.name}-${project.version}.jar"))
    }
}

group = "me.allinkdev"
version = project.property("project_version").toString()
description = "Automatically update your open-source Paper plugins."
java.sourceCompatibility = JavaVersion.VERSION_17


tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

bukkit {
    name = "AutoUpdater"
    version = rootProject.version.toString()
    main = "me.allinkdev.autoupdater.paper.Main"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    author = "Allink"
    description = project.description
    version = project.version.toString()
    apiVersion = minecraftVersion
}