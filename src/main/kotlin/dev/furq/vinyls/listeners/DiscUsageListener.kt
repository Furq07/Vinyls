package dev.furq.vinyls.listeners

import com.jeff_media.customblockdata.CustomBlockData
import com.jeff_media.morepersistentdatatypes.DataType
import dev.furq.vinyls.Vinyls
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.SoundCategory
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class DiscUsageListener(private val plugin: Vinyls) : Listener {

    private val discKey = NamespacedKey(plugin, "music_disc")

    @EventHandler
    fun handleDiscInteract(event: PlayerInteractEvent) {
        val player = event.player
        val block = event.clickedBlock
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (block?.type != Material.JUKEBOX) return
        if (event.hand != EquipmentSlot.HAND) return

        val pdc: PersistentDataContainer = CustomBlockData(block, plugin)
        if (!pdc.has(discKey, DataType.ITEM_STACK) && player.inventory.itemInMainHand.type != Material.AIR) {
            playCustomDisc(block, player.inventory.itemInMainHand, player)
        } else {
            stopCustomDisc(block)
            player.swingMainHand()
        }
    }

    @EventHandler
    fun handleBlockBreak(event: BlockBreakEvent) {
        if (event.block.type != Material.JUKEBOX) return
        val pdc: PersistentDataContainer = CustomBlockData(event.block, plugin)
        if (pdc.has(discKey, DataType.ITEM_STACK)) {
            stopCustomDisc(event.block)
        }
    }

    @EventHandler
    fun handleBlockExplode(event: EntityExplodeEvent) {
        event.blockList().forEach { block ->
            if (block.type == Material.JUKEBOX) {
                val pdc: PersistentDataContainer = CustomBlockData(block, plugin)
                if (pdc.has(discKey, DataType.ITEM_STACK)) {
                    stopCustomDisc(block)
                }
            }
        }
    }

    @EventHandler
    fun handleBlockBurn(event: BlockBurnEvent) {
        if (event.block.type != Material.JUKEBOX) return
        val pdc: PersistentDataContainer = CustomBlockData(event.block, plugin)
        if (pdc.has(discKey, DataType.ITEM_STACK)) {
            stopCustomDisc(event.block)
        }
    }

    private fun playCustomDisc(block: Block, disc: ItemStack, player: Player) {
        val discID = disc.itemMeta?.persistentDataContainer?.get(NamespacedKey(plugin, "music_disc"), PersistentDataType.STRING) ?: return
        val pdc: PersistentDataContainer = CustomBlockData(block, plugin)
        val discClone = disc.clone()
        discClone.amount = 1
        disc.amount -= 1
        pdc.set(discKey, DataType.ITEM_STACK, discClone)

        val blockLocation = block.location.add(0.5, 0.5, 0.5)
        block.world.playSound(blockLocation, "vinyls.$discID", SoundCategory.RECORDS, 1.0f, 1.0f)
        player.swingMainHand()
    }

    private fun stopCustomDisc(block: Block) {
        val pdc: PersistentDataContainer = CustomBlockData(block, plugin)
        val disc = pdc.get(discKey, DataType.ITEM_STACK)
        val discID = disc?.itemMeta?.persistentDataContainer?.get(NamespacedKey(plugin, "music_disc"), PersistentDataType.STRING) ?: return

        val blockLocation = block.location.add(0.5, 0.5, 0.5)
        block.world.getNearbyEntities(blockLocation, 32.0, 32.0, 32.0).filterIsInstance<Player>().forEach { player ->
            player.stopSound("vinyls.$discID", SoundCategory.RECORDS)
        }

        block.world.dropItemNaturally(block.location, disc)
        pdc.remove(discKey)
    }
}