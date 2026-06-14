package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.item.trinkets.MidasGoldItem;
import eyeliss.particle.mod.item.trinkets.SadimsIronItem; // Imported your new item class!
import dev.emi.trinkets.api.TrinketInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrinketInventory.class)
public class TrinketEquipMixin {

    @Inject(method = "setStack", at = @At("HEAD"))
    private void onTrinketEquipped(int slot, ItemStack stack, CallbackInfo ci) {
        TrinketInventory inventory = (TrinketInventory) (Object) this;
        World world = inventory.getComponent().getEntity().getWorld();

        // Check if we are running on the server side and an item is actually arriving
        if (!world.isClient() && !stack.isEmpty()) {

            // Fetch whatever item is CURRENTLY sitting inside the slot index
            ItemStack existingStack = inventory.getStack(slot);

            // Only play audio cues if the slot was completely empty beforehand (prevents inventory sync echo glitches)
            if (existingStack.isEmpty()) {
                var entity = inventory.getComponent().getEntity();
                String slotId = inventory.getSlotType().getId();

                if (slotId.endsWith("legs/pocket")) {
                    // 1. MIDAS' GOLD: High pitch metallic gold ring
                    if (stack.getItem() instanceof MidasGoldItem) {
                        world.playSound(
                                null,
                                entity.getX(), entity.getY(), entity.getZ(),
                                SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
                                SoundCategory.NEUTRAL,
                                1.0F, 0.2F
                        );
                    }
                    // 2. SADIM'S IRON: Low pitch heavy iron lock clonk
                    else if (stack.getItem() instanceof SadimsIronItem) {
                        world.playSound(
                                null,
                                entity.getX(), entity.getY(), entity.getZ(),
                                SoundEvents.ITEM_ARMOR_EQUIP_IRON,
                                SoundCategory.NEUTRAL,
                                1.0F, 0.4F
                        );
                    }
                }
            }
        }
    }
}
