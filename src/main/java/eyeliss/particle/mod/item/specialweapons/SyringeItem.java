package eyeliss.particle.mod.item.specialweapons;

import eyeliss.particle.mod.component.ModComponents;
import eyeliss.particle.mod.component.SyringeContents;
import eyeliss.particle.mod.enchantment.ModEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class SyringeItem extends Item {
    private final int enchantability;

    public SyringeItem(int maxDurability, double attackDamage, double attackSpeed, int enchantability, Settings settings) {
        super(settings.maxDamage(maxDurability)
                .component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createSyringeAttributes(attackDamage, attackSpeed))
                .component(ModComponents.SYRINGE_CONTENTS, SyringeContents.EMPTY)
        );
        this.enchantability = enchantability;
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        World world = attacker.getWorld();
        SyringeContents contents = stack.getOrDefault(ModComponents.SYRINGE_CONTENTS, SyringeContents.EMPTY);

        Optional<RegistryEntry.Reference<Enchantment>> infusionEntry = world.getRegistryManager().getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getOptional(ModEnchantments.CHEMICAL_INFUSION);
        boolean hasInfusionEnchant = infusionEntry.isPresent() && net.minecraft.enchantment.EnchantmentHelper.getLevel(infusionEntry.get(), stack) > 0;

        Optional<RegistryEntry.Reference<Enchantment>> burstEntry = world.getRegistryManager().getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getOptional(ModEnchantments.CHEMICAL_BURST);
        boolean hasBurstEnchant = burstEntry.isPresent() && net.minecraft.enchantment.EnchantmentHelper.getLevel(burstEntry.get(), stack) > 0;

        if (hasInfusionEnchant) {
            target.damage(target.getDamageSources().mobAttack(attacker), 1.0F);
        }

        if (contents != null && !contents.payloads().isEmpty() && !contents.payloads().getFirst().effectId().equals("minecraft:empty") && contents.durationLeft() > 0) {
            boolean hasInstantEffect = false;

            for (SyringeContents.Payload payload : contents.payloads()) {
                String effectId = payload.effectId();
                if (effectId.equals("minecraft:empty")) continue;

                if (hasInfusionEnchant && (effectId.equals("minecraft:instant_health") || effectId.equals("minecraft:instant_damage"))) {
                    continue;
                }

                if (hasInfusionEnchant && attacker.getRandom().nextFloat() > 0.75f) continue;
                if (effectId.equals("minecraft:instant_health") || effectId.equals("minecraft:instant_damage")) {
                    hasInstantEffect = true;
                }

                net.minecraft.entity.effect.StatusEffect rawEffect = Registries.STATUS_EFFECT.get(Identifier.of(effectId));
                if (rawEffect != null) {
                    RegistryEntry statusEffectEntry = (RegistryEntry) Registries.STATUS_EFFECT.getEntry(rawEffect);

                    int baseDoseTicks = 15 * 20;
                    if (hasBurstEnchant) baseDoseTicks /= 2;

                    int doseTicksForThisEffect = baseDoseTicks;

                    if (effectId.equals("minecraft:instant_health") || effectId.equals("minecraft:instant_damage")) {
                        doseTicksForThisEffect = 2;
                    } else if (doseTicksForThisEffect > contents.durationLeft()) {
                        doseTicksForThisEffect = contents.durationLeft();
                    }

                    int baselineTargetDuration = 0;
                    int syringeAmplifier = payload.amplifier();

                    if (target.hasStatusEffect(statusEffectEntry)) {
                        StatusEffectInstance currentActiveEffect = target.getStatusEffect(statusEffectEntry);
                        if (currentActiveEffect != null) {
                            baselineTargetDuration = currentActiveEffect.getDuration();

                            if (!effectId.equals("minecraft:instant_health") && !effectId.equals("minecraft:instant_damage")) {
                                if (currentActiveEffect.getAmplifier() > syringeAmplifier) doseTicksForThisEffect /= 2;
                            }
                        }
                    }

                    int finalAmplifier = syringeAmplifier;
                    if (target.hasStatusEffect(statusEffectEntry)) {
                        StatusEffectInstance currentActiveEffect = target.getStatusEffect(statusEffectEntry);
                        if (currentActiveEffect != null && currentActiveEffect.getAmplifier() > syringeAmplifier) finalAmplifier = currentActiveEffect.getAmplifier();
                    }

                    target.addStatusEffect(new StatusEffectInstance(statusEffectEntry, baselineTargetDuration + doseTicksForThisEffect, finalAmplifier, false, true, true), attacker);

                    target.addStatusEffect(new StatusEffectInstance(statusEffectEntry, baselineTargetDuration + doseTicksForThisEffect, finalAmplifier, false, true, true), attacker);

                    if (!world.isClient() && world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                        // Spawns a burst of 15 swirling potion swirl particles tracking the target's height
                        serverWorld.spawnParticles(
                                rawEffect.isInstant() ? net.minecraft.particle.ParticleTypes.INSTANT_EFFECT : net.minecraft.particle.ParticleTypes.EFFECT,
                                target.getX(), target.getBodyY(0.5), target.getZ(),
                                15, 0.3, 0.4, 0.3, 0.05
                        );

                        // Plays the custom spell_engine sound at exactly 0.1 volume
                        world.playSound(
                                null,
                                target.getX(), target.getY(), target.getZ(),
                                net.minecraft.sound.SoundEvent.of(Identifier.of("spell_engine", "generic_poison_impact")),
                                net.minecraft.sound.SoundCategory.PLAYERS,
                                0.1F, // 🔊 Volume constraint
                                1.0F  // Pitch standard
                        );
                    }
                }
            }

            int baseDoseCost;
            if (hasInstantEffect) {
                baseDoseCost = contents.durationLeft() / 5;
                if (baseDoseCost < 1) baseDoseCost = contents.durationLeft();
            } else {
                baseDoseCost = 15 * 20;
                if (hasBurstEnchant) baseDoseCost /= 2;
            }

            if (baseDoseCost > contents.durationLeft()) baseDoseCost = contents.durationLeft();
            int newDurationLeft = contents.durationLeft() - baseDoseCost;

            if (newDurationLeft <= 0) {
                stack.set(ModComponents.SYRINGE_CONTENTS, SyringeContents.EMPTY);
                stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, hasInfusionEnchant ? createEnchantedSyringeAttributes() : createSyringeAttributes(3.0, -1.8));
            } else {
                stack.set(ModComponents.SYRINGE_CONTENTS, new SyringeContents(contents.payloads(), newDurationLeft));
                if (hasInfusionEnchant) stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, createEnchantedSyringeAttributes());
            }
        }

        // 1.21.1 EquipmentSlot Fix
        stack.damage(1, attacker, net.minecraft.entity.EquipmentSlot.MAINHAND);
        return true;
    }
    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public net.minecraft.util.UseAction getUseAction(ItemStack stack) {
        boolean hasRanged = stack.getEnchantments().getEnchantments().stream()
                .anyMatch(entry -> entry.matchesKey(ModEnchantments.RANGED_DELIVERY));
        return hasRanged ? net.minecraft.util.UseAction.BOW : net.minecraft.util.UseAction.NONE;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        boolean hasRanged = itemStack.getEnchantments().getEnchantments().stream()
                .anyMatch(entry -> entry.matchesKey(ModEnchantments.RANGED_DELIVERY));

        if (hasRanged) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(itemStack);
        }
        return TypedActionResult.pass(itemStack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTime) {
        if (!(user instanceof net.minecraft.entity.player.PlayerEntity player)) return;

        boolean hasRanged = stack.getEnchantments().getEnchantments().stream()
                .anyMatch(entry -> entry.matchesKey(ModEnchantments.RANGED_DELIVERY));
        if (!hasRanged) return;

        int chargeTicks = this.getMaxUseTime(stack, user) - remainingUseTime;
        if (chargeTicks < 7) return;

        SyringeContents contents = stack.getOrDefault(ModComponents.SYRINGE_CONTENTS, SyringeContents.EMPTY);

        Optional<RegistryEntry.Reference<Enchantment>> burstEntry = world.getRegistryManager().getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getOptional(ModEnchantments.CHEMICAL_BURST);
        boolean hasBurstEnchant = burstEntry.isPresent() && net.minecraft.enchantment.EnchantmentHelper.getLevel(burstEntry.get(), stack) > 0;

        Optional<RegistryEntry.Reference<Enchantment>> infusionEntry = world.getRegistryManager().getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getOptional(ModEnchantments.CHEMICAL_INFUSION);
        boolean hasInfusion = infusionEntry.isPresent() && net.minecraft.enchantment.EnchantmentHelper.getLevel(infusionEntry.get(), stack) > 0;

        boolean hasInstantEffect = contents.payloads().stream().anyMatch(p ->
                p.effectId().equals("minecraft:instant_health") || p.effectId().equals("minecraft:instant_damage")
        );

        if (hasInfusion && hasInstantEffect) {
            return;
        }

        int baseDoseCost;
        if (hasInstantEffect) {
            baseDoseCost = 2;
        } else {
            baseDoseCost = 15 * 20;
            if (hasBurstEnchant) baseDoseCost /= 2;
        }

        if (baseDoseCost > contents.durationLeft()) baseDoseCost = contents.durationLeft();
        int newDurationLeft = contents.durationLeft() - baseDoseCost;

        ItemStack projectileStack = stack.copy();

        if (newDurationLeft <= 0) {
            stack.set(ModComponents.SYRINGE_CONTENTS, SyringeContents.EMPTY);
            stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, hasInfusion ? createEnchantedSyringeAttributes() : createSyringeAttributes(3.0, -1.8));
        } else {
            stack.set(ModComponents.SYRINGE_CONTENTS, new SyringeContents(contents.payloads(), newDurationLeft));
        }

        // Spawn the custom projectile entity on the server
        if (!world.isClient()) {
            eyeliss.particle.mod.entity.ThrownSyringeEntity projectile = new eyeliss.particle.mod.entity.ThrownSyringeEntity(world, player, projectileStack);

            float pullProgress = net.minecraft.item.BowItem.getPullProgress(chargeTicks);
            float speed = pullProgress * 2.5F;

            projectile.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, speed, 1.0F);
            world.spawnEntity(projectile);

            // Cooldown overlay activation (12 seconds = 240 ticks)
            player.getItemCooldownManager().set(this, 240);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sound.SoundEvents.ENTITY_ARROW_SHOOT,
                net.minecraft.sound.SoundCategory.PLAYERS,
                1.0F, 1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);
    }

    private String toRomanNumeral(int number) {
        if (number < 1) return "";
        int[] values = {10, 9, 5, 4, 1};
        String[] romanLetters = {"X", "IX", "V", "IV", "I"};
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                number -= values[i];
                roman.append(romanLetters[i]);
            }
        }
        return roman.toString();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        SyringeContents contents = stack.getOrDefault(ModComponents.SYRINGE_CONTENTS, SyringeContents.EMPTY);
        boolean hasInfusionEnchant = false;
        boolean hasBurstEnchant = false;

        if (context.getRegistryLookup() != null) {
            Optional<RegistryEntry.Reference<Enchantment>> infusionEntry = context.getRegistryLookup().getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getOptional(ModEnchantments.CHEMICAL_INFUSION);
            if (infusionEntry.isPresent() && net.minecraft.enchantment.EnchantmentHelper.getLevel(infusionEntry.get(), stack) > 0) hasInfusionEnchant = true;
            Optional<RegistryEntry.Reference<Enchantment>> burstEntry = context.getRegistryLookup().getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getOptional(ModEnchantments.CHEMICAL_BURST);
            if (burstEntry.isPresent() && net.minecraft.enchantment.EnchantmentHelper.getLevel(burstEntry.get(), stack) > 0) hasBurstEnchant = true;
        }

        if (contents == null || contents.payloads().isEmpty() || contents.payloads().getFirst().effectId().equals("minecraft:empty") || contents.durationLeft() <= 0) {
            tooltip.add(Text.literal("Craft with any potion to").setStyle(Text.literal("").getStyle().withItalic(false).withExclusiveFormatting(Formatting.GRAY)));
            tooltip.add(Text.literal("apply it's effects impact.").setStyle(Text.literal("").getStyle().withItalic(false).withExclusiveFormatting(Formatting.GRAY)));
            tooltip.add(Text.literal("Empty Cargo").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        } else {
            // Check if any of the payloads contain an instant effect
            boolean hasInstantEffect = contents.payloads().stream().anyMatch(p ->
                    p.effectId().equals("minecraft:instant_health") || p.effectId().equals("minecraft:instant_damage")
            );

            tooltip.add(Text.literal(hasInfusionEnchant ? "Loaded Potions (75% Chance on Hit):" : "Loaded Potion:").formatted(Formatting.GOLD));

            int nextHitCost;
            if (hasInstantEffect) {
                // Instants drain 20% of the current maximum fluid capacity per use (10 / 5 = 2 ticks)
                nextHitCost = contents.durationLeft() / 5;
                if (nextHitCost < 1) nextHitCost = contents.durationLeft();
            } else {
                nextHitCost = 15 * 20;
                if (hasBurstEnchant) nextHitCost /= 2;
                if (nextHitCost > contents.durationLeft()) nextHitCost = contents.durationLeft();
            }

            for (SyringeContents.Payload payload : contents.payloads()) {
                if (payload.effectId().equals("minecraft:empty")) continue;
                net.minecraft.entity.effect.StatusEffect rawEffect = Registries.STATUS_EFFECT.get(Identifier.of(payload.effectId()));
                if (rawEffect != null) {
                    String romanLevel = toRomanNumeral(payload.amplifier() + 1);
                    tooltip.add(Text.empty().append(" • ").append(rawEffect.getName()).append(!romanLevel.isEmpty() ? " " + romanLevel : "").formatted(Formatting.BLUE));
                }
            }

            // 🧪 DYNAMIC INSTANT POTION TOOLTIP DISPLAY
            if (hasInstantEffect) {
                // Dynamically computes the remaining uses based on 2-tick intervals
                int usesLeft = (contents.durationLeft() + 1) / 2;
                if (usesLeft > 5) usesLeft = 5; // Caps visual layout safely

                tooltip.add(Text.literal(String.format("   Total Fluid Left: 0.5s (%d/5 Uses Remaining)", usesLeft)).formatted(Formatting.GRAY));
                tooltip.add(Text.literal("   Spent on Next Hit: -1 Use").formatted(Formatting.RED));
            } else {
                // Baseline layout for standard continuous status effects
                tooltip.add(Text.literal(String.format("   Total Fluid Left: %.1fs", contents.durationLeft() / 20.0)).formatted(Formatting.GRAY));
                tooltip.add(Text.literal(String.format("   Spent on Next Hit: -%.1fs", nextHitCost / 20.0)).formatted(Formatting.RED));
            }
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    public static AttributeModifiersComponent createEnchantedSyringeAttributes() {
        return AttributeModifiersComponent.builder()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID, 0.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(Item.BASE_ATTACK_SPEED_MODIFIER_ID, -1.8, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.of("item.attack_range"), 0.5, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                .build();
    }

    public static AttributeModifiersComponent createSyringeAttributes(double attackDamage, double attackSpeed) {
        return AttributeModifiersComponent.builder()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID, attackDamage, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(Item.BASE_ATTACK_SPEED_MODIFIER_ID, attackSpeed, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.of("item.attack_range"), 0.5, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                .build();
    }
}

