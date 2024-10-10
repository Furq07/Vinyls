package dev.furq.vinyls.utils

import dev.furq.vinyls.Vinyls.Companion.discs
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TabCompleter : TabCompleter {
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
                        val discs = discs.getConfigurationSection("discs")?.getKeys(false)!!
                        return discs.toList()
                    }

                    else -> return null
                }
            }
        }
        return null
    }
}