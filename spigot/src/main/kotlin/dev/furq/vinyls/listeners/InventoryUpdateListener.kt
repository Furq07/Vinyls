package dev.furq.vinyls.listeners

import dev.furq.vinyls.Vinyls
import dev.furq.vinyls.utils.InventoryUpdater
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent


class InventoryUpdateListener(private val plugin: Vinyls) : Listener {

    @EventHandler
    fun PlayerJoinEvent.onPlayerJoin() {
        InventoryUpdater(plugin).updatePlayerInventory(player.inventory)
    }

    @EventHandler
    fun PlayerItemHeldEvent.onItemHeld() {
        val item = player.inventory.getItem(newSlot)
        InventoryUpdater(plugin).updateItemIfNeeded(item, player.inventory, newSlot)
    }
}