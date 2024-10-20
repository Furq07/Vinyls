package dev.furq.vinyls.utils

import net.minecraft.block.entity.JukeboxBlockEntity
//? if >=1.20.6 {
/*import net.minecraft.component.DataComponentTypes
*///?}
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

object DiscUsageManager {

    fun playCustomMusic(
        disc: ItemStack,
        jukeboxEntity: JukeboxBlockEntity,
        player: PlayerEntity,
    ) {
        val pos = jukeboxEntity.pos
        val world = jukeboxEntity.world!!
        val box = Box(pos).expand(32.0, 32.0, 32.0)
        val vec3d = Vec3d.ofCenter(pos)
        //? if <1.20.6 {
        val discID = disc.nbt?.getString("vinyls:music_disc") ?: return

        //?} else {
        /*val discID = disc.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.getString("vinyls:music_disc") ?: return
        *///?}
        val soundId = Identifier.of("minecraft", "vinyls.$discID")
        val soundEvent = SoundEvent.of(soundId) ?: return
        player.sendMessage(Text.literal("§bNow Playing: ${disc.name.string}"), true)
        world.getEntitiesByClass(ServerPlayerEntity::class.java, box) { true }.forEach { p ->
            p.networkHandler.sendPacket(
                PlaySoundS2CPacket(
                    RegistryEntry.of(soundEvent),
                    SoundCategory.RECORDS,
                    vec3d.x,
                    vec3d.y,
                    vec3d.z,
                    1.0f,
                    1.0f,
                    world.random.nextLong()
                )
            )
        }
    }

    fun stopCustomMusic(disc: ItemStack, jukeboxEntity: JukeboxBlockEntity) {
        //? if <1.20.6 {
        val discID = disc.nbt?.getString("vinyls:music_disc") ?: return

        //?} else {
        /*val discID = disc.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.getString("vinyls:music_disc") ?: return
        *///?}
        val soundId = Identifier.of("minecraft", "vinyls.$discID")
        val pos = jukeboxEntity.pos
        val box = Box(pos).expand(32.0, 32.0, 32.0)
        val world = jukeboxEntity.world!!
        for (player in world.players)
            world.getEntitiesByClass(ServerPlayerEntity::class.java, box) { true }.forEach { p ->
                p.networkHandler.sendPacket(StopSoundS2CPacket(soundId, SoundCategory.RECORDS))
            }
    }
}
