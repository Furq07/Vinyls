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
import dev.furq.spindle.Config
import java.util.concurrent.CompletableFuture
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.*

class VinylsCommand(private val mod: Vinyls) {

    private var discsParser = Config.load("discs.yml")
    private var messageParser = Config.load("messages.yml")
    private var prefix = messageParser.getString("prefix", "§9Vinyls §6»")
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
                                    StringArgumentType.getString(context, "disc_name")
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
                                        StringArgumentType.getString(context, "disc_name"),
                                        StringArgumentType.getString(context, "player")
                                    )
                                    1
                                })
                        )
                    )
                    .executes { context ->
                        context.source.sendError(Text.literal("$prefix ${messageParser.getString("command-unknown")}"))
                        0
                    }
            )
        }
    }

    private fun handleReload(source: ServerCommandSource) {
        mod.loadConfig()
        discsParser = Config.load("discs.yml")
        messageParser = Config.load("messages.yml")
        prefix = messageParser.getString("prefix", "&9Vinyls &6»")
        source.server.worlds.forEach {
            it.players.forEach { player ->
                InventoryUpdater.updatePlayerInventory(player.inventory)
            }
        }
        source.sendMessage(Text.literal("$prefix Reloaded Vinyls successfully!"))
    }

    private fun handleGiveDisc(source: ServerCommandSource, discName: String, playerName: String? = null) {
        val discs = discsParser.getMap("discs")
            ?: return source.sendError(Text.literal("$prefix ${messageParser.getString("discs-not-found")}"))
        if (discsParser.getNestedMap(
                discs,
                discName
            ) == null
        ) return source.sendError(Text.literal("$prefix ${messageParser.getString("disc-not-found")}"))
        //? if <1.21 {
        val material = Registries.ITEM[Identifier(discsParser.getString("discs.$discName.material").lowercase())]
        //?} else {
        /*val material = Registries.ITEM[Identifier.ofVanilla(discsParser.getString("discs.$discName.material").lowercase())]
        *///?}

        val customModelData = discsParser.getInt("discs.$discName.custom_model_data")
        val displayName = discsParser.getString("discs.$discName.display_name")
        val lore = discsParser.getList("discs.$discName.lore") as List<String>

        val discItem = ItemStack(material).apply {
            //? if <1.20.4 {
            val displayLore = NbtList()
            lore.map { Text.literal(it) }
                .map { Text.Serializer.toJson(it) }
                .map { NbtString.of(it) }
                .forEach { displayLore.add(it) }
            //?} elif =1.20.4 {
            /*val displayLore = NbtList()
            val jsonOps = JsonOps.INSTANCE
            lore?.map { Text.literal(it) }
                ?.map { TextCodecs.STRINGIFIED_CODEC.encodeStart(jsonOps, it).resultOrPartial { e -> throw RuntimeException(e) }.get() }
                ?.map { NbtString.of(it.asString) }
                ?.forEach { displayLore.add(it) }
            *///?} else {
            /*val loreTextComponents: List<Text> = lore.map { Text.literal(it) }
            *///?}

            //? if <1.20.6 {
            setCustomName(Text.literal(displayName))
            getOrCreateSubNbt("display").put("Lore", displayLore)
            orCreateNbt.putInt("CustomModelData", customModelData)
            orCreateNbt.putString("${mod.modID}:music_disc", discName)
            orCreateNbt.putString("${mod.modID}:unique_id", UUID.randomUUID().toString())
            //?} else {
            /*set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName))
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
                        messageParser.getString("disc-given")
                            ?.replace("{player}", targetPlayer.name.string)
                            ?.replace("{discName}", discName)
                    }"
                )
            )
            targetPlayer.sendMessage(
                Text.literal(
                    "$prefix ${
                        messageParser.getString("disc-received")
                            ?.replace("{discName}", discName)
                    }"
                )
            )
        } else {
            source.sendError(Text.literal("$prefix ${messageParser.getString("player-not-found")}"))
        }
    }

    private fun suggestDiscs(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val discs = discsParser.getMap("discs").keys.toList()
        return suggest(builder, discs)
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
}