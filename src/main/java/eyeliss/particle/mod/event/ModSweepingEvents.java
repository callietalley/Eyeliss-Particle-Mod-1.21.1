package eyeliss.particle.mod.event;

import eyeliss.particle.mod.component.EngravingContents;
import eyeliss.particle.mod.component.ModComponents;
import eyeliss.particle.mod.component.ModSweepingAttackUtil;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import java.util.List;

public class ModSweepingEvents {

    public static void registerSweepingAttackHandler() {
        // Safe, stable Fabric hook that fires on common threads right when a player initiates an attack strike
        AttackEntityCallback.EVENT.register((player, world, hand, targetEntity, hitResult) -> {
            if (world.isClient() || player.isSpectator() || !(targetEntity instanceof LivingEntity target)) {
                return ActionResult.PASS;
            }

            ItemStack weapon = player.getStackInHand(hand);
            if (weapon.isEmpty()) return ActionResult.PASS;

            List<EngravingContents> engravings = weapon.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());
            boolean hasSweeping = engravings.stream().anyMatch(e -> e.engravingId().equals("sweeping"));
            if (!hasSweeping) return ActionResult.PASS;

            boolean isSword = weapon.getItem() instanceof SwordItem;

            // Enforce vanilla sweeping requirements: Player must be on ground, not sprinting, and not falling fast
            if (player.isOnGround() && !player.isSprinting() && player.getVelocity().horizontalLengthSquared() < 0.25) {

                // Fetch the mathematically correct range radius using your record-compliant utility class
                double areaModifier = ModSweepingAttackUtil.calculateSweepingAreaRangeModifier(weapon, isSword);

                if (areaModifier > 0) {
                    // 1. Spawn a localized sweeping particle cloud right across the harvest sweep line
                    if (world instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                                target.getX(), target.getBodyY(0.5), target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                    }

                    // 2. Compute the expansion box boundaries based on your calibrated level attributes
                    // If it's a sword, we expand the base vanilla sweep radius. If it's a non-sword, we create it!
                    double baseExpansionX = (isSword ? 1.0 : 0.75) * areaModifier;
                    double baseExpansionY = 0.25;
                    double baseExpansionZ = (isSword ? 1.0 : 0.75) * areaModifier;

                    List<LivingEntity> sweepingTargets = world.getEntitiesByClass(
                            LivingEntity.class,
                            target.getBoundingBox().expand(baseExpansionX, baseExpansionY, baseExpansionZ),
                            entity -> entity != player && entity != target && !player.isTeammate(entity)
                    );

                    // 3. Distribute sweeping damage across all targets caught inside the geometry zones
                    for (LivingEntity secondaryTarget : sweepingTargets) {
                        secondaryTarget.takeKnockback(0.4F, Math.sin(player.getYaw() * 0.017453292F), -Math.cos(player.getYaw() * 0.017453292F));
                        // Inflict standard sweeping damage
                        secondaryTarget.damage(world.getDamageSources().playerAttack(player), 1.0F);
                    }

                    // 4. Play the vanilla sword slicing sound effect packet natively
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
            }

            return ActionResult.PASS; // Allows standard primary attack hit calculations to finish normally
        });
    }
}
