package dev.furq.vinyls.utils

import dev.furq.vinyls.Vinyls
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TabCompleter(private val plugin: Vinyls) : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>,
    ): List<String>? {
        if (command.name.equals("vinyls", ignoreCase = true)) {
            if (args.size == 1) {
                return listOf("reload", "give")
            } else if (args.size == 2) {
                when (args[0].lowercase()) {
                    "give" -> {
                        val discsConfigFile = File(plugin.dataFolder, "discs.yml")
                        if (!discsConfigFile.exists()) plugin.saveResource("discs.yml", false)
                        val discsConfig = YamlConfiguration.loadConfiguration(discsConfigFile)
                        val discs = discsConfig.getConfigurationSection("discs")?.getKeys(false)!!
                        return discs.toList()
                    }

                    else -> return null
                }
            }
        }
        return null
    }
}