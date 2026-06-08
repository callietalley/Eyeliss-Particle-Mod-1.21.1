package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.item.ModItems;
import eyeliss.particle.mod.sound.ModSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleItem.class) // Targets the vanilla BundleItem class
public class VanillaBundleRejectionMixin {

    /**
     * Case A: Cursor holds an item and clicks ONTO a stationary vanilla bundle slot.
     */
    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true)
    private void rejectShadowBundleNesting(ItemStack stack, ItemStack otherStack, net.minecraft.screen.slot.Slot slot, ClickType clickType, PlayerEntity player, net.minecraft.inventory.StackReference cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
        if (otherStack.isOf(ModItems.SHADOW_BUNDLE)) {
            // Trigger failure sound cue directly to the active client profile
            player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(),
                    ModSounds.SHADOW_BUNDLE_INSERT_FAIL_EVENT, SoundCategory.PLAYERS, 0.8F, 1.0F);

            // Cancel execution entirely and mark interaction event as consumed
            cir.setReturnValue(true);
        }
    }

    /**
     * Case B: Cursor holds a vanilla bundle and clicks ONTO a stationary Shadow Bundle item slot.
     */
    @Inject(method = "onStackClicked", at = @At("HEAD"), cancellable = true)
    private void rejectVanillaBundleDropOntoShadowBundle(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        // 'stack' represents the vanilla bundle item held on your active cursor
        // Check if the targeted, stationary item slot contains your custom shadow bundle
        if (slot.hasStack() && slot.getStack().isOf(ModItems.SHADOW_BUNDLE)) {

            // Execute failure rejection audio parameters
            player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(),
                    ModSounds.SHADOW_BUNDLE_INSERT_FAIL_EVENT, SoundCategory.PLAYERS, 0.8F, 1.0F);

            // Kill interaction flow immediately before vanilla bundle container array calculation
            cir.setReturnValue(true);
        }
    }
}