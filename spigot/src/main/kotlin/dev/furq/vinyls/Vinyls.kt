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
    private lateinit var discUsageListener: DiscUsageListener
    private lateinit var inventoryUpdateListener: InventoryUpdateListener
    private lateinit var messagesConfig: YamlConfiguration

    override fun onEnable() {
        discUsageListener = DiscUsageListener(this)
        inventoryUpdateListener = InventoryUpdateListener(this)
        server.pluginManager.registerEvents(discUsageListener, this)
        server.pluginManager.registerEvents(inventoryUpdateListener, this)
        CustomBlockData.registerListener(this)

        saveDefaultConfig()
        reloadConfig()

        val messagesConfigFile = File(dataFolder, "messages.yml")
        if (!messagesConfigFile.exists()) saveResource("messages.yml", false)
        messagesConfig = YamlConfiguration.loadConfiguration(messagesConfigFile)

        val discsConfigFile = File(dataFolder, "discs.yml")
        if (!discsConfigFile.exists()) saveResource("discs.yml", false)
        val discsConfig = YamlConfiguration.loadConfiguration(discsConfigFile)

        val sourceFolder = File(dataFolder, "source_files")
        if (!sourceFolder.exists()) sourceFolder.mkdirs()
        val targetFolder = File(dataFolder, "resource_pack")
        if (!targetFolder.exists()) targetFolder.mkdirs()

        ResourcePackGenerator(this).generateResourcePack(discsConfig, sourceFolder, targetFolder)

        getCommand("vinyls")?.setExecutor(VinylsCommand(this))
        getCommand("vinyls")?.tabCompleter = TabCompleter(this)

        logger.info("Thank you for using my plugin - Furq")

        val config = this.config
        if (config.getBoolean("update-checker")) updateChecker()
    }

    private fun updateChecker() {
        UpdateChecker(this, UpdateCheckSource.SPIGOT, "117674")
            .setDownloadLink("https://modrinth.com/plugin/vinyls")
            .setNotifyOpsOnJoin(true)
            .setUserAgent(UserAgentBuilder().addPluginNameAndVersion())
            .checkNow()
    }

    fun getMessage(key: String): String {
        return messagesConfig.getString(key, "Message not found")!!
    }
}