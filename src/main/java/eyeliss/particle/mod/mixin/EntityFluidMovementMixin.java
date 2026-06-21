package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.fluid.ModFluids;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityFluidMovementMixin {

    @Inject(method = "updateWaterState", at = @At("TAIL"))
    private void applySourceSauceSluggishBuoyancy(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;

        // Check if the entity is inside your fluid
        FluidState fluidState = entity.getWorld().getFluidState(entity.getBlockPos());
        if (fluidState.getFluid() == ModFluids.STILL_SOURCE_SAUCE || fluidState.getFluid() == ModFluids.FLOWING_SOURCE_SAUCE) {
            Vec3d velocity = entity.getVelocity();

            // If the entity is a living creature (like a player) trying to move/jump
            if (entity instanceof LivingEntity living) {
                // FIXED: Safely casts via the accessor interface to check if spacebar is active
                LivingEntityAccessor accessor = (LivingEntityAccessor) living;

                if (accessor.isJumping()) {
                    // Give them a controlled, slow upward velocity (matching vanilla lava swimming strength)
                    entity.setVelocity(velocity.x * 0.5D, 0.06D, velocity.z * 0.5D);
                } else {
                    // If they aren't jumping, damp their downward speed so they sink sludgily instead of falling fast
                    if (velocity.y < -0.02D) {
                        entity.setVelocity(velocity.x * 0.5D, -0.02D, velocity.z * 0.5D);
                    }
                }
            } else {
                // Standard non-player items or entities float upward slowly to match lava density
                if (velocity.y < 0.04D) {
                    entity.setVelocity(velocity.x * 0.5D, velocity.y + 0.02D, velocity.z * 0.5D);
                }
            }
        }
    }
}
