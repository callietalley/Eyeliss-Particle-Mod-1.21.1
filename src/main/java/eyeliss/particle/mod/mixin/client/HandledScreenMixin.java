package eyeliss.particle.mod.mixin.client;

import eyeliss.particle.mod.item.ModItems;
import eyeliss.particle.mod.network.ShadowBundleScrollPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.List;

@Mixin(net.minecraft.client.Mouse.class)
public class HandledScreenMixin {

    @Inject(method = "onMouseScroll(JDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDDD)Z"), cancellable = true)
    private void handleShadowBundleScrolling(long window, double horizontalAmount, double verticalAmount, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen currentScreen = client.currentScreen;

        if (currentScreen instanceof HandledScreen<?> handledScreen) {
            Slot focusedSlot = eyeliss_getSlotFromScreen(handledScreen);

            if (focusedSlot != null && focusedSlot.hasStack()) {
                ItemStack bundleStack = focusedSlot.getStack();

                if (bundleStack.isOf(ModItems.SHADOW_BUNDLE)) {
                    BundleContentsComponent contents = bundleStack.get(DataComponentTypes.BUNDLE_CONTENTS);

                    if (contents != null && !contents.isEmpty()) {
                        List<ItemStack> itemArrayList = new ArrayList<>(contents.stream().toList());

                        if (itemArrayList.size() > 1) {
                            boolean isScrollUp = verticalAmount > 0;

                            if (isScrollUp) {
                                ItemStack firstItem = itemArrayList.remove(0);
                                itemArrayList.add(firstItem);
                            } else {
                                ItemStack lastItem = itemArrayList.remove(itemArrayList.size() - 1);
                                itemArrayList.add(0, lastItem);
                            }
                            bundleStack.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(itemArrayList));

                            ClientPlayNetworking.send(new ShadowBundleScrollPayload(focusedSlot.id, isScrollUp));

                            ci.cancel();
                        }
                    }
                }
            }
        }
    }

    @Unique
    private Slot eyeliss_getSlotFromScreen(HandledScreen<?> screen) {
        return ((eyeliss.particle.mod.mixin.client.HandledScreenAccessor) screen).getFocusedSlot();
    }
}