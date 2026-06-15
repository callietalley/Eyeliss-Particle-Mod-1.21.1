package eyeliss.particle.mod.effect;

import eyeliss.particle.mod.network.OverhealthSyncPayload;
import eyeliss.particle.mod.particle.ModParticles;
import eyeliss.particle.mod.util.OverhealthTracker;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class OverhealthEffect extends StatusEffect {

    public OverhealthEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x00FF8C);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity == null) return true;

        StatusEffectInstance instance = entity.getStatusEffect(Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH));

        if (instance != null) {
            int activeDuration = instance.getDuration();
            int historicalDuration = OverhealthTracker.getHistoricalDuration(entity);

            if (!OverhealthTracker.hasOverhealth(entity) || (activeDuration > (historicalDuration + 5))) {
                OverhealthTracker.grantOverhealth(entity, amplifier);
            }

            OverhealthTracker.setHistoricalDuration(entity, activeDuration);

            // =========================================================================
            //   ⏱️ 3-SECOND TRI-SHIELD SPAWNER PASS (NO OVERWRITE LOOPS)
            // =========================================================================
            if (!entity.getWorld().isClient() && entity.getWorld() instanceof ServerWorld serverWorld) {
                // FIXED TIMER: Changed from % 120 down to % 60.
                // This forces a brand-new defensive ring refresh exactly every 3 seconds (60 ticks)!
                if (activeDuration % 60 == 0) {
                    serverWorld.spawnParticles(
                            ModParticles.OVERHEALTH_ORBIT,
                            entity.getX(), entity.getBodyY(0.4), entity.getZ(),
                            3,                       // Count=3: Forces the engine to tell the client to spawn 3 items on this frame!
                            (double) entity.getId(), // velocityX: Safely beams the tracking entity's anchor ID down to the client
                            0.0, 0.0,
                            0.0                      // Speed parameter multiplier locked at 0
                    );
                }
            }
            // =========================================================================
        }

        if (entity instanceof ServerPlayerEntity serverPlayer) {
            float currentShield = OverhealthTracker.getOverhealth(serverPlayer);
            float maxShieldBounds = OverhealthTracker.getMaxOverhealth(serverPlayer);

            ServerPlayNetworking.send(serverPlayer, new OverhealthSyncPayload(currentShield, maxShieldBounds));
        }

        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
