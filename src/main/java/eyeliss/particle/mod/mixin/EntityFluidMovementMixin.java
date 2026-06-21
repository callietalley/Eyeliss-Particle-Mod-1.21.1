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

        FluidState fluidState = entity.getWorld().getFluidState(entity.getBlockPos());
        if (fluidState.getFluid() == ModFluids.STILL_SOURCE_SAUCE || fluidState.getFluid() == ModFluids.FLOWING_SOURCE_SAUCE) {
            Vec3d velocity = entity.getVelocity();

            if (entity instanceof LivingEntity living) {
                LivingEntityAccessor accessor = (LivingEntityAccessor) living;

                if (accessor.isJumping()) {
                    entity.setVelocity(velocity.x * 0.5D, 0.06D, velocity.z * 0.5D);
                } else {
                    if (velocity.y < -0.02D) {
                        entity.setVelocity(velocity.x * 0.5D, -0.02D, velocity.z * 0.5D);
                    }
                }
            } else {
                if (velocity.y < 0.04D) {
                    entity.setVelocity(velocity.x * 0.5D, velocity.y + 0.02D, velocity.z * 0.5D);
                }
            }
        }
    }
}
