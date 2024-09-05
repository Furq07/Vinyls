package dev.furq.vinyls.mixin;

import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if <1.20.4 {
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
//?}

//? if <1.20.6 {
import net.minecraft.nbt.NbtCompound;
//?}
//? if <1.21 {
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//?}
//? if >=1.20.6 {
/*import net.minecraft.component.DataComponentTypes;
import org.jetbrains.annotations.Nullable;
import net.minecraft.component.type.NbtComponent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}


@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin {

    //? if <1.20.4 {
    @Final
    @Shadow
    private DefaultedList<ItemStack> inventory;
    //?} else {
    /*@Shadow
    private ItemStack recordStack;
    *///?}

    //? if <1.21 {
    @Shadow
    protected abstract void updateState(Entity entity, boolean hasRecord);

    @Shadow
    public abstract void startPlaying();
    //?}

    //? if >=1.21 {
    /*@Shadow protected abstract void onRecordStackChanged(boolean par1);
     *///?}


    @Inject(method = "setStack", at = @At("HEAD"))
    //? if <1.20.4 {
    private void onSetStack(int slot, ItemStack stack, CallbackInfo ci) {
        //?} else {
        /*private void onSetStack(ItemStack stack, CallbackInfo ci) {
         *///?}
        //? if <1.20.6 {
        NbtCompound nbt = stack.getNbt();
        //?} else {
        /*@Nullable NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
         *///?}
        if (nbt != null && nbt.contains("vinyls:music_disc")) {
            //? if <1.20.4 {
            this.inventory.set(slot, stack);
            //?} else {
            /*this.recordStack = stack;
             *///?}
            //? if >=1.21 {
            /*this.onRecordStackChanged(true);
             *///?} else {
            this.updateState(null, true);
            this.startPlaying();
            //?}
        }
    }

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    public void isValid(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        //? if <1.20.6 {
        NbtCompound nbt = stack.getNbt();
        //?} else {
        /*@Nullable NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
         *///?}
        cir.setReturnValue(nbt != null && nbt.contains("vinyls:music_disc"));
    }
}