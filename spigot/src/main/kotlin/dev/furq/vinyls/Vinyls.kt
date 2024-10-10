package dev.furq.vinyls

import com.jeff_media.customblockdata.CustomBlockData
import com.jeff_media.updatechecker.UpdateCheckSource
import com.jeff_media.updatechecker.UpdateChecker
import com.jeff_media.updatechecker.UserAgentBuilder
import dev.furq.vinyls.commands.VinylsCommand
import dev.furq.vinyls.listeners.DiscUsageListener
import dev.furq.vinyls.listeners.InventoryUpdateListener
import dev.furq.vinyls.utils.ResourcePackGenerator
import dev.furq.vinyls.utils.TabCompleter
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Vinyls : JavaPlugin() {

    companion object {
        lateinit var messages: YamlConfiguration
        lateinit var discs: YamlConfiguration
        val prefix = messages.getString("prefix", "&9Vinyls &6»")
    }

    private lateinit var discUsageListener: DiscUsageListener
    private lateinit var inventoryUpdateListener: InventoryUpdateListener

    override fun onEnable() {
        discUsageListener = DiscUsageListener(this)
        inventoryUpdateListener = InventoryUpdateListener(this)
        server.pluginManager.registerEvents(discUsageListener, this)
        server.pluginManager.registerEvents(inventoryUpdateListener, this)
        CustomBlockData.registerListener(this)

        loadConfig()

        getCommand("vinyls")?.setExecutor(VinylsCommand(this))
        getCommand("vinyls")?.tabCompleter = TabCompleter()

        logger.info("Thank you for using my plugin - Furq")

        val config = this.config
        if (config.getBoolean("update-checker")) updateChecker()
    }

    fun loadConfig() {
        try {
            saveDefaultConfig()
            reloadConfig()

            val messagesConfigFile = File(dataFolder, "messages.yml")
            if (!messagesConfigFile.exists()) saveResource("messages.yml", false)
            messages = YamlConfiguration.loadConfiguration(messagesConfigFile)

            val discsConfigFile = File(dataFolder, "discs.yml")
            if (!discsConfigFile.exists()) saveResource("discs.yml", false)
            discs = YamlConfiguration.loadConfiguration(discsConfigFile)

            val sourceFolder = File(dataFolder, "source_files")
            if (!sourceFolder.exists()) sourceFolder.mkdirs()
            val targetFolder = File(dataFolder, "resource_pack")
            if (!targetFolder.exists()) targetFolder.mkdirs()
            ResourcePackGenerator(logger).generateResourcePack(
                discs,
                sourceFolder,
                targetFolder
            )
        } catch (e: Exception) {
            logger.info("An error occurred while loading configuration\n$e")
        }
    }

    private fun updateChecker() {
        UpdateChecker(this, UpdateCheckSource.SPIGOT, "117674")
            .setDownloadLink("https://modrinth.com/plugin/vinyls")
            .setNotifyOpsOnJoin(true)
            .setUserAgent(UserAgentBuilder().addPluginNameAndVersion())
            .checkNow()
    }

    fun getMessage(key: String): String {
        return messages.getString(key, "Message not found")!!
    }
}