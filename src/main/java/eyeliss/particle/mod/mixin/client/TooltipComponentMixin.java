package eyeliss.particle.mod.mixin.client;

import eyeliss.particle.mod.client.tooltip.ShadowBundleTooltipData;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayList;
import java.util.List;

@Mixin(TooltipComponent.class)
public interface TooltipComponentMixin {
    @Inject(method = "of(Lnet/minecraft/item/tooltip/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;", at = @At("HEAD"), cancellable = true)
    private static void registerShadowBundleTooltip(TooltipData data, CallbackInfoReturnable<TooltipComponent> cir) {
        if (data instanceof ShadowBundleTooltipData shadowData) {
            BundleContentsComponent originalContents = shadowData.contents();
            List<ItemStack> visualCombinedList = new ArrayList<>();

            for (ItemStack realStack : originalContents.stream().toList()) {
                boolean visuallyMerged = false;
                for (ItemStack existingVisualStack : visualCombinedList) {
                    if (ItemStack.areItemsAndComponentsEqual(existingVisualStack, realStack)) {
                        existingVisualStack.increment(realStack.getCount());
                        visuallyMerged = true;
                        break;
                    }
                }
                if (!visuallyMerged) {
                    visualCombinedList.add(realStack.copy());
                }
            }

            cir.setReturnValue(new BundleTooltipComponent(new BundleContentsComponent(visualCombinedList)));
        }
    }
}