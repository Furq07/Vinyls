package dev.furq.vinyls.utils

import dev.furq.vinyls.Vinyls
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ResourcePackGenerator(private val plugin: Vinyls) {
    fun generateResourcePack(discsConfig: FileConfiguration, sourceFolder: File, targetFolder: File) {
        val soundsDir = File(targetFolder, "assets/minecraft/sounds/records").apply { mkdirs() }
        val texturesItemDir = File(targetFolder, "assets/minecraft/textures/item").apply { mkdirs() }
        val modelsItemDir = File(targetFolder, "assets/minecraft/models/item").apply { mkdirs() }
        val modelsDir = File(targetFolder, "assets/minecraft/models").apply { mkdirs() }
        val soundsJsonFile = File(targetFolder, "assets/minecraft/sounds.json")

        val soundsConfig = YamlConfiguration.loadConfiguration(soundsJsonFile)
        val soundsData = mutableMapOf<String, Any>()

        if (soundsJsonFile.exists()) {
            val rawSoundsData = soundsConfig.getValues(false)
            rawSoundsData.forEach { (key, value) ->
                if (value is Map<*, *>) {
                    soundsData[key] = value
                }
            }
        }

        val itemModelDataMap = mutableMapOf<String, MutableMap<String, Any>>()
        val existingDiscNames = mutableSetOf<String>()

        discsConfig.getConfigurationSection("discs")?.getKeys(false)?.forEach { discName ->
            val discConfig = discsConfig.getConfigurationSection("discs.$discName")!!
            val material = discConfig.getString("material")!!.lowercase()
            val customModelData = discConfig.getInt("custom_model_data")

            existingDiscNames.add(discName)

            copyFile(File(sourceFolder, "$discName.ogg"), File(soundsDir, "$discName.ogg"))
            soundsData["vinyls.$discName"] = mapOf(
                "sounds" to listOf(mapOf("name" to "records/$discName", "stream" to true))
            )

            val textureFile = File(sourceFolder, "$discName.png")
            val textureExists = textureFile.exists()

            copyFile(textureFile, File(texturesItemDir, "$discName.png"))

            val texturePath = if (textureExists) "$discName" else "minecraft:item/music_disc_cat"

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
                    mapOf("parent" to "item/generated", "textures" to mapOf("layer0" to "item/$discName"))
                discModelPath.writeText(discModelData.toJson())
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
            val itemModelPath = File(modelsItemDir, "${material}.json")
            itemModelPath.writeText(itemModelData.toJson())
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

        soundsJsonFile.writeText(soundsData.toJson())
        File(
            targetFolder,
            "pack.mcmeta"
        ).writeText("""{"pack":{"description":"Vinyls Pack Generation","pack_format":34}}""".trimIndent())
        zipResourcePack(targetFolder)
    }

    private fun copyFile(source: File, destination: File) {
        if (source.exists()) {
            source.copyTo(destination, overwrite = true)
        } else {
            plugin.logger.warning("${source.absolutePath} does not exist.")
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

    private fun Map<String, Any>.toJson(): String = buildString {
        append("{")
        this@toJson.entries.joinToString(",") { (key, value) ->
            "\"$key\":${value.toJson()}"
        }.also { append(it) }
        append("}")
    }

    private fun List<*>.toJson(): String = buildString {
        append("[")
        this@toJson.joinToString(",") { item ->
            item.toJson()
        }.also { append(it) }
        append("]")
    }

    private fun Any?.toJson(): String = when (this) {
        is String -> "\"$this\""
        is Number, is Boolean -> this.toString()
        is Map<*, *> -> (this as Map<String, Any>).toJson()
        is List<*> -> this.toJson()
        else -> "\"$this\""
    }
}