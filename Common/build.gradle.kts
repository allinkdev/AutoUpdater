plugins {
    id("java")
}

group = "me.allinkdev"
version = project.property("project_version").toString()

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.apache.logging.log4j:log4j-slf4j18-impl:2.17.0")

    compileOnly("com.google.code.gson:gson:2.9.0")
    compileOnly("com.google.guava:guava:31.0.1-jre")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}