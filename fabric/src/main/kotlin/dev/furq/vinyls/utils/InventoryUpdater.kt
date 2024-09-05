package dev.furq.vinyls.utils

//? if >=1.20.6 {
/*import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.CustomModelDataComponent
import net.minecraft.component.type.LoreComponent
*///?}
//? if <1.20.6 {
import net.minecraft.nbt.NbtCompound
//?}
//? if =1.20.4 {
/*import net.minecraft.text.TextCodecs
import com.mojang.serialization.JsonOps
import com.google.gson.JsonParser
*///?}
//? if <=1.20.4 {
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
//?}
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.yaml.snakeyaml.Yaml
import java.io.File


object InventoryUpdater {
    private val discsConfigFile = File("config/vinyls/discs.yml")
    private var discsConfig = loadYamlConfig(discsConfigFile)

    fun updatePlayerInventory(inventory: PlayerInventory) {
        discsConfig = loadYamlConfig(discsConfigFile)
        for (index in 0 until inventory.size()) {
            val item = inventory.getStack(index)
            if (item.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.contains("minecraft:music_disc") != null) {
                updateItemIfNeeded(item, inventory, index)
            }
        }
    }

    fun updateItemIfNeeded(item: ItemStack, inventory: PlayerInventory, index: Int): ItemStack {
        //? if <1.20.6 {
        if (!item.hasNbt()) return item
        val itemNbt = item.nbt ?: return item

        //?} else {
        /*if (item.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt() == null) return item
        val itemNbt = item.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt() ?: return item
        *///?}

        val discName = itemNbt.getString("vinyls:music_disc") ?: return item
        val discConfig = discsConfig["discs"] as? Map<String, Map<String, Any>> ?: return item
        val discDetails = discConfig[discName] ?: return item

        val itemId = discDetails["material"].toString().lowercase()
        //? if <1.21 {
        val material = Registries.ITEM[Identifier(itemId)]
        //?} else {
        /*val material = Registries.ITEM[Identifier.ofVanilla(itemId)]
        *///?}
        val customModelData = (discDetails["custom_model_data"] ?: 1000) as Int
        val displayName = (discDetails["display_name"] ?: "&bCustom Disc") as String
        val lore = (discDetails["lore"] ?: "Vinyls - Custom") as List<String>

        var updateNeeded = false
        var newItem = item.copy() ?: return item

        if (newItem.item != material) {
            newItem = ItemStack(material, newItem.count)

            //? if <1.20.6 {
            newItem.nbt = item.nbt?.copy() ?: NbtCompound()
            //?} else {
            /*newItem.set(DataComponentTypes.CUSTOM_NAME, item.name)
            newItem.set(DataComponentTypes.LORE, item.get(DataComponentTypes.LORE))
            newItem.set(DataComponentTypes.CUSTOM_MODEL_DATA, item.get(DataComponentTypes.CUSTOM_MODEL_DATA))
            newItem.set(DataComponentTypes.MAX_STACK_SIZE, 1)
            newItem.set(DataComponentTypes.CUSTOM_DATA, item.get(DataComponentTypes.CUSTOM_DATA))
            *///?}
            updateNeeded = true
        }

        //? if <1.20.6 {
        val newNbt = newItem.nbt ?: NbtCompound()
        //?} else {
        //?}
        if (displayName.replace("&", "§") != newItem.name.string) {
            //? if <1.20.6 {
            newItem.setCustomName(Text.literal(displayName.replace("&", "§")))
            //?} else {
            /*newItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName.replace("&", "§")))
            *///?}
            updateNeeded = true
        }


        //? if <1.20.6 {
        if (customModelData != newNbt.getInt("CustomModelData")) {
            //?} else {
            /*if (CustomModelDataComponent(customModelData) != newItem.get(DataComponentTypes.CUSTOM_MODEL_DATA)) {
            *///?}
            //? if <1.20.6 {
            newNbt.putInt("CustomModelData", customModelData)
            //?} else {
            /*newItem.set(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent(customModelData))
            *///?}
            updateNeeded = true
        }

        //? if <1.20.4 {
        if (lore != newItem.nbt?.getList("Lore", 8)?.map { Text.Serializer.fromJson(it.asString()) }) {
            //?} elif =1.20.4 {
            /*val jsonOps = JsonOps.INSTANCE
            if (lore != newNbt.getList("Lore", 8)?.map { TextCodecs.STRINGIFIED_CODEC.decode(jsonOps, JsonParser.parseString(it.asString())).resultOrPartial { e -> throw RuntimeException(e) }.get() }) {
            *///?} else {
            /*if (newItem.get(DataComponentTypes.LORE) != LoreComponent(lore.map { Text.literal(it.replace("&", "§")) })) {
            *///?}
            //? if <1.20.4 {
            val displayLore = NbtList()
            lore.map { Text.literal(it.replace("&", "§")) }
                .map { Text.Serializer.toJson(it) }
                .map { NbtString.of(it) }
                .forEach { displayLore.add(it) }
            newNbt.getCompound("display").put("Lore", displayLore)
            //?} elif =1.20.4 {
            /*val displayLore = NbtList()
            val jsonOps = JsonOps.INSTANCE
            lore.map { Text.literal(it.replace("&", "§")) }
                .map { TextCodecs.STRINGIFIED_CODEC.encodeStart(jsonOps, it).resultOrPartial { e -> throw RuntimeException(e) }.get() }
                .map { NbtString.of(it.asString) }
                .forEach { displayLore.add(it) }
            newNbt.getCompound("display").put("Lore", displayLore)
            *///?} else {
            /*val loreTextComponents: List<Text> = lore.map { Text.literal(it.replace("&", "§")) }
            newItem.set(DataComponentTypes.LORE, LoreComponent(loreTextComponents))
            *///?}
            updateNeeded = true
        }

        if (updateNeeded) {
            //? if <1.20.6 {
            newItem.nbt = newNbt
            //?}
            inventory.setStack(index, newItem)
        }
        return newItem
    }

    private fun loadYamlConfig(file: File): Map<String, Any> {
        return if (file.exists()) {
            Yaml().load(file.inputStream()) ?: emptyMap()
        } else {
            emptyMap()
        }
    }
}