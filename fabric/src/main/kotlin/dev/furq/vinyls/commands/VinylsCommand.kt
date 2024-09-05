package dev.furq.vinyls.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.furq.vinyls.Vinyls
import dev.furq.vinyls.utils.InventoryUpdater
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
//? if >=1.20.6 {
/*import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.component.type.CustomModelDataComponent
import net.minecraft.component.type.LoreComponent
import net.minecraft.nbt.NbtCompound
*///?}
//? if =1.20.4 {
/*import net.minecraft.text.TextCodecs
import com.mojang.serialization.JsonOps
*///?}
//? if <=1.20.4 {
import net.minecraft.nbt.NbtString
import net.minecraft.nbt.NbtList
//?}
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.*

class VinylsCommand(private val mod: Vinyls) {

    private val discsConfigFile = File("config/${mod.modID}/discs.yml")
    private val messageConfigFile = File("config/${mod.modID}/messages.yml")
    private var discsConfig = loadYamlConfig(discsConfigFile)
    private var messageConfig = loadYamlConfig(messageConfigFile)
    private val prefix = messageConfig["prefix"]?.toString() ?: "§9Vinyls §6»"

    init {
        CommandRegistrationCallback.EVENT.register { dispatcher: CommandDispatcher<ServerCommandSource>, _: CommandRegistryAccess?, _: CommandManager.RegistrationEnvironment? ->
            dispatcher.register(
                CommandManager.literal("vinyls")
                    .then(CommandManager.literal("reload")
                        .executes {
                            handleReload(it.source)
                            1
                        })
                    .then(CommandManager.literal("give")
                        .then(CommandManager.argument("disc_name", StringArgumentType.string())
                            .suggests { _, builder ->
                                suggestDiscs(builder)
                            }
                            .executes { context ->
                                handleGiveDisc(
                                    context.source,
                                    StringArgumentType.getString(context, "discName")
                                )
                                1
                            }
                            .then(CommandManager.argument("player", StringArgumentType.string())
                                .suggests { context, builder ->
                                    suggestPlayers(context, builder)
                                }
                                .executes { context ->
                                    handleGiveDisc(
                                        context.source,
                                        StringArgumentType.getString(context, "discName"),
                                        StringArgumentType.getString(context, "player")
                                    )
                                    1
                                })
                        )
                    )
                    .executes { context ->
                        context.source.sendError(Text.literal("$prefix ${messageConfig["command-unknown"]}"))
                        0
                    }
            )
        }
    }

    private fun handleReload(source: ServerCommandSource) {
        mod.loadConfig()
        discsConfig = loadYamlConfig(discsConfigFile)
        source.server.worlds.forEach {
            it.players.forEach { player ->
                InventoryUpdater.updatePlayerInventory(player.inventory)
            }
        }
        source.sendMessage(Text.literal("$prefix Reloaded Vinyls successfully!"))
    }

    private fun handleGiveDisc(source: ServerCommandSource, discName: String, playerName: String? = null) {
        val discConfig = discsConfig["discs"] as Map<String, Map<String, Any>>?

        if (discConfig.isNullOrEmpty()) {
            return source.sendError(Text.literal("$prefix ${messageConfig["discs-not-found"]}"))
        }

        val discDetails = discConfig[discName]
            ?: return source.sendError(Text.literal("$prefix ${messageConfig["disc-not-found"]}"))

        //? if <1.21 {
        val material = Registries.ITEM[Identifier(discDetails["material"].toString().lowercase())]
        //?} else {
        /*val material = Registries.ITEM[Identifier.ofVanilla(discDetails["material"].toString().lowercase())]
        *///?}

        val customModelData = discDetails["custom_model_data"] as Int
        val displayName = discDetails["display_name"] as String
        val lore = discDetails["lore"] as List<String>

        val discItem = ItemStack(material).apply {
            //? if <1.20.4 {
            val displayLore = NbtList()
            lore.map { Text.literal(it.replace("&", "§")) }
                .map { Text.Serializer.toJson(it) }
                .map { NbtString.of(it) }
                .forEach { displayLore.add(it) }
            //?} elif =1.20.4 {
            /*val displayLore = NbtList()
            val jsonOps = JsonOps.INSTANCE
            lore?.map { Text.literal(it.replace("&", "§")) }
                ?.map { TextCodecs.STRINGIFIED_CODEC.encodeStart(jsonOps, it).resultOrPartial { e -> throw RuntimeException(e) }.get() }
                ?.map { NbtString.of(it.asString) }
                ?.forEach { displayLore.add(it) }
            *///?} else {
            /*val loreTextComponents: List<Text> = lore.map { Text.literal(it.replace("&", "§")) }
            *///?}

            //? if <1.20.6 {
            setCustomName(Text.literal(displayName.replace("&", "§")))
            getOrCreateSubNbt("display").put("Lore", displayLore)
            orCreateNbt.putInt("CustomModelData", customModelData)
            orCreateNbt.putString("${mod.modID}:music_disc", discName)
            orCreateNbt.putString("${mod.modID}:unique_id", UUID.randomUUID().toString())
            //?} else {
            /*set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName.replace("&", "§")))
            val nbt = get(DataComponentTypes.CUSTOM_DATA)?.copyNbt() ?: NbtCompound().apply {
                putString("${mod.modID}:music_disc", discName)
            }
            set(DataComponentTypes.LORE, LoreComponent(loreTextComponents))
            set(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent(customModelData))
            set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt))
            set(DataComponentTypes.MAX_STACK_SIZE, 1)
            *///?}
        }

        val targetPlayer = playerName?.let { source.server.playerManager.getPlayer(it) } ?: source.player

        if (targetPlayer != null) {
            targetPlayer.inventory.insertStack(discItem)
            source.sendMessage(
                Text.literal(
                    "$prefix ${
                        messageConfig["disc-given"]?.toString()
                            ?.replace("{player}", targetPlayer.name.string)
                            ?.replace("{discName}", discName)
                    }"
                )
            )
            targetPlayer.sendMessage(
                Text.literal(
                    "$prefix ${
                        messageConfig["disc-received"]?.toString()
                            ?.replace("{discName}", discName)
                    }"
                )
            )
        } else {
            source.sendError(Text.literal("$prefix ${messageConfig["player-not-found"]}"))
        }
    }

    private fun suggestDiscs(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val discConfig = discsConfig["discs"] as Map<String, Map<String, Any>>?
        val discNames = discConfig?.keys?.toList() ?: emptyList()
        return suggest(builder, discNames)
    }

    private fun suggestPlayers(
        context: CommandContext<ServerCommandSource>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val server = context.source.server
        val input = builder.remaining.lowercase(Locale.getDefault())
        val playerNames = server.playerManager.playerList
            .map { it.name.string }
            .filter { it.lowercase(Locale.getDefault()).startsWith(input) }
            .toList()
        return suggest(builder, playerNames)
    }

    private fun suggest(builder: SuggestionsBuilder, suggestions: List<String>): CompletableFuture<Suggestions> {
        suggestions.forEach { suggestion ->
            builder.suggest(suggestion)
        }
        return builder.buildFuture()
    }

    private fun loadYamlConfig(file: File): Map<String, Any> {
        return if (file.exists()) {
            Yaml().load(file.inputStream()) ?: emptyMap()
        } else {
            emptyMap()
        }
    }
}