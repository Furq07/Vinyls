package dev.furq.vinyls

import com.jeff_media.customblockdata.CustomBlockData
import dev.furq.vinyls.commands.VinylsCommand
import dev.furq.vinyls.listeners.DiscUsageListener
import dev.furq.vinyls.tabcompleter.TabCompleter
import dev.furq.vinyls.utils.ResourcePackGenerator
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker
import com.jeff_media.updatechecker.UserAgentBuilder
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Vinyls : JavaPlugin() {
    private lateinit var discUsageListener: DiscUsageListener

    override fun onEnable() {
        discUsageListener = DiscUsageListener(this)
        server.pluginManager.registerEvents(discUsageListener, this)
        CustomBlockData.registerListener(this)

        saveDefaultConfig()
        reloadConfig()

        val config = this.config
        val discsConfigFile = File(dataFolder, "discs.yml")
        if (!discsConfigFile.exists()) saveResource("discs.yml", false)
        val discsConfig = YamlConfiguration.loadConfiguration(discsConfigFile)
        val sourceFolder = File(dataFolder, "source_files")
        val targetFolder = File(dataFolder, "resource_pack")
        if (!sourceFolder.exists()) sourceFolder.mkdirs()
        if (!targetFolder.exists()) targetFolder.mkdirs()
        ResourcePackGenerator(this).generateResourcePack(discsConfig, sourceFolder, targetFolder)

        getCommand("vinyls")?.setExecutor(VinylsCommand(this))
        getCommand("vinyls")?.tabCompleter = TabCompleter(this)

        logger.info("Thank you for using my plugin - Furq")

        if (config.getBoolean("update-checker")) updateChecker()
    }

    private fun updateChecker() {
        UpdateChecker(this, UpdateCheckSource.SPIGOT, "117674")
            .setDownloadLink("https://modrinth.com/plugin/vinyls")
            .setDonationLink("buymeacoffee.com/furq")
            .setNotifyOpsOnJoin(true)
            .setUserAgent(UserAgentBuilder().addPluginNameAndVersion())
            .checkNow()
    }
}