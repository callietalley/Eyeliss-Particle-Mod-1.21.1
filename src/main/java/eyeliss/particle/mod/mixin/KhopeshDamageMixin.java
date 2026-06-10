package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.item.specialweapons.KhopeshItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class KhopeshDamageMixin {

    @ModifyVariable(
            method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float modifyKhopeshDamage(float amount, DamageSource source) {
        LivingEntity target = (LivingEntity) (Object) this;

        if (target.getWorld().isClient()) {
            return amount;
        }

        if (source.getAttacker() instanceof PlayerEntity player) {
            ItemStack heldItem = player.getMainHandStack();

            if (heldItem.getItem() instanceof KhopeshItem) {
                double distance = player.distanceTo(target);

                // Configure your distance bounds
                double minReachDistance = 1.75;
                double maxReachDistance = 3.5;
                float maxMultiplier = 0.833333334f;

                float progress = (float) ((distance - minReachDistance) / (maxReachDistance - minReachDistance));
                float clampedProgress = Math.clamp(progress, 0.0f, 1.0f);

                float currentMultiplier = clampedProgress * maxMultiplier;

                if (clampedProgress >= 1.0f) {
                    target.getWorld().playSound(
                            null,
                            target.getX(), target.getY(), target.getZ(),
                            SoundEvents.BLOCK_AMETHYST_CLUSTER_HIT,
                            SoundCategory.PLAYERS,
                            1.0f,
                            1.2f
                    );

                    // Spawn the 10 firework particles bursting outward
                    if (target.getWorld() instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(
                                ParticleTypes.FIREWORK,
                                target.getX(),
                                target.getRandomBodyY(),
                                target.getZ(),
                                10,
                                0.1, 0.1, 0.1,
                                0.15
                        );
                    }
                }

                return amount * (1.0f + currentMultiplier);
            }
        }

        return amount;
    }
}