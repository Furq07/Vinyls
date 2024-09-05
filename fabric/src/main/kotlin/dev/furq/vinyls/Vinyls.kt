package dev.furq.vinyls

import dev.furq.vinyls.commands.VinylsCommand
import dev.furq.vinyls.listeners.InventoryUpdateListener
import dev.furq.vinyls.utils.ResourcePackGenerator
import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class Vinyls : ModInitializer {

    private lateinit var vinylsCommand: VinylsCommand
    private lateinit var inventoryUpdateListener: InventoryUpdateListener
    val modID = "vinyls"
    val logger: Logger = LoggerFactory.getLogger(modID)

    override fun onInitialize() {
        try {
            vinylsCommand = VinylsCommand(this)
            inventoryUpdateListener = InventoryUpdateListener()
            loadConfig()
            logger.info("Thank you for using my mod - Furq")
        } catch (e: Exception) {
            logger.error("An error occurred during initialization", e)
        }
    }

    fun loadConfig() {
        try {
            val configFolder = File("config/$modID")
            if (!configFolder.exists()) configFolder.mkdirs()

            val discsConfigFile = File(configFolder, "discs.yml")
            if (!discsConfigFile.exists()) {
                "discs.yml".saveResource(discsConfigFile)
            }

            val messageConfigFile = File(configFolder, "messages.yml")
            if (!messageConfigFile.exists()) {
                "messages.yml".saveResource(messageConfigFile)
            }

            val sourceFolder = File(configFolder, "source_files")
            val targetFolder = File(configFolder, "resource_pack")
            if (!sourceFolder.exists()) sourceFolder.mkdirs()
            if (!targetFolder.exists()) targetFolder.mkdirs()

            ResourcePackGenerator(this).generateResourcePack(discsConfigFile, sourceFolder, targetFolder)
        } catch (e: Exception) {
            logger.error("An error occurred while loading configuration", e)
        }
    }

    private fun String.saveResource(target: File) {
        try {
            val resourceStream: InputStream? = this@Vinyls.javaClass.classLoader.getResourceAsStream(this)
            if (resourceStream != null) {
                resourceStream.use { input ->
                    Files.copy(input, target.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    logger.info("Resource $this copied to ${target.absolutePath}")
                }
            } else {
                logger.error("Resource $this not found!")
            }
        } catch (e: Exception) {
            logger.error("Failed to save resource $this to ${target.absolutePath}", e)
        }
    }
}