plugins {
    java
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("io.papermc.paperweight.userdev") version "1.3.7"
}

val paperVersion = project.property("paper_version").toString()

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

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    compileOnly("io.papermc.paper:paper-api:${paperVersion}")
    compileOnly("com.mojang:brigadier:1.0.18")
}

tasks {
    jar {
        from("LICENSE")
    }
}

group = "me.allinkdev"
version = "1.0-SNAPSHOT"
description = "Automatically update your open-source Paper plugins."
java.sourceCompatibility = JavaVersion.VERSION_17


tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

bukkit {
    name = "AutoUpdater"
    version = rootProject.version.toString()
    main = "me.allinkdev.autoupdater.AutoUpdater"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    author = "Allink"
    description = project.description
}