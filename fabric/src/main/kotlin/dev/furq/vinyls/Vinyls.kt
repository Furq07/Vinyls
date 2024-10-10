package dev.furq.vinyls

import dev.furq.spindle.Config
import dev.furq.spindle.Parser
import dev.furq.spindle.SerializerType
import dev.furq.vinyls.commands.VinylsCommand
import dev.furq.vinyls.listeners.InventoryUpdateListener
import dev.furq.vinyls.utils.ResourcePackGenerator
import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class Vinyls : ModInitializer {

    companion object {
        const val modID = "vinyls"
        val logger: Logger = LoggerFactory.getLogger(modID)
        lateinit var messages: Parser
        lateinit var discs: Parser
        lateinit var prefix: String
    }

    private lateinit var vinylsCommand: VinylsCommand
    private lateinit var inventoryUpdateListener: InventoryUpdateListener
    private val configFolder = File("config/$modID")

    override fun onInitialize() {
        try {
            loadConfig()
            prefix = messages.getString("prefix", "&9Vinyls &6»")
            vinylsCommand = VinylsCommand(this)
            inventoryUpdateListener = InventoryUpdateListener()
            logger.info("Thank you for using my mod - Furq")
        } catch (e: Exception) {
            logger.error("An error occurred during initialization", e)
        }
    }

    fun loadConfig() {
        try {
            val configFiles = listOf("discs.yml", "messages.yml")
            Config.setupConfig(configFolder, configFiles)
            discs = Config.load("discs.yml")
            messages = Config.load("messages.yml")
            val formatType = Config.load("messages.yml").getBoolean("minimessage_format", false)
            var serializerType = SerializerType.LEGACY
            if (formatType) serializerType = SerializerType.MINIMESSAGE
            Config.setSerializerType(serializerType)

            val sourceFolder = File(configFolder, "source_files")
            val targetFolder = File(configFolder, "resource_pack")
            if (!sourceFolder.exists()) sourceFolder.mkdirs()
            if (!targetFolder.exists()) targetFolder.mkdirs()

            ResourcePackGenerator(logger).generateResourcePack(
                discs,
                sourceFolder,
                targetFolder
            )
        } catch (e: Exception) {
            logger.error("An error occurred while loading configuration", e)
        }
    }
}