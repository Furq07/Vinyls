plugins {
    java
    id("io.github.goooler.shadow") version "8.1.7"
    kotlin("jvm") version "2.0.20"
}

group = project.property("mod.group").toString()
version = "spigot-${project.property("mod.version").toString()}"

repositories {
    mavenCentral()
    maven("spigotmc-repo") {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven("https://repo.jeff-media.com/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.jeff-media:MorePersistentDataTypes:2.4.0")
    implementation("com.jeff-media:custom-block-data:2.2.2")
    implementation("com.jeff_media:SpigotUpdateChecker:3.0.3")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.processResources {
    inputs.properties(mapOf("version" to version))
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(mapOf("version" to version))
    }
}

tasks.shadowJar {
    relocate("com.jeff_media.customblockdata", "dev.furq.lib.customblockdata")
    relocate("com.jeff_media.morepersistentdatatypes", "dev.furq.lib.morepersistentdatatypes")
    relocate("com.jeff_media.updatechecker", "dev.furq.lib.updatechecker")
    archiveBaseName.set("vinyls")
    archiveClassifier.set("")
    mergeServiceFiles()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    enabled = false
}