package eyeliss.particle.mod.event;

import eyeliss.particle.mod.effect.ModEffects;
import eyeliss.particle.mod.network.OverhealthSyncPayload;
import eyeliss.particle.mod.util.OverhealthTracker;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

public class OverhealthHandler {

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            RegistryEntry<StatusEffect> overhealthEntry = Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH);

            if (entity.hasStatusEffect(overhealthEntry)) {
                StatusEffectInstance effectInstance = entity.getStatusEffect(overhealthEntry);
                int amplifier = effectInstance != null ? effectInstance.getAmplifier() : 0;

                if (!OverhealthTracker.hasOverhealth(entity)) {
                    OverhealthTracker.grantOverhealth(entity, amplifier);
                }

                float currentShield = OverhealthTracker.getOverhealth(entity);

                if (currentShield > 0) {
                    entity.getWorld().playSound(
                            null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS,
                            1.0f, 1.5f
                    );

                    float maxShieldBounds = OverhealthTracker.getMaxOverhealth(entity);

                    if (amount >= currentShield) {
                        float remainingDamage = amount - currentShield;
                        OverhealthTracker.setOverhealth(entity, 0.0f);
                        entity.removeStatusEffect(overhealthEntry);

                        if (entity instanceof ServerPlayerEntity serverPlayer) {
                            ServerPlayNetworking.send(serverPlayer, new OverhealthSyncPayload(0.0f, maxShieldBounds));
                        }

                        // ==========================================================
                        //   🎨 SIMPLIFIED SHATTER EFFECT: SPAWNS 10 PARTICLES NATIVELY
                        // ==========================================================
                        if (entity.getWorld() instanceof ServerWorld serverWorld) {
                            DustParticleEffect yellowDust = new DustParticleEffect(new Vector3f(1.0f, 0.85f, 0.0f), 1.0f);

                            // Native vanilla single-line call to explode 10 particles outwards automatically
                            serverWorld.spawnParticles(
                                    yellowDust,
                                    entity.getX(), entity.getBodyY(0.5), entity.getZ(),
                                    10,    // Spawns exactly 10 particles
                                    0.3, 0.3, 0.3, // Spread distance in X, Y, Z directions
                                    0.15   // Outer explosion velocity speed multiplier
                            );

                            serverWorld.playSound(
                                    null, entity.getX(), entity.getY(), entity.getZ(),
                                    SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.PLAYERS,
                                    2.0f, 0.2f
                            );
                        }

                        // ==========================================================
                        //   ⚡ SIMPLIFIED SPELL ENGINE STUN (1.25s / 25 Ticks)
                        // ==========================================================
                        StatusEffect targetStunEffect = Registries.STATUS_EFFECT.get(Identifier.of("spell_engine", "stun"));

                        if (targetStunEffect != null) {
                            RegistryEntry<StatusEffect> stunRegistryEntry = Registries.STATUS_EFFECT.getEntry(targetStunEffect);
                            entity.addStatusEffect(new StatusEffectInstance(stunRegistryEntry, 25, 0));
                        }
                        // ==========================================================

                        entity.damage(source, remainingDamage);
                    } else {
                        float newShieldValue = currentShield - amount;
                        OverhealthTracker.setOverhealth(entity, newShieldValue);

                        if (entity instanceof ServerPlayerEntity serverPlayer) {
                            ServerPlayNetworking.send(serverPlayer, new OverhealthSyncPayload(newShieldValue, maxShieldBounds));
                        }
                    }

                    return false;
                }
            }
            return true;
        });
    }
}
