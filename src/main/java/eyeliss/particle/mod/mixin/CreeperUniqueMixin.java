package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.item.ModItems;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
public class CreeperUniqueMixin {

    @Shadow private int currentFuseTime;
    @Shadow private int fuseTime;

    @Unique
    private boolean droppedSuspensefulEssence = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void checkExplosionDrop(CallbackInfo ci) {
        CreeperEntity creeper = (CreeperEntity) (Object) this;

        // Run strictly on server side, when the creeper is dying, and hasn't dropped it yet
        if (!creeper.getWorld().isClient() && creeper.isDead() && !droppedSuspensefulEssence) {

            float chargePercentage = (float) this.currentFuseTime / (float) this.fuseTime;

            if (chargePercentage >= 0.75f) {
                if (ModItems.SUSPENSEFUL_ESSENCE != null) {
                    ItemStack essenceDrop = new ItemStack(ModItems.SUSPENSEFUL_ESSENCE, 1);
                    creeper.dropStack(essenceDrop);
                    droppedSuspensefulEssence = true; // Prevents double drops
                }
            }
        }
    }
}
