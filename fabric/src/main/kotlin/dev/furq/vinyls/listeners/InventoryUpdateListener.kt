package dev.furq.vinyls.listeners

import dev.furq.vinyls.utils.InventoryUpdater
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.network.ServerPlayerEntity


class InventoryUpdateListener {

    init {
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            handlePlayerJoin(handler.player)
        }
    }

    private fun handlePlayerJoin(player: ServerPlayerEntity) {
        InventoryUpdater.updatePlayerInventory(player.inventory)
    }
}