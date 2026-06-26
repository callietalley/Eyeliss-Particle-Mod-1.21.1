package eyeliss.particle.mod.component;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.Optional;

public class ActiveEngravingEvaluator {

    private static final Identifier FORTITUDE_ID = Identifier.of("eyelisspartmod", "engraving_fortitude");
    private static final Identifier LIGHTWEIGHT_ID = Identifier.of("eyelisspartmod", "engraving_lightweight");
    private static final Identifier HASTED_MOD_ID = Identifier.of("eyelisspartmod", "engraving_hasted_spell_haste");

    public static void applyPlayerTickEffects(PlayerEntity player) {
        if (player.getWorld().isClient()) return;

        int activeFortitudeLevel = 0;
        int activeLightweightLevel = 0;
        int activeHastedLevelSum = 0;

        for (ItemStack armorStack : player.getArmorItems()) {
            List<EngravingContents> engravings = armorStack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());
            for (EngravingContents entry : engravings) {
                if (entry.engravingId().equals("fortitude")) activeFortitudeLevel += entry.level();
                else if (entry.engravingId().equals("lightweight")) activeLightweightLevel += entry.level();
                else if (entry.engravingId().equals("hasted")) activeHastedLevelSum += entry.level();
            }
        }

        // --- SUBROUTINE A: BALANCED FORTITUDE PROTECTION ENGINE ---
        Optional<RegistryEntry.Reference<EntityAttribute>> damageTakenAttrOpt = Registries.ATTRIBUTE.getEntry(Identifier.of("spell_engine", "damage_taken"));
        if (damageTakenAttrOpt.isPresent()) {
            EntityAttributeInstance instance = player.getAttributeInstance(damageTakenAttrOpt.get());
            if (instance != null) {
                instance.removeModifier(FORTITUDE_ID);
                if (activeFortitudeLevel > 0) {
                    double reductionValue = 0.025 + (activeFortitudeLevel - 1) * 0.0125;
                    instance.addTemporaryModifier(new EntityAttributeModifier(FORTITUDE_ID, -Math.min(0.95, reductionValue), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
            }
        }

        // --- SUBROUTINE B: BALANCED LIGHTWEIGHT SPEED TRACKER ENGINE ---
        EntityAttributeInstance speedInstance = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedInstance != null) {
            speedInstance.removeModifier(LIGHTWEIGHT_ID);
            if (activeLightweightLevel > 0) {
                double speedValue = 0.05 + (activeLightweightLevel - 1) * 0.025;
                speedInstance.addTemporaryModifier(new EntityAttributeModifier(LIGHTWEIGHT_ID, speedValue, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        // --- SUBROUTINE C: HASTED SCALING PROTECTION ---
        // Level 1 = +50.0% multiplier. Every additional level past 1 adds exactly a 12.5% increment increase
        Optional<RegistryEntry.Reference<EntityAttribute>> spellHasteAttrOpt = Registries.ATTRIBUTE.getEntry(Identifier.of("spell_power", "haste"));
        if (spellHasteAttrOpt.isPresent()) {
            EntityAttributeInstance hasteInstance = player.getAttributeInstance(spellHasteAttrOpt.get());
            if (hasteInstance != null) {
                hasteInstance.removeModifier(HASTED_MOD_ID);
                if (activeHastedLevelSum > 0) {
                    double finalHasteValueMultiplier = 0.50 + ((activeHastedLevelSum - 1) * 0.125);
                    hasteInstance.addTemporaryModifier(new EntityAttributeModifier(
                            HASTED_MOD_ID,
                            finalHasteValueMultiplier,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ));
                }
            }
        }
    }

    public static void handlePostAttackDamage(PlayerEntity player, LivingEntity target, DamageSource source, float amountDealt) {
        ItemStack weapon = player.getMainHandStack();
        List<EngravingContents> engravings = weapon.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());

        for (EngravingContents entry : engravings) {
            if (entry.engravingId().equals("absorption")) {
                float healPercent = 0.125f + (entry.level() - 1) * 0.075f;
                player.heal(amountDealt * healPercent);
                break;
            }
        }
    }

    public static float modifyIncomingArmorProtection(LivingEntity target, DamageSource source, float originalArmorValue) {
        if (source.getAttacker() instanceof PlayerEntity player) {
            ItemStack weapon = player.getMainHandStack();
            List<EngravingContents> engravings = weapon.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());

            for (EngravingContents entry : engravings) {
                if (entry.engravingId().equals("crushing")) {
                    float armorIgnorePercent = 0.15f + (entry.level() - 1) * 0.10f;
                    return originalArmorValue * (1.0f - Math.min(1.0f, armorIgnorePercent));
                }
            }
        }
        return originalArmorValue;
    }

    // =========================================================================
    // PART 2: GENERAL WEAPON ENGRAVINGS CALCULATOR ENGINE
    // =========================================================================
    public static float calculateCustomWeaponDamageModifiers(PlayerEntity player, LivingEntity target, DamageSource source, float baselineDamage) {
        ItemStack weapon = player.getMainHandStack();
        if (weapon.isEmpty() || source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_PROJECTILE)) {
            return baselineDamage;
        }

        List<EngravingContents> engravings = weapon.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());
        if (engravings.isEmpty()) return baselineDamage;

        float finalCalculatedDamage = baselineDamage;

        for (EngravingContents entry : engravings) {
            String id = entry.engravingId();
            int level = entry.level();

            // 1. MAGIC TOUCH: Converts 10% per level of physical damage into magic sub-packets
            if (id.equals("magic_touch")) {
                // FIX: Calibrated down from level * 0.25f to level * 0.10f
                // Level 1 = 10% (-0.10), Level 2 = 20% (-0.20), Level 3 = 30% (-0.30)
                float physicalLostReductionFactor = level * 0.10f;
                float physicalDamageKept = baselineDamage * (1.0f - Math.min(1.0f, physicalLostReductionFactor));
                float magicDamagePayload = baselineDamage * Math.min(1.0f, physicalLostReductionFactor);

                if (magicDamagePayload > 0 && !player.getWorld().isClient()) {
                    // Direct health mutation bypasses i-frames cleanly to preserve weapon knockback
                    target.setHealth(Math.max(0.0f, target.getHealth() - magicDamagePayload));
                }
                finalCalculatedDamage = physicalDamageKept;
            }

            // 2. EXORCISM: Lowers non-undead hits by -10% per lvl / Multiplies undead hits by +75% per lvl
            if (id.equals("exorcism")) {
                boolean isUndeadTarget = target.getType().isIn(net.minecraft.registry.tag.EntityTypeTags.UNDEAD);
                if (isUndeadTarget) {
                    finalCalculatedDamage = baselineDamage + (baselineDamage * (level * 0.5f));
                } else {
                    finalCalculatedDamage = baselineDamage * (1.0f - (level * 0.10f));
                }
            }

            // 3. TRUTH: Direct health deduction inflicts true bypass damage silently without clearing knockback tracking
            if (id.equals("truth")) {
                float trueDamagePayload = 0.0f;
                if (level < 3) {
                    trueDamagePayload = level * 1.0f;
                } else {
                    trueDamagePayload = 4.0f + (level - 3);
                }

                if (trueDamagePayload > 0 && !player.getWorld().isClient()) {
                    // Inflict true damage safely directly onto the target health map matrix
                    target.setHealth(Math.max(0.0f, target.getHealth() - trueDamagePayload));
                }
            }
        }

        return Math.max(0.0f, finalCalculatedDamage);
    }

    public static void finalizeBlessedGraduationProject(ItemStack heldItem, ServerPlayerEntity serverPlayer) {
        List<EngravingContents> current = new java.util.ArrayList<>(heldItem.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of()));

        // 1. Actively strip out the temporary "blessed" aura trait upon bar completion
        current.removeIf(e -> e.engravingId().equals("blessed"));

        // 2. Full index array of your mod's absolute blessing-type engraving IDs
        List<String> registeredBlessings = List.of(
                "restoration", "transcendence", "wisdom", "ethereal", "dwarven", "shattering", "magic_touch", "hasted"
        );

        List<String> eligibleBlessings = new java.util.ArrayList<>();

        // 3. FIX: Self-contained dynamic tag evaluation pass to clear outer resolution blockers!
        // We cross-reference the master blessing registry against your tool's active tags inline.
        for (String blessingId : registeredBlessings) {
            boolean alreadyHasIt = current.stream().anyMatch(e -> e.engravingId().equals(blessingId));

            if (!alreadyHasIt) {
                boolean isApplicable = false;

                // Safely read your pool entries internally via standard map checks
                // This prevents weapons from getting tool blessings, and tools from getting armor blessings!
                for (net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> tag : eyeliss.particle.mod.component.ModEngravings.getBlessingPools().keySet()) {
                    if (heldItem.isIn(tag)) {
                        List<String> poolContents = eyeliss.particle.mod.component.ModEngravings.getBlessingPools().get(tag);
                        if (poolContents != null && poolContents.contains(blessingId)) {
                            isApplicable = true;
                            break;
                        }
                    }
                }

                if (isApplicable) {
                    eligibleBlessings.add(blessingId);
                }
            }
        }

        // SCENARIO A: A valid, un-acquired blessing for this specific item type is available!
        if (!eligibleBlessings.isEmpty()) {
            String selectedBlessingId = eligibleBlessings.get(serverPlayer.getRandom().nextInt(eligibleBlessings.size()));

            current.add(new EngravingContents(selectedBlessingId, 1));
            heldItem.set(ModComponents.ENGRAVING_CONTENTS, current);

            // FIX: Force instant cache validation right as the Devotion bar finishes transmuting!
            if (heldItem.contains(net.minecraft.component.DataComponentTypes.ENCHANTMENTS)) {
                var enchants = heldItem.get(net.minecraft.component.DataComponentTypes.ENCHANTMENTS);
                heldItem.set(net.minecraft.component.DataComponentTypes.ENCHANTMENTS, enchants);
            } else {
                heldItem.set(net.minecraft.component.DataComponentTypes.ENCHANTMENTS, net.minecraft.component.type.ItemEnchantmentsComponent.DEFAULT);
            }

            serverPlayer.getWorld().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    net.minecraft.sound.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, net.minecraft.sound.SoundCategory.PLAYERS, 0.9f, 1.3f);
            return;
        }

        // SCENARIO B FALLBACK: Item already holds ALL blessings allowed for its tool group!
        heldItem.set(ModComponents.ENGRAVING_CONTENTS, current);
        serverPlayer.sendMessage(net.minecraft.text.Text.literal("This item already possesses all available blessings for its type! Rolling a standard trait instead...").formatted(net.minecraft.util.Formatting.GOLD), true);

        // Dynamic fallback pass: routes back to your core random rolling script so progression isn't lost
        java.util.Optional<String> fallbackRolledIdOpt = eyeliss.particle.mod.component.ModEngravings.rollEngravingFor(heldItem, serverPlayer.getRandom());
        if (fallbackRolledIdOpt.isPresent()) {
            String fallbackId = fallbackRolledIdOpt.get();
            boolean updated = false;
            for (int i = 0; i < current.size(); i++) {
                if (current.get(i).engravingId().equals(fallbackId)) {
                    int currentLevel = current.get(i).level();
                    int maxCap = eyeliss.particle.mod.component.ModEngravings.isBlessingOrCurse(fallbackId) ? 1 : 3;
                    if (currentLevel < maxCap) {
                        current.set(i, new EngravingContents(fallbackId, currentLevel + 1));
                        updated = true;
                    }
                    break;
                }
            }
            if (!updated) {
                current.add(new EngravingContents(fallbackId, 1));
            }
            heldItem.set(ModComponents.ENGRAVING_CONTENTS, current);
            serverPlayer.getWorld().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    net.minecraft.sound.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, net.minecraft.sound.SoundCategory.PLAYERS, 0.7f, 1.0f);
        }
    }
}
