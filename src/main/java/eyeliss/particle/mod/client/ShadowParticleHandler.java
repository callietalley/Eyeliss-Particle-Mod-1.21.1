package eyeliss.particle.mod.client;

import eyeliss.particle.mod.component.ModComponents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.model.BakedModel; // Added missing import
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation; // Added missing import
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

import java.util.Random;

public class ShadowParticleHandler {

    private static final Random RANDOM = new Random();

    // Pure black dust particle effect parameter block (RGB: 0,0,0 | Scale: 1.0f)
    private static final DustParticleEffect BLACK_DUST = new DustParticleEffect(new Vector3f(0.0f, 0.0f, 0.0f), 1.0f);

    // Custom Dark Purple dust effect with a smaller scale profile (RGB: 0.5, 0.0, 0.5 | Scale: 0.6f)
    private static final DustParticleEffect PURPLE_DUST = new DustParticleEffect(new Vector3f(0.5f, 0.0f, 0.5f), 0.6f);

    private static final TagKey<net.minecraft.item.Item> SPEAR_TAG = TagKey.of(
            Registries.ITEM.getKey(),
            Identifier.of("eyelisspartmod", "spear_weapon")
    );

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.world == null || client.isPaused()) return;

            ClientPlayerEntity localPlayer = client.player;

            // Loop through all loaded entities in the client's current world
            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {

                    ItemStack mainHand = livingEntity.getStackInHand(Hand.MAIN_HAND);
                    ItemStack offHand = livingEntity.getStackInHand(Hand.OFF_HAND);

                    boolean hasCursedMain = !mainHand.isEmpty() && mainHand.getOrDefault(ModComponents.IS_CURSED, false);
                    boolean hasCursedOff = !offHand.isEmpty() && offHand.getOrDefault(ModComponents.IS_CURSED, false);

                    if (!hasCursedMain && !hasCursedOff) continue;

                    // Perspective filter for local player tracking parameters
                    if (livingEntity == localPlayer) {
                        if (client.options.getPerspective().isFirstPerson()) continue;
                    }

                    if (RANDOM.nextFloat() < 0.60f) {
                        float bodyYawRadians = (float) Math.toRadians(livingEntity.bodyYaw);

                        double speedMultiplier = 0.03 + (RANDOM.nextDouble() * 0.04);
                        double velocityX = -Math.sin(bodyYawRadians) * speedMultiplier;
                        double velocityZ = Math.cos(bodyYawRadians) * speedMultiplier;
                        double velocityY = (RANDOM.nextDouble() - 0.3) * 0.01;

                        double forwardDirX = -Math.sin(bodyYawRadians);
                        double forwardDirZ = Math.cos(bodyYawRadians);

                        // 1. Process Main Hand
                        if (hasCursedMain) {
                            BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModel(mainHand, livingEntity.getWorld(), livingEntity, livingEntity.getId());
                            double modelOffsetX = 0.0, modelOffsetY = 0.0, modelOffsetZ = 0.0;

                            if (model != null && model.getTransformation() != null) {
                                Transformation transform = model.getTransformation().getTransformation(ModelTransformationMode.THIRD_PERSON_RIGHT_HAND);
                                if (transform != null) {
                                    Vector3f translation = transform.translation;
                                    modelOffsetX = translation.x() / 16.0f;
                                    modelOffsetY = translation.y() / 16.0f;
                                    modelOffsetZ = translation.z() / 16.0f;
                                }
                            }

                            double handAngle = bodyYawRadians + (Math.PI / 2.0);
                            double handDistance = 0.35 + modelOffsetX;

                            double baseGridX = livingEntity.getX() - (Math.sin(handAngle) * handDistance) + (forwardDirX * modelOffsetZ);
                            double baseGridY = livingEntity.getY() + 0.9 + modelOffsetY;
                            double baseGridZ = livingEntity.getZ() + (Math.cos(handAngle) * handDistance) + (forwardDirZ * modelOffsetZ);

                            // --- STANDARD BLACK DUST ---
                            double extBlack = mainHand.isIn(SPEAR_TAG) ? (RANDOM.nextDouble() * 2.0) - 1.0 : RANDOM.nextDouble();
                            double bx = baseGridX + (forwardDirX * extBlack) + (RANDOM.nextDouble() - 0.5) * 0.03;
                            double by = baseGridY + (RANDOM.nextDouble() - 0.5) * 0.12;
                            double bz = baseGridZ + (forwardDirZ * extBlack) + (RANDOM.nextDouble() - 0.5) * 0.03;
                            livingEntity.getWorld().addParticle(BLACK_DUST, bx, by, bz, velocityX, velocityY, velocityZ);

                            // --- SMALLER & FREQUENT PURPLE DUST ---
                            for (int i = 0; i < 2; i++) {
                                double extPurple = mainHand.isIn(SPEAR_TAG) ? (RANDOM.nextDouble() * 2.0) - 1.0 : RANDOM.nextDouble();
                                double px = baseGridX + (forwardDirX * extPurple) + (RANDOM.nextDouble() - 0.5) * 0.02;
                                double py = baseGridY + (RANDOM.nextDouble() - 0.5) * 0.08;
                                double pz = baseGridZ + (forwardDirZ * extPurple) + (RANDOM.nextDouble() - 0.5) * 0.02;

                                livingEntity.getWorld().addParticle(PURPLE_DUST, px, py, pz, velocityX * 0.5, velocityY, velocityZ * 0.5);
                            }
                        }

                        // 2. Process Off-Hand
                        if (hasCursedOff) {
                            BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModel(offHand, livingEntity.getWorld(), livingEntity, livingEntity.getId());
                            double modelOffsetX = 0.0, modelOffsetY = 0.0, modelOffsetZ = 0.0;

                            if (model != null && model.getTransformation() != null) {
                                Transformation transform = model.getTransformation().getTransformation(ModelTransformationMode.THIRD_PERSON_LEFT_HAND);
                                if (transform != null) {
                                    Vector3f translation = transform.translation;
                                    modelOffsetX = translation.x() / 16.0f;
                                    modelOffsetY = translation.y() / 16.0f;
                                    modelOffsetZ = translation.z() / 16.0f;
                                }
                            }

                            double handAngle = bodyYawRadians - (Math.PI / 2.0);
                            double handDistance = 0.35 + modelOffsetX;

                            double baseGridX = livingEntity.getX() - (Math.sin(handAngle) * handDistance) + (forwardDirX * modelOffsetZ);
                            double baseGridY = livingEntity.getY() + 0.9 + modelOffsetY;
                            double baseGridZ = livingEntity.getZ() + (Math.cos(handAngle) * handDistance) + (forwardDirZ * modelOffsetZ);

                            // --- STANDARD BLACK DUST ---
                            double extBlack = offHand.isIn(SPEAR_TAG) ? (RANDOM.nextDouble() * 2.0) - 1.0 : RANDOM.nextDouble();
                            double bx = baseGridX + (forwardDirX * extBlack) + (RANDOM.nextDouble() - 0.5) * 0.03;
                            double by = baseGridY + (RANDOM.nextDouble() - 0.5) * 0.12;
                            double bz = baseGridZ + (forwardDirZ * extBlack) + (RANDOM.nextDouble() - 0.5) * 0.03;
                            livingEntity.getWorld().addParticle(BLACK_DUST, bx, by, bz, velocityX, velocityY, velocityZ);

                            // --- SMALLER & FREQUENT PURPLE DUST ---
                            for (int i = 0; i < 2; i++) {
                                double extPurple = offHand.isIn(SPEAR_TAG) ? (RANDOM.nextDouble() * 2.0) - 1.0 : RANDOM.nextDouble();
                                double px = baseGridX + (forwardDirX * extPurple) + (RANDOM.nextDouble() - 0.5) * 0.02;
                                double py = baseGridY + (RANDOM.nextDouble() - 0.5) * 0.08;
                                double pz = baseGridZ + (forwardDirZ * extPurple) + (RANDOM.nextDouble() - 0.5) * 0.02;

                                livingEntity.getWorld().addParticle(PURPLE_DUST, px, py, pz, velocityX * 0.5, velocityY, velocityZ * 0.5);
                            }
                        }
                    }
                }
            }
        });
    }
}