import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    id("fabric-loom")
    kotlin("jvm") version "2.0.20"
}

class ModData {
    val id = property("mod.id").toString()
    val name = property("mod.name").toString()
    val version = property("mod.version").toString()
    val group = property("mod.group").toString()
}

class ModDependencies {
    operator fun get(name: String) = property("deps.$name").toString()
}

val mod = ModData()
val deps = ModDependencies()
val mcVersion = stonecutter.current.version
val mcDep = property("mod.mc_dep").toString()

version = "fabric-${mod.version}+$mcVersion"
group = mod.group
base { archivesName.set(mod.id) }

repositories {
    mavenCentral()
}

dependencies {
    fun fapi(vararg modules: String) {
        modules.forEach { fabricApi.module(it, deps["fapi"]) }
    }

    minecraft("com.mojang:minecraft:${mcVersion}")
    mappings("net.fabricmc:yarn:${mcVersion}+build.${deps["yarn_build"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${deps["fabric_loader"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.12.1+kotlin.2.0.20")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${deps["fabric_api"]}")
    vineflowerDecompilerClasspath("org.vineflower:vineflower:1.10.1")
    modImplementation(include("org.yaml", "snakeyaml", "2.2"))
    modImplementation(include("dev.furq", "spindle", "1.0.0"))
    modImplementation(include("net.kyori", "adventure-text-serializer-legacy", "4.17.0"))
    modImplementation(include("net.kyori", "adventure-api", "4.17.0"))
    modImplementation(include("net.kyori", "adventure-key", "4.17.0"))
    modImplementation(include("net.kyori", "adventure-text-minimessage", "4.17.0"))
    modImplementation(include("net.kyori", "examination-api", "1.3.0"))
}

loom {
    decompilers {
        get("vineflower").apply {
            options.put("mark-corresponding-synthetics", "1")
        }
    }

    runConfigs.all {
        ideConfigGenerated(stonecutter.current.isActive)
        vmArgs("-Dmixin.debug.export=true")
        runDir = "../../run"
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(
            if (stonecutter.compare(mcVersion, "1.20.6") >= 0)
                org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
            else
                org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        )
    }
}

java {
    withSourcesJar()
    val javaVersion =
        if (stonecutter.compare(mcVersion, "1.20.6") >= 0) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = javaVersion
    sourceCompatibility = javaVersion
}
tasks.processResources {
    inputs.property("id", mod.id)
    inputs.property("name", mod.name)
    inputs.property("version", mod.version)
    inputs.property("mcdep", mcDep)

    val map = mapOf(
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "mcdep" to mcDep
    )

    filesMatching("fabric.mod.json") { expand(map) }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.remapJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))
    dependsOn("build")
}