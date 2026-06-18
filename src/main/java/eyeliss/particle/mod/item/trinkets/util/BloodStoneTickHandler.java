package eyeliss.particle.mod.item.trinkets.util;

import dev.emi.trinkets.api.TrinketsApi;
import eyeliss.particle.mod.item.trinkets.BloodStoneItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Box;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static eyeliss.particle.mod.sound.ModSounds.BLOOD_STEAL_EVENT;
import static eyeliss.particle.mod.sound.ModSounds.BLOOD_STONE_EVENT;

public class BloodStoneTickHandler {
    private static final Map<UUID, Integer> COOLDOWNS = new HashMap<>();

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID playerId = player.getUuid();
                if (COOLDOWNS.containsKey(playerId)) {
                    int timeLeft = COOLDOWNS.get(playerId) - 1;
                    if (timeLeft <= 0) COOLDOWNS.remove(playerId);
                    else COOLDOWNS.put(playerId, timeLeft);
                }
            }
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((damagedEntity, damageSource, damageAmount) -> {
            if (damagedEntity instanceof ServerPlayerEntity targetPlayer) {
                UUID targetPlayerId = targetPlayer.getUuid();

                if (targetPlayer.getHealth() <= 0.0f || targetPlayer.isDead() || COOLDOWNS.containsKey(targetPlayerId)) {
                    return true;
                }

                boolean hasBloodStone = TrinketsApi.getTrinketComponent(targetPlayer)
                        .map(component -> component.isEquipped(stack -> stack.getItem() instanceof BloodStoneItem))
                        .orElse(false);

                if (hasBloodStone) {
                    float maxHealth = (float) targetPlayer.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
                    float projectedHealth = targetPlayer.getHealth() - damageAmount;

                    if (projectedHealth <= maxHealth * 0.20f) {
                        if (triggerBloodDrain(targetPlayer)) {
                            COOLDOWNS.put(targetPlayerId, 4000);
                        }
                    }
                }
            }
            return true;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((target, damageSource) -> {
            if (damageSource.getAttacker() instanceof ServerPlayerEntity player && target.getMaxHealth() > 10.0f) {
                boolean hasBloodStone = TrinketsApi.getTrinketComponent(player)
                        .map(component -> component.isEquipped(stack -> stack.getItem() instanceof BloodStoneItem))
                        .orElse(false);

                if (hasBloodStone) {
                    var regenEffect = net.minecraft.registry.Registries.STATUS_EFFECT.getEntry(StatusEffects.REGENERATION.value());
                    StatusEffectInstance regenInstance = new StatusEffectInstance(
                            regenEffect, 40, 1, false, true, true
                    );
                    player.addStatusEffect(regenInstance);

                    player.getServerWorld().spawnParticles(
                            eyeliss.particle.mod.particle.ModParticles.BLOOD_SMOKE,
                            player.getX(), player.getY() + 0.2, player.getZ(),
                            5, 0.2, 0.2, 0.2, 0.05
                    );
                }
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID playerId = player.getUuid();

            if (COOLDOWNS.containsKey(playerId)) {
                int remainingTicks = COOLDOWNS.get(playerId);
                if (remainingTicks > 0) {
                    TrinketsApi.getTrinketComponent(player).ifPresent(component ->
                            component.getEquipped(stack -> stack.getItem() instanceof BloodStoneItem).forEach(tuple ->
                                    player.getItemCooldownManager().set(tuple.getRight().getItem(), remainingTicks)
                            )
                    );
                }
            }
        });
    }

    private static boolean triggerBloodDrain(ServerPlayerEntity player) {
        double radius = 8.0;
        Box box = player.getBoundingBox().expand(radius);

        List<LivingEntity> targets = player.getServerWorld().getEntitiesByClass(
                LivingEntity.class,
                box,
                entity -> entity != player && entity.isAlive() && !entity.isTeammate(player)
        );

        boolean targetsDrained = false;
        float totalStolenHealth = 0.0f;

        double playerX = player.getX();
        double playerY = player.getY() + 1.0;
        double playerZ = player.getZ();

        for (LivingEntity target : targets) {
            float targetHealth = target.getHealth();
            float amountToDrain = Math.min(targetHealth, 3.0f);

            if (amountToDrain > 0) {
                target.damage(player.getServerWorld().getDamageSources().magic(), amountToDrain);
                totalStolenHealth += amountToDrain;
                targetsDrained = true;

                player.getServerWorld().playSound(
                        null, target.getX(), target.getY() + 1.0, target.getZ(),
                        BLOOD_STEAL_EVENT, SoundCategory.HOSTILE, 0.7f, 1.2f
                );

                double targetX = target.getX();
                double targetY = target.getY() + 1.0;
                double targetZ = target.getZ();
                double dx = playerX - targetX;
                double dy = playerY - targetY;
                double dz = playerZ - targetZ;
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                int particleCount = (int) (distance * 3);

                for (int i = 0; i <= particleCount; i++) {
                    double ratio = (double) i / particleCount;
                    player.getServerWorld().spawnParticles(
                            ParticleTypes.INSTANT_EFFECT,
                            targetX + (dx * ratio), targetY + (dy * ratio), targetZ + (dz * ratio),
                            1, 0.0, 0.0, 0.0, 0.0
                    );
                }
            }
        }

        if (targetsDrained && totalStolenHealth > 0) {
            player.heal(totalStolenHealth);

            player.getServerWorld().playSound(
                    null, playerX, playerY, playerZ,
                    BLOOD_STONE_EVENT, SoundCategory.PLAYERS, 1.0f, 1.0f
            );

            player.getServerWorld().spawnParticles(
                    eyeliss.particle.mod.particle.ModParticles.BLOOD_SMOKE,
                    playerX, playerY, playerZ, 35, 0.3, 0.4, 0.3, 0.2
            );

            TrinketsApi.getTrinketComponent(player).ifPresent(component ->
                    component.getEquipped(stack -> stack.getItem() instanceof BloodStoneItem).forEach(tuple ->
                            player.getItemCooldownManager().set(tuple.getRight().getItem(), 4000)
                    )
            );

            return true;
        }
        return false;
    }

    public static void resetCooldown(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        COOLDOWNS.remove(playerId);

        TrinketsApi.getTrinketComponent(player).ifPresent(component ->
                component.getEquipped(stack -> stack.getItem() instanceof BloodStoneItem).forEach(tuple ->
                        player.getItemCooldownManager().remove(tuple.getRight().getItem())
                )
        );
    }
}
