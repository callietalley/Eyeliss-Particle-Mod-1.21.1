package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxSlot.class)
public class ShulkerBoxRejectionMixin {

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void preventShadowBundleInsertion(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isOf(ModItems.SHADOW_BUNDLE)) {

            cir.setReturnValue(false);
        }
    }
}