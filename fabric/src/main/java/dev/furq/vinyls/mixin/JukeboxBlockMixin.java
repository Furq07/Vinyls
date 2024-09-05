package dev.furq.vinyls.mixin;

import dev.furq.vinyls.utils.DiscUsageManager;
import dev.furq.vinyls.utils.InventoryUpdater;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if <1.21 {
import net.minecraft.item.MusicDiscItem;
 //?}
//? if >=1.20.6 {
/*import net.minecraft.component.DataComponentTypes;
import org.jetbrains.annotations.Nullable;
*///?}

@Mixin(JukeboxBlock.class)
public class JukeboxBlockMixin {

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    //? if <1.20.6 {
    public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
     //?} else {
    /*public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        Hand hand = player.getActiveHand();
        *///?}
        if (hand != Hand.MAIN_HAND) {
            cir.setReturnValue(ActionResult.PASS);
            return;
        }
        JukeboxBlockEntity blockEntity = (JukeboxBlockEntity) world.getBlockEntity(pos);
        if (blockEntity == null) return;

        //? if <1.21 {
        if (blockEntity.isPlayingRecord()) {
        //?} else {
        /*BooleanProperty hasRecordProperty = JukeboxBlock.HAS_RECORD;
        boolean hasRecord = state.get(hasRecordProperty);
        if (hasRecord) {
        *///?}
            DiscUsageManager.INSTANCE.stopCustomMusic(blockEntity.getStack(0), blockEntity);
            return;
        }

        ItemStack disc = player.getStackInHand(hand);
        //? if <1.21 {
        if (disc.isEmpty() || (disc.getItem() instanceof MusicDiscItem)) return;
         //?} else {
        /*if (disc.isEmpty() || (disc.contains(DataComponentTypes.JUKEBOX_PLAYABLE))) return;
        *///?}

        //? if <1.20.6 {
        NbtCompound nbt = disc.getNbt();
         //?} else {
        /*@Nullable NbtCompound nbt = disc.get(DataComponentTypes.CUSTOM_DATA) != null ? disc.get(DataComponentTypes.CUSTOM_DATA).copyNbt() : null;
        *///?}

        if (nbt != null && nbt.contains("vinyls:music_disc")) {
            DiscUsageManager.INSTANCE.playCustomMusic(disc, blockEntity, player);
            disc = InventoryUpdater.INSTANCE.updateItemIfNeeded(disc, player.getInventory(), player.getInventory().selectedSlot);
            blockEntity.setStack(0, disc);
            player.setStackInHand(hand, ItemStack.EMPTY);
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Inject(method = "onStateReplaced", at = @At("HEAD"))
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        JukeboxBlockEntity blockEntity = (JukeboxBlockEntity) world.getBlockEntity(pos);
        if (blockEntity == null) return;
        //? if <1.21 {
        if (blockEntity.isPlayingRecord()) {
         //?} else {
        /*BooleanProperty hasRecordProperty = JukeboxBlock.HAS_RECORD;
        boolean hasRecord = state.get(hasRecordProperty);
        if (hasRecord) {
        *///?}
            DiscUsageManager.INSTANCE.stopCustomMusic(blockEntity.getStack(0), blockEntity);
            blockEntity.dropRecord();
        }
    }
}