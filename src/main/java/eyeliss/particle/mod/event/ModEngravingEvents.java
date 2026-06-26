package eyeliss.particle.mod.event;

import eyeliss.particle.mod.component.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModEngravingEvents {

    // Unique tracking identifier token for your mod's reach calculation layer
    private static final Identifier ETHEREAL_REACH_ID = Identifier.of("eyelisspartmod", "ethereal_block_reach");

    public static void registerEvents() {

        // --- 1. CONTINUOUS SERVER TICK HOOK: Armor effects & Ethereal +2 Reach ---
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ActiveEngravingEvaluator.applyPlayerTickEffects(player);

                // Check if the player is holding an active Ethereal tool in their main hand
                ItemStack mainHand = player.getMainHandStack();
                boolean hasEthereal = false;

                if (!mainHand.isEmpty()) {
                    List<EngravingContents> engravings = mainHand.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());
                    for (EngravingContents e : engravings) {
                        if (e.engravingId().equals("ethereal")) {
                            hasEthereal = true;
                            break;
                        }
                    }
                }

                // Fetch the player's active block interaction range attribute instance
                EntityAttributeInstance reachInstance = player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE);
                if (reachInstance != null) {
                    // Always clear existing modifiers to handle real-time hotbar scrolling shifts
                    reachInstance.removeModifier(ETHEREAL_REACH_ID);

                    // Apply a temporary +2.0 reach modifier if the weapon is active
                    if (hasEthereal) {
                        reachInstance.addTemporaryModifier(new EntityAttributeModifier(
                                ETHEREAL_REACH_ID,
                                2.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ));
                    }
                }
            }
        });

        // --- 2. COMBAT HIT HOOK: Handles post-attack life siphoning arrays ---
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, amount, damage, fatal) -> {
            if (source.getAttacker() instanceof PlayerEntity player) {
                ActiveEngravingEvaluator.handlePostAttackDamage(player, entity, source, damage);
            }
        });

        // --- 3. MOB DEATH HOOK: Handles kill-based Shriving Stones ONLY ---
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (source.getAttacker() instanceof ServerPlayerEntity player) {

                // Evaluate the slain entity's baseline health pools to dictate precise tier brackets
                float maxHealth = entity.getMaxHealth();
                int pointsEarned = 1;

                if (maxHealth >= 10.0f && maxHealth < 20.0f) {
                    pointsEarned = 2;
                } else if (maxHealth >= 20.0f && maxHealth < 50.0f) {
                    pointsEarned = 4;
                } else if (maxHealth >= 50.0f && maxHealth < 100.0f) {
                    pointsEarned = 8;
                } else if (maxHealth >= 100.0f) {
                    pointsEarned = 20;
                }

                // Track all kill-chargeable items equipped across main hand, off hand, and armor layers
                List<ItemStack> chargeableItems = new ArrayList<>();
                ItemStack mainHand = player.getMainHandStack();
                if (!mainHand.isEmpty() && mainHand.contains(ModComponents.SHRIVING_CHARGE)) {
                    chargeableItems.add(mainHand);
                }
                ItemStack offHand = player.getOffHandStack();
                if (!offHand.isEmpty() && offHand.contains(ModComponents.SHRIVING_CHARGE)) {
                    if (player.getRandom().nextFloat() < 0.50f) chargeableItems.add(offHand);
                }
                for (ItemStack armorStack : player.getArmorItems()) {
                    if (!armorStack.isEmpty() && armorStack.contains(ModComponents.SHRIVING_CHARGE)) {
                        if (player.getRandom().nextFloat() < 0.50f) chargeableItems.add(armorStack);
                    }
                }

                // Process points distribution across eligible gear layouts
                for (ItemStack targetStack : chargeableItems) {
                    BloodShrivingCharge charge = targetStack.get(ModComponents.SHRIVING_CHARGE);
                    if (charge == null) continue;

                    int newKillsTotal = charge.currentKills() + pointsEarned;

                    // Upgrade Milestone Threshold Trigger Reached!
                    if (newKillsTotal >= charge.requiredKills()) {
                        List<EngravingContents> currentList = new ArrayList<>(targetStack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of()));

                        String selectedEngravingId = null;
                        boolean foundValidUpgrade = false;

                        // Run up to 20 re-roll attempts to find an engraving that isn't already maxed out
                        for (int attempt = 0; attempt < 20; attempt++) {
                            Optional<String> rolledIdOpt = ModEngravings.rollEngravingFor(targetStack, player.getRandom());
                            if (rolledIdOpt.isEmpty()) break;

                            String rolledId = rolledIdOpt.get();
                            int currentLevelOfRolled = 0;

                            for (EngravingContents e : currentList) {
                                if (e.engravingId().equals(rolledId)) {
                                    currentLevelOfRolled = e.level();
                                    break;
                                }
                            }

                            // Enforce strict level ceilings (Blessings/Curses: 1, Normal: 3)
                            int maxCeilingLimit = ModEngravings.isBlessingOrCurse(rolledId) ? 1 : 3;

                            if (currentLevelOfRolled < maxCeilingLimit) {
                                selectedEngravingId = rolledId;
                                foundValidUpgrade = true;
                                break;
                            }
                        }

                        // EDGE CASE PROTECTOR: If everything is fully maxed out, freeze the stone safely at max-1
                        if (!foundValidUpgrade) {
                            player.sendMessage(Text.literal("This item has reached its absolute maximum engraving potential!").formatted(Formatting.RED), true);
                            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                    SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.5f, 1.5f);

                            targetStack.set(ModComponents.SHRIVING_CHARGE, new BloodShrivingCharge(charge.requiredKills() - 1, charge.requiredKills()));
                            continue;
                        }

                        // Strip out the upgrade tracker component
                        targetStack.remove(ModComponents.SHRIVING_CHARGE);

                        // Append or upgrade the validated engraving safely
                        boolean updatedExisting = false;
                        for (int i = 0; i < currentList.size(); i++) {
                            if (currentList.get(i).engravingId().equals(selectedEngravingId)) {
                                currentList.set(i, new EngravingContents(selectedEngravingId, currentList.get(i).level() + 1));
                                updatedExisting = true;
                                break;
                            }
                        }
                        if (!updatedExisting) {
                            currentList.add(new EngravingContents(selectedEngravingId, 1));
                        }

                        targetStack.set(ModComponents.ENGRAVING_CONTENTS, currentList);

                        // FIX: Force instant client-side cache refresh loop upon item mutation
                        if (targetStack.contains(DataComponentTypes.ENCHANTMENTS)) {
                            var enchants = targetStack.get(DataComponentTypes.ENCHANTMENTS);
                            targetStack.set(DataComponentTypes.ENCHANTMENTS, enchants);
                        } else {
                            targetStack.set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
                        }

                        // FIX: Secure loop placement inside active stack scope to mount Blessed progress bars instantly
                        if (selectedEngravingId != null && selectedEngravingId.equalsIgnoreCase("blessed") && !targetStack.contains(ModComponents.BLESSED_CHARGE)) {
                            targetStack.set(ModComponents.BLESSED_CHARGE, new BlessedCharge(0, 300));
                        }

                        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.7f, 1.0f);
                    } else {
                        targetStack.set(ModComponents.SHRIVING_CHARGE, new BloodShrivingCharge(newKillsTotal, charge.requiredKills()));
                    }
                }
            }
        });

        // --- 4. BLOCK HARVEST HOOK: Handles Geologic Shriving Stones ONLY ---
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) return;

            List<ItemStack> blockProcessingTargets = new ArrayList<>();

            // 1. Main hand item gets direct processing priority
            ItemStack mainHandTool = serverPlayer.getMainHandStack();
            if (!mainHandTool.isEmpty() && mainHandTool.contains(ModComponents.BLOCK_CHARGE)) {
                blockProcessingTargets.add(mainHandTool);
            }

            // 2. Off-hand item rolls an independent 50% siphon chance ONLY if the main hand ALSO has it equipped
            ItemStack offHandTool = serverPlayer.getOffHandStack();
            if (!offHandTool.isEmpty() && offHandTool.contains(ModComponents.BLOCK_CHARGE) && mainHandTool.contains(ModComponents.BLOCK_CHARGE)) {
                if (serverPlayer.getRandom().nextFloat() < 0.50f) {
                    blockProcessingTargets.add(offHandTool);
                }
            }

            // Process progressive block counters across all eligible targets
            for (ItemStack targetStack : blockProcessingTargets) {
                BlockShrivingCharge blockCharge = targetStack.get(ModComponents.BLOCK_CHARGE);
                if (blockCharge == null) continue;

                int nextBlocksCount = blockCharge.currentBlocks() + 1;

                // Milestone Ceiling Trigger Reached!
                if (nextBlocksCount >= blockCharge.requiredBlocks()) {
                    List<EngravingContents> currentList = new ArrayList<>(targetStack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of()));

                    String selectedId = null;
                    boolean foundValidUpgrade = false;

                    for (int loop = 0; loop < 20; loop++) {
                        Optional<String> rolledOpt = ModEngravings.rollEngravingFor(targetStack, serverPlayer.getRandom());
                        if (rolledOpt.isEmpty()) break;

                        String rolledId = rolledOpt.get();
                        int level = currentList.stream().filter(e -> e.engravingId().equals(rolledId)).map(EngravingContents::level).findFirst().orElse(0);
                        int ceiling = ModEngravings.isBlessingOrCurse(rolledId) ? 1 : 3;

                        if (level < ceiling) {
                            selectedId = rolledId;
                            foundValidUpgrade = true;
                            break;
                        }
                    }

                    if (!foundValidUpgrade) {
                        serverPlayer.sendMessage(Text.literal("This item has reached its absolute maximum engraving potential!").formatted(Formatting.RED), true);
                        serverPlayer.getWorld().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                                SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.5f, 1.5f);

                        targetStack.set(ModComponents.BLOCK_CHARGE, new BlockShrivingCharge(blockCharge.requiredBlocks() - 1, blockCharge.requiredBlocks()));
                        continue;
                    }

                    targetStack.remove(ModComponents.BLOCK_CHARGE);

                    boolean updated = false;
                    for (int idx = 0; idx < currentList.size(); idx++) {
                        if (currentList.get(idx).engravingId().equals(selectedId)) {
                            currentList.set(idx, new EngravingContents(selectedId, currentList.get(idx).level() + 1));
                            updated = true;
                            break;
                        }
                    }
                    if (!updated) currentList.add(new EngravingContents(selectedId, 1));
                    targetStack.set(ModComponents.ENGRAVING_CONTENTS, currentList);

                    // FIX: Force instant client-side cache refresh loop upon item mutation
                    if (targetStack.contains(DataComponentTypes.ENCHANTMENTS)) {
                        var enchants = targetStack.get(DataComponentTypes.ENCHANTMENTS);
                        targetStack.set(DataComponentTypes.ENCHANTMENTS, enchants);
                    } else {
                        targetStack.set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
                    }

                    // FIX: Secure loop placement inside active stack scope to mount Blessed progress bars instantly
                    if (selectedId != null && selectedId.equalsIgnoreCase("blessed") && !targetStack.contains(ModComponents.BLESSED_CHARGE)) {
                        targetStack.set(ModComponents.BLESSED_CHARGE, new BlessedCharge(0, 300));
                    }

                    serverPlayer.getWorld().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                            SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.7f, 1.2f);
                } else {
                    targetStack.set(ModComponents.BLOCK_CHARGE, new BlockShrivingCharge(nextBlocksCount, blockCharge.requiredBlocks()));
                }
            }
        });
    }
}
