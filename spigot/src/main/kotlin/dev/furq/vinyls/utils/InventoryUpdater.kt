package dev.furq.vinyls.utils

import dev.furq.vinyls.Vinyls
import dev.furq.vinyls.Vinyls.Companion.discs
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.persistence.PersistentDataType

class InventoryUpdater(private val plugin: Vinyls) {

    fun updatePlayerInventory(inventory: PlayerInventory) {
        inventory.contents.filterNotNull().forEachIndexed { index, item ->
            if (isMusicDisc(item)) {
                updateItemIfNeeded(item, inventory, index)
            }
        }
    }

    fun updateItemIfNeeded(item: ItemStack?, inventory: PlayerInventory, index: Int) {
        val itemMeta = item?.itemMeta ?: return
        val pdc = itemMeta.persistentDataContainer
        val discID = pdc.get(NamespacedKey(plugin, "music_disc"), PersistentDataType.STRING) ?: return
        val discConfig = discs.getConfigurationSection("discs.$discID") ?: return
        val material = discConfig.getString("material")?.let { Material.valueOf(it) } ?: return
        val customModelData = discConfig.getInt("custom_model_data")
        val displayName = discConfig.getString("display_name")?.let { ChatColor.translateAlternateColorCodes('&', it) }
        val lore = discConfig.getStringList("lore").map { ChatColor.translateAlternateColorCodes('&', it) }

        var updateNeeded = false

        if (item.type != material) {
            item.type = material
            updateNeeded = true
        }

        if (displayName != itemMeta.displayName) {
            itemMeta.setDisplayName(displayName)
            updateNeeded = true
        }

        if (customModelData != itemMeta.customModelData) {
            itemMeta.setCustomModelData(customModelData)
            updateNeeded = true
        }

        if (lore != itemMeta.lore) {
            itemMeta.lore = lore
            updateNeeded = true
        }

        if (updateNeeded) {
            item.itemMeta = itemMeta
            inventory.setItem(index, item)
        }
    }

    private fun isMusicDisc(item: ItemStack): Boolean {
        val pdc = item.itemMeta?.persistentDataContainer
        return pdc?.has(NamespacedKey(plugin, "music_disc"), PersistentDataType.STRING) == true
    }
}