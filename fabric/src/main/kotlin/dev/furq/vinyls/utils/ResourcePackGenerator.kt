package dev.furq.vinyls.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.furq.spindle.Parser
import org.slf4j.Logger
import java.io.*
import java.lang.reflect.Type
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ResourcePackGenerator(private val logger: Logger) {

    private val gson = Gson()

    fun generateResourcePack(discsConfig: Parser, sourceFolder: File, targetFolder: File) {
        val soundsDir = File(targetFolder, "assets/minecraft/sounds/records").apply { mkdirs() }
        val texturesItemDir = File(targetFolder, "assets/minecraft/textures/item").apply { mkdirs() }
        val modelsItemDir = File(targetFolder, "assets/minecraft/models/item").apply { mkdirs() }
        val modelsDir = File(targetFolder, "assets/minecraft/models").apply { mkdirs() }
        val soundsJsonFile = File(targetFolder, "assets/minecraft/sounds.json")

        val soundsData = if (soundsJsonFile.exists()) {
            loadJsonConfig(soundsJsonFile).toMutableMap()
        } else {
            mutableMapOf()
        }

        val itemModelDataMap = mutableMapOf<String, MutableMap<String, Any>>()
        val existingDiscNames = mutableSetOf<String>()
        val discs = discsConfig.getMap("discs") as Map<String, Map<String, Any>>
        discs.forEach { (discName, discData) ->
            val material = discData["material"] as String
            val customModelData = discData["custom_model_data"] as Int

            existingDiscNames.add(discName)

            copyFile(File(sourceFolder, "$discName.ogg"), File(soundsDir, "$discName.ogg"))
            soundsData["vinyls.$discName"] = mapOf(
                "sounds" to listOf(mapOf("name" to "records/$discName", "stream" to true))
            )

            val textureFile = File(sourceFolder, "$discName.png")
            val textureExists = textureFile.exists()

            copyFile(textureFile, File(texturesItemDir, "$discName.png"))

            val texturePath = if (textureExists) discName else "minecraft:item/music_disc_cat"

            val itemModelData = itemModelDataMap.getOrPut(material) { mutableMapOf() }
            itemModelData["parent"] = "minecraft:item/generated"
            itemModelData["textures"] = mapOf("layer0" to "item/$material")
            itemModelData["overrides"] =
                (itemModelData["overrides"] as? MutableList<Map<String, Any>> ?: mutableListOf()).apply {
                    add(
                        mapOf(
                            "predicate" to mapOf("custom_model_data" to customModelData),
                            "model" to texturePath
                        )
                    )
                }

            if (textureExists) {
                val discModelPath = File(modelsDir, "$discName.json")
                val discModelData =
                    mapOf("parent" to "minecraft:item/generated", "textures" to mapOf("layer0" to "item/$discName"))
                saveJsonConfig(discModelPath, discModelData)
            }
        }

        soundsDir.listFiles()?.forEach { oggFile ->
            val discName = oggFile.nameWithoutExtension
            if (discName !in existingDiscNames) {
                oggFile.delete()
            }
        }

        texturesItemDir.listFiles()?.forEach { pngFile ->
            val discName = pngFile.nameWithoutExtension
            if (discName !in existingDiscNames) {
                pngFile.delete()
            }
        }

        itemModelDataMap.forEach { (material, itemModelData) ->
            val itemModelPath = File(modelsItemDir, "$material.json")
            saveJsonConfig(itemModelPath, itemModelData)
        }

        modelsItemDir.listFiles()?.forEach { materialFile ->
            val materialName = materialFile.nameWithoutExtension
            if (!itemModelDataMap.containsKey(materialName)) {
                materialFile.delete()
            }
        }

        modelsDir.listFiles()?.forEach { modelFile ->
            val discName = modelFile.nameWithoutExtension
            if (discName !in existingDiscNames) {
                modelFile.delete()
            }
        }

        saveJsonConfig(soundsJsonFile, soundsData)
        File(
            targetFolder,
            "pack.mcmeta"
        ).writeText("""{"pack":{"description":"Vinyls Pack Generation","pack_format":34}}""".trimIndent())
        zipResourcePack(targetFolder)
    }

    private fun saveJsonConfig(file: File, data: Map<String, Any>) {
        FileWriter(file).use { writer ->
            gson.toJson(data, writer)
        }
    }

    private fun loadJsonConfig(file: File): Map<String, Any> {
        FileReader(file).use { reader ->
            val type: Type = object : TypeToken<Map<String, Any>>() {}.type
            return gson.fromJson(reader, type)
        }
    }

    private fun copyFile(source: File, destination: File) {
        if (source.exists()) {
            source.copyTo(destination, overwrite = true)
        } else {
            logger.info("\u00A74WARNING\u00A7a: ${source.absolutePath} does not exist.")
        }
    }

    private fun zipResourcePack(targetFolder: File) {
        val zipFile = File(targetFolder.parentFile, "${targetFolder.name}.zip")
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zipOut ->
            targetFolder.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val entryName = targetFolder.toPath().relativize(file.toPath()).toString().replace("\\", "/")
                    zipOut.putNextEntry(ZipEntry(entryName))
                    file.inputStream().use { it.copyTo(zipOut) }
                    zipOut.closeEntry()
                }
            }
        }
    }
}