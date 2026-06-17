package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.component.ModComponents;
import eyeliss.particle.mod.component.SyringeContents;
import eyeliss.particle.mod.item.ModWeapons;
import eyeliss.particle.mod.item.specialweapons.SyringeItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin extends ScreenHandler {

    protected GrindstoneScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "updateResult", at = @At("TAIL"), cancellable = true)
    private void modifySyringeGrindstoneBehavior(CallbackInfo ci) {
        ItemStack slot0 = this.getSlot(0).getStack();
        ItemStack slot1 = this.getSlot(1).getStack();

        if (!slot0.isEmpty() && !slot1.isEmpty()) {
            return;
        }

        ItemStack inputTarget = slot0.isOf(ModWeapons.SYRINGE) ? slot0 : (slot1.isOf(ModWeapons.SYRINGE) ? slot1 : ItemStack.EMPTY);

        if (!inputTarget.isEmpty()) {
            SyringeContents contents = inputTarget.getOrDefault(ModComponents.SYRINGE_CONTENTS, SyringeContents.EMPTY);

            ArrayList<String> activePotions = new ArrayList<>();
            if (contents != null && contents.payloads() != null) {
                for (SyringeContents.Payload payload : contents.payloads()) {
                    if (!payload.effectId().equals("minecraft:empty")) {
                        activePotions.add(payload.effectId());
                    }
                }
            }

            if (!activePotions.isEmpty()) {
                ItemStack outputStack = inputTarget.copy();

                outputStack.set(ModComponents.SYRINGE_CONTENTS, SyringeContents.EMPTY);

                boolean hasInfusionEnchant = outputStack.getEnchantments() != null &&
                        outputStack.getEnchantments().getEnchantments().stream()
                                .anyMatch(entry -> entry.matchesKey(eyeliss.particle.mod.enchantment.ModEnchantments.CHEMICAL_INFUSION));

                if (hasInfusionEnchant) {
                    outputStack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, SyringeItem.createEnchantedSyringeAttributes());
                } else {
                    outputStack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, SyringeItem.createSyringeAttributes(3.0, -1.8));
                }

                this.getSlot(2).setStack(outputStack);
                this.sendContentUpdates();
                ci.cancel();
            }
        }
    }
}
