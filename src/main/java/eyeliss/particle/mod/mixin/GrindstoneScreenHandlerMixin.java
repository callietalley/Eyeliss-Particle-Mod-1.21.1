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
        // Access items universally using ScreenHandler slots (Slots 0 and 1 are the Grindstone inputs)
        ItemStack slot0 = this.getSlot(0).getStack();
        ItemStack slot1 = this.getSlot(1).getStack();

        // Single-item operations only. If both input slots have items, let vanilla handle item repairs.
        if (!slot0.isEmpty() && !slot1.isEmpty()) {
            return;
        }

        // Identify which slot contains our active target item
        ItemStack inputTarget = slot0.isOf(ModWeapons.SYRINGE) ? slot0 : (slot1.isOf(ModWeapons.SYRINGE) ? slot1 : ItemStack.EMPTY);

        if (!inputTarget.isEmpty()) {
            SyringeContents contents = inputTarget.getOrDefault(ModComponents.SYRINGE_CONTENTS, SyringeContents.EMPTY);

            // 💡 FIX: Loop through payloads() and collect non-empty effect IDs to align with our upgraded data record
            ArrayList<String> activePotions = new ArrayList<>();
            if (contents != null && contents.payloads() != null) {
                for (SyringeContents.Payload payload : contents.payloads()) {
                    if (!payload.effectId().equals("minecraft:empty")) {
                        activePotions.add(payload.effectId());
                    }
                }
            }

            // 🧪 RULE 1: If the Syringe contains potion fluid, clear the payload but preserve enchantments!
            if (!activePotions.isEmpty()) {
                ItemStack outputStack = inputTarget.copy();

                // Clear out the chemical fluid storage back to empty
                outputStack.set(ModComponents.SYRINGE_CONTENTS, SyringeContents.EMPTY);

                boolean hasInfusionEnchant = outputStack.getEnchantments() != null &&
                        outputStack.getEnchantments().getEnchantments().stream()
                                .anyMatch(entry -> entry.matchesKey(eyeliss.particle.mod.enchantment.ModEnchantments.CHEMICAL_INFUSION));

                // Recalculate traits: if enchanted, force visual stat display to 0.0 (1 total damage), otherwise reset to default 3.0
                if (hasInfusionEnchant) {
                    outputStack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, SyringeItem.createEnchantedSyringeAttributes());
                } else {
                    outputStack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, SyringeItem.createSyringeAttributes(3.0, -1.8));
                }

                // Slot 2 is the Grindstone result output slot container across all mappings
                this.getSlot(2).setStack(outputStack);
                this.sendContentUpdates();
                ci.cancel();
            }

            // 🧪 RULE 2: If the Syringe is completely empty of fluid, step aside!
            // This lets vanilla's default system process standard disenchanting, stripping books and yielding player XP.
        }
    }
}
