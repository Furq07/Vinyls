package dev.furq.vinyls.commands

import dev.furq.vinyls.Vinyls
import dev.furq.vinyls.utils.ResourcePackGenerator
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.File
import java.util.UUID

class VinylsCommand(private val plugin: Vinyls) : CommandExecutor {

    private val discsConfigFile = File(plugin.dataFolder, "discs.yml")
    private var discsConfig = YamlConfiguration.loadConfiguration(discsConfigFile)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.label.equals("vinyls", ignoreCase = true) && args.isNotEmpty()) {
            when (args[0].lowercase()) {
                "reload" -> handleReload(sender)
                "givedisc" -> handleGiveDisc(sender, args)
                else -> sender.sendMessage("§cUnknown subcommand.")
            }
        }
        return true
    }

    private fun handleReload(sender: CommandSender) {
        plugin.reloadConfig()
        discsConfig = YamlConfiguration.loadConfiguration(discsConfigFile)
        val sourceFolder = File(plugin.dataFolder, "source_files")
        val targetFolder = File(plugin.dataFolder, "resource_pack")
        if (!sourceFolder.exists()) sourceFolder.mkdirs()
        if (!targetFolder.exists()) targetFolder.mkdirs()
        ResourcePackGenerator(plugin).generateResourcePack(discsConfig, sourceFolder, targetFolder)
        sender.sendMessage("§aReloaded Vinyls successfully!")
    }

    private fun handleGiveDisc(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage("§cUsage: /vinyls givedisc <discName> [player]")
            return
        }
        val discName = args[1]
        val discs = discsConfig.getConfigurationSection("discs")?.getKeys(false) ?: return sender.sendMessage("§cNo discs found in config.")

        if (discName !in discs) {
            sender.sendMessage("§cThis item does not exist.")
            return
        }

        val discConfig = discsConfig.getConfigurationSection("discs.$discName")!!
        val material = discConfig.getString("material")?.let { Material.valueOf(it) }
        if (material == null) {
            sender.sendMessage("§cInvalid material for disc.")
            return
        }

        val customModelData = discConfig.getInt("custom_model_data")
        val displayName = discConfig.getString("display_name")
        val lore = discConfig.getStringList("lore")

        val discItem = ItemStack(material, 1).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName!!))
                setCustomModelData(customModelData)
                val colorLore = lore.map { ChatColor.translateAlternateColorCodes('&', it) }
                this.lore = colorLore
                persistentDataContainer.set(NamespacedKey(plugin, "music_disc"), PersistentDataType.STRING, discName)
                persistentDataContainer.set(NamespacedKey(plugin, "unique_id"), PersistentDataType.STRING, UUID.randomUUID().toString())
            }
        }

        val targetPlayer: Player? = when {
            args.size >= 3 -> Bukkit.getPlayer(args[2])
            sender is Player -> sender
            else -> null
        }

        if (targetPlayer != null) {
            targetPlayer.inventory.addItem(discItem)
            sender.sendMessage("§aGave ${targetPlayer.name} the disc $discName.")
            targetPlayer.sendMessage("§aYou have received the disc $discName.")
        } else {
            sender.sendMessage("§cPlayer not found or not specified.")
        }
    }
}