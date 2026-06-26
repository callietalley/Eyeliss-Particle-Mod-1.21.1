package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.component.*;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class EngravingEffectPlayerMixin {

    // Precision decimal accumulators to bridge fractional experience across ticks securely
    @Unique private static final ThreadLocal<Float> eyelisspartmod$fractionalXPTracker = ThreadLocal.withInitial(() -> 0.0f);
    @Unique private static final ThreadLocal<Float> eyelisspartmod$fractionalDevotionTracker = ThreadLocal.withInitial(() -> 0.0f);

    // Unique tracking ledger guard to cleanly block stack overflow recursive feedback loops
    @Unique private boolean eyelisspartmod$isProcessingXp = false;

    // --- UNIVERSAL WISDOM ENGINE: INTERCEPTS ALL XP COMBAT/MINING TRANSACTIONS ---
    @Inject(method = "addExperience(I)V", at = @At("HEAD"), cancellable = true)
    private void injectUniversalWisdomCollectionSynergy(int originalXPAmount, CallbackInfo ci) {
        // Stop execution instantly if this call was triggered by our own modifier bonus injection pass
        if (this.eyelisspartmod$isProcessingXp || originalXPAmount <= 0) return;

        PlayerEntity player = (PlayerEntity)(Object)this;
        if (player.getWorld().isClient()) return;

        // --- PHASE 1: STANDARD WISDOM MULTIPLIER SCALING (+20% per piece) ---
        int wisdomCount = 0;
        wisdomCount += eyelisspartmod$countWisdom(player.getMainHandStack());
        wisdomCount += eyelisspartmod$countWisdom(player.getOffHandStack());
        for (ItemStack armor : player.getArmorItems()) {
            wisdomCount += eyelisspartmod$countWisdom(armor);
        }

        int processedXPAmount = originalXPAmount;
        if (wisdomCount > 0) {
            double multiplier = 1.0 + (wisdomCount * 0.20);
            int newAmount = (int) Math.round(originalXPAmount * multiplier);
            if (newAmount == originalXPAmount && multiplier > 1.0) {
                newAmount += 1;
            }

            int extraXpToAward = newAmount - originalXPAmount;
            if (extraXpToAward > 0) {
                this.eyelisspartmod$isProcessingXp = true;
                player.addExperience(extraXpToAward);
                this.eyelisspartmod$isProcessingXp = false;
            }
            processedXPAmount = newAmount;
        }

        // --- PHASE 2: PROGRESSION SHRIVING & DEVOTION MULTI-SIPHONS ---
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ItemStack heldItem = serverPlayer.getMainHandStack();

            if (!heldItem.isEmpty()) {
                // TRACKING PATHWAY A: Legacy Blood Kills & Geologic Shriving Stone points (20% Amplified rate)
                if (eyelisspartmod$countWisdom(heldItem) > 0) {
                    float incomingRatioWeight = processedXPAmount * 0.02f;
                    float currentAccumulatedPool = eyelisspartmod$fractionalXPTracker.get() + incomingRatioWeight;

                    int successfulRollsCount = 0;
                    while (currentAccumulatedPool >= 1.0f) {
                        currentAccumulatedPool -= 1.0f;
                        // Calibrated up from < 0.05f to < 0.20f to grant a crisp 20% proc rate per 50 XP interval
                        if (serverPlayer.getRandom().nextFloat() < 0.20f) {
                            successfulRollsCount++;
                        }
                    }
                    eyelisspartmod$fractionalXPTracker.set(currentAccumulatedPool);

                    if (successfulRollsCount > 0) {
                        int totalPointsToAward = successfulRollsCount * 15;
                        if (heldItem.contains(ModComponents.SHRIVING_CHARGE)) {
                            eyelisspartmod$awardKillBonus(heldItem, serverPlayer, totalPointsToAward);
                        }
                        else if (heldItem.contains(ModComponents.BLOCK_CHARGE)) {
                            eyelisspartmod$awardBlockBonus(heldItem, serverPlayer, totalPointsToAward);
                        }
                    }
                }

                // TRACKING PATHWAY B: Isolated Blessed Aura Devotion Charger (1 Point per 10 XP points)
                if (heldItem.contains(ModComponents.BLESSED_CHARGE)) {
                    float incomingDevotionWeight = processedXPAmount * 0.10f;
                    float currentDevotionPool = eyelisspartmod$fractionalDevotionTracker.get() + incomingDevotionWeight;

                    int wholeDevotionPointsToAward = 0;
                    while (currentDevotionPool >= 1.0f) {
                        currentDevotionPool -= 1.0f;
                        wholeDevotionPointsToAward++;
                    }
                    eyelisspartmod$fractionalDevotionTracker.set(currentDevotionPool);

                    if (wholeDevotionPointsToAward > 0) {
                        eyelisspartmod$awardDevotionBonus(heldItem, serverPlayer, wholeDevotionPointsToAward);
                    }
                }
            }

            // --- PHASE 3: DYNAMIC RESTORATION BLESSING CACHE-BYPASSING REPAIR ENGINE ---
            List<ItemStack> repairableEquipment = new ArrayList<>();
            ItemStack mainTool = serverPlayer.getMainHandStack();
            if (!mainTool.isEmpty()) repairableEquipment.add(mainTool);
            ItemStack offTool = serverPlayer.getOffHandStack();
            if (!offTool.isEmpty()) repairableEquipment.add(offTool);
            for (ItemStack armor : serverPlayer.getArmorItems()) {
                if (!armor.isEmpty()) repairableEquipment.add(armor);
            }

            int xpLeftToProcess = processedXPAmount;

            for (ItemStack piece : repairableEquipment) {
                if (xpLeftToProcess <= 0) break;

                List<EngravingContents> engravings = piece.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());
                boolean hasRestoration = engravings.stream().anyMatch(e -> e.engravingId().equals("restoration"));

                if (hasRestoration && piece.isDamaged()) {
                    net.minecraft.component.type.ItemEnchantmentsComponent enchants = piece.getOrDefault(
                            net.minecraft.component.DataComponentTypes.ENCHANTMENTS,
                            net.minecraft.component.type.ItemEnchantmentsComponent.DEFAULT
                    );

                    boolean hasVanillaMending = enchants.getEnchantments().stream()
                            .anyMatch(e -> e.matchesKey(Enchantments.MENDING));

                    // CALIBRATED COMBO RATIOS: Restoration Alone = Mending I (1XP:2Durability) | Stacked with Mending = Mending II (1XP:4Durability!)
                    int repairPointsPerXP = hasVanillaMending ? 4 : 2;

                    while (piece.isDamaged() && xpLeftToProcess > 0) {
                        int currentDamage = piece.getDamage();
                        int newDamageValue = Math.max(0, currentDamage - repairPointsPerXP);

                        piece.setDamage(newDamageValue);
                        xpLeftToProcess--;
                    }

                    if (serverPlayer.getWorld() instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                                serverPlayer.getX(), serverPlayer.getBodyY(0.5), serverPlayer.getZ(), 4, 0.3, 0.3, 0.3, 0.0);
                    }
                }
            }

            // Flush remaining XP back to the player level bar if any repairs occurred, bypassing vanilla loops
            if (xpLeftToProcess != processedXPAmount) {
                this.eyelisspartmod$isProcessingXp = true;
                serverPlayer.addExperience(xpLeftToProcess);
                this.eyelisspartmod$isProcessingXp = false;
                ci.cancel();
            }
        }
    }

    // =========================================================================
    // PART 3: BLESSED AURA EXPONENTIAL XP DEVOTION ACCUMULATOR GAUGE
    // =========================================================================
    @Unique
    private void eyelisspartmod$awardDevotionBonus(ItemStack heldItem, ServerPlayerEntity serverPlayer, int pointsToAward) {
        BlessedCharge charge = heldItem.get(ModComponents.BLESSED_CHARGE);
        if (charge == null) return;

        int updatedDevotion = charge.currentPoints() + pointsToAward;

        if (updatedDevotion >= charge.requiredPoints()) {
            heldItem.remove(ModComponents.BLESSED_CHARGE);

            // Calls your centralized, pool-restricted public bridge method to consume 'blessed' and apply the upgrade
            ActiveEngravingEvaluator.finalizeBlessedGraduationProject(heldItem, serverPlayer);
        } else {
            heldItem.set(ModComponents.BLESSED_CHARGE, new BlessedCharge(updatedDevotion, charge.requiredPoints()));

            serverPlayer.getWorld().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.5f, 1.3f);
        }
    }

    // =========================================================================
    // PART 4: LEGACY SHRIVING STONE BACKEND UPGRADE AUXILIARIES
    // =========================================================================
    @Unique
    private void eyelisspartmod$awardKillBonus(ItemStack stack, ServerPlayerEntity player, int points) {
        BloodShrivingCharge c = stack.get(ModComponents.SHRIVING_CHARGE);
        if (c != null) {
            int next = Math.min(c.currentKills() + points, c.requiredKills());
            stack.set(ModComponents.SHRIVING_CHARGE, new BloodShrivingCharge(next, c.requiredKills()));
        }
    }

    @Unique
    private void eyelisspartmod$awardBlockBonus(ItemStack stack, ServerPlayerEntity player, int points) {
        BlockShrivingCharge c = stack.get(ModComponents.BLOCK_CHARGE);
        if (c != null) {
            int next = Math.min(c.currentBlocks() + points, c.requiredBlocks());
            stack.set(ModComponents.BLOCK_CHARGE, new BlockShrivingCharge(next, c.requiredBlocks()));
        }
    }

    @Unique
    private int eyelisspartmod$countWisdom(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        List<EngravingContents> list = stack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());
        return list.stream().filter(e -> e.engravingId().equals("wisdom")).mapToInt(EngravingContents::level).sum();
    }

    // =========================================================================
    // PART 5: SHATTERING BLESSING HARVEST SUITABILITY OVERRIDE
    // =========================================================================
    @Inject(method = "canHarvest(Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private void injectShatteringHarvestSuitability(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        ItemStack tool = player.getMainHandStack();
        if (tool.isEmpty()) return;

        List<EngravingContents> engravings = tool.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());
        boolean hasShattering = engravings.stream().anyMatch(e -> e.engravingId().equals("shattering"));

        if (hasShattering) {
            boolean isStandardToolBlock = state.isIn(net.minecraft.registry.tag.BlockTags.SHOVEL_MINEABLE)
                    || state.isIn(net.minecraft.registry.tag.BlockTags.PICKAXE_MINEABLE)
                    || state.isIn(net.minecraft.registry.tag.BlockTags.AXE_MINEABLE)
                    || state.isIn(net.minecraft.registry.tag.BlockTags.HOE_MINEABLE);

            // Force tool suitability to true on fragile glass blocks to apply tier break speeds natively
            if (!isStandardToolBlock && !state.isAir()) {
                cir.setReturnValue(true);
            }
        }
    }
}
