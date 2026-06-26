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

            if (player.isOnGround() && !player.isSprinting() && player.getVelocity().horizontalLengthSquared() < 0.25) {

                double areaModifier = ModSweepingAttackUtil.calculateSweepingAreaRangeModifier(weapon, isSword);

                if (areaModifier > 0) {
                    if (world instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                                target.getX(), target.getBodyY(0.5), target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                    }

                    double baseExpansionX = (isSword ? 1.0 : 0.75) * areaModifier;
                    double baseExpansionY = 0.25;
                    double baseExpansionZ = (isSword ? 1.0 : 0.75) * areaModifier;

                    List<LivingEntity> sweepingTargets = world.getEntitiesByClass(
                            LivingEntity.class,
                            target.getBoundingBox().expand(baseExpansionX, baseExpansionY, baseExpansionZ),
                            entity -> entity != player && entity != target && !player.isTeammate(entity)
                    );

                    for (LivingEntity secondaryTarget : sweepingTargets) {
                        secondaryTarget.takeKnockback(0.4F, Math.sin(player.getYaw() * 0.017453292F), -Math.cos(player.getYaw() * 0.017453292F));
                        // Inflict standard sweeping damage
                        secondaryTarget.damage(world.getDamageSources().playerAttack(player), 1.0F);
                    }

                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
            }

            return ActionResult.PASS;
        });
    }
}
