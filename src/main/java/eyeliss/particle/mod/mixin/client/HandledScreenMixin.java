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

                            // 1. Predict order modification locally on the client for smooth instant feedback
                            if (isScrollUp) {
                                // Scroll UP now moves forward: Pushes the front element down to the back of the queue
                                ItemStack firstItem = itemArrayList.remove(0);
                                itemArrayList.add(firstItem);
                            } else {
                                // Scroll DOWN now moves backward: Pulls the trailing element and places it at index 0
                                ItemStack lastItem = itemArrayList.remove(itemArrayList.size() - 1);
                                itemArrayList.add(0, lastItem);
                            }
                            bundleStack.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(itemArrayList));

                            // 2. Send the data over the network. The server processes it and prevents snap-back rollbars.
                            ClientPlayNetworking.send(new ShadowBundleScrollPayload(focusedSlot.id, isScrollUp));

                            // 3. Halt hardware event execution so background slots are untouched
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