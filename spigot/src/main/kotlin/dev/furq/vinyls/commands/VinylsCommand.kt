package dev.furq.vinyls.commands

import dev.furq.vinyls.Vinyls
import dev.furq.vinyls.Vinyls.Companion.discs
import dev.furq.vinyls.Vinyls.Companion.prefix
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class VinylsCommand(private val plugin: Vinyls) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.label.equals("vinyls", ignoreCase = true) && args.isNotEmpty()) {
            when (args[0].lowercase()) {
                "reload" -> handleReload(sender)
                "give" -> handleGiveDisc(sender, args)
                else -> sender.sendMessage("$prefix ${plugin.getMessage("command-unknown")}")
            }
        }
        return true
    }

    private fun handleReload(sender: CommandSender) {
        plugin.loadConfig()
        sender.sendMessage("$prefix §7Reloaded Vinyls successfully!")
    }

    private fun handleGiveDisc(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage("$prefix §7Usage: /vinyls give <discName> [player]")
            return
        }
        val discName = args[1]
        val discsMap = discs.getConfigurationSection("discs")?.getKeys(false)
            ?: return sender.sendMessage("$prefix ${plugin.getMessage("discs-not-found")}")

        if (discName !in discsMap) {
            sender.sendMessage("$prefix ${plugin.getMessage("disc-not-found")}")
            return
        }

        val discConfig = discs.getConfigurationSection("discs.$discName")!!

        val material = discConfig.getString("material")?.let { Material.valueOf(it) }
            ?: return sender.sendMessage("$prefix ${plugin.getMessage("material-invalid")}")
        val customModelData = discConfig.getInt("custom_model_data")
        val displayName = discConfig.getString("display_name")
        val lore = discConfig.getStringList("lore").map { ChatColor.translateAlternateColorCodes('&', it) }
        val discItem = ItemStack(material, 1).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName!!))
                setCustomModelData(customModelData)
                setLore(lore)
                persistentDataContainer.set(NamespacedKey(plugin, "music_disc"), PersistentDataType.STRING, discName)
                persistentDataContainer.set(
                    NamespacedKey(plugin, "unique_id"),
                    PersistentDataType.STRING,
                    UUID.randomUUID().toString()
                )
            }
        }

        val targetPlayer: Player? = when {
            args.size >= 3 -> Bukkit.getPlayer(args[2])
            sender is Player -> sender
            else -> null
        }

        if (targetPlayer != null) {
            targetPlayer.inventory.addItem(discItem)
            sender.sendMessage(
                "$prefix ${
                    plugin.getMessage("disc-given").replace("{player}", targetPlayer.name)
                        .replace("{discName}", discName)
                }"
            )
            targetPlayer.sendMessage("$prefix ${plugin.getMessage("disc-received").replace("{discName}", discName)}")
        } else {
            sender.sendMessage("$prefix ${plugin.getMessage("player-not-found")}")
        }
    }
}