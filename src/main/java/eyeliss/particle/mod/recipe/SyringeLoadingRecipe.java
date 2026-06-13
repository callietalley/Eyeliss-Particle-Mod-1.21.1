package eyeliss.particle.mod.recipe;

import eyeliss.particle.mod.component.ModComponents;
import eyeliss.particle.mod.component.SyringeContents;
import eyeliss.particle.mod.item.ModWeapons;
import eyeliss.particle.mod.item.specialweapons.SyringeItem;
import eyeliss.particle.mod.enchantment.ModEnchantments;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Optional;

public class SyringeLoadingRecipe extends SpecialCraftingRecipe {

    public SyringeLoadingRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        boolean foundSyringe = false;
        boolean foundPotion = false;
        ItemStack potionStack = ItemStack.EMPTY;
        ItemStack syringeStack = ItemStack.EMPTY;

        for (int i = 0; i < input.getSize(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (stack.isOf(ModWeapons.SYRINGE)) {
                if (foundSyringe) return false;
                foundSyringe = true;
                syringeStack = stack;
            } else if (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
                if (foundPotion) return false;
                foundPotion = true;
                potionStack = stack;
            } else {
                return false;
            }
        }

        if (foundSyringe && foundPotion) {
            Optional<net.minecraft.registry.entry.RegistryEntry.Reference<net.minecraft.enchantment.Enchantment>> enchantEntry =
                    world.getRegistryManager()
                            .getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                            .getOptional(ModEnchantments.CHEMICAL_INFUSION);

            boolean hasEnchantment = enchantEntry.isPresent() &&
                    net.minecraft.enchantment.EnchantmentHelper.getLevel(enchantEntry.get(), syringeStack) > 0;

            // ❌ INFUSION BLOCKER CHECK: Reject recipe if loading instant effects into an infused syringe
            PotionContentsComponent potionContents = potionStack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContents != null && hasEnchantment) {
                for (StatusEffectInstance effectInstance : potionContents.getEffects()) {
                    String rawEffectId = effectInstance.getEffectType().getKey()
                            .map(key -> key.getValue().toString())
                            .orElse("minecraft:empty");

                    if (rawEffectId.equals("minecraft:instant_health") || rawEffectId.equals("minecraft:instant_damage")) {
                        return false;
                    }
                }
            }

            SyringeContents contents = syringeStack.getOrDefault(ModComponents.SYRINGE_CONTENTS, SyringeContents.EMPTY);
            int maxSlots = hasEnchantment ? 3 : 1;

            ArrayList<SyringeContents.Payload> currentPayloads = new ArrayList<>(contents.payloads());
            currentPayloads.removeIf(p -> p.effectId().equals("minecraft:empty"));
            if (currentPayloads.size() >= maxSlots) {
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack syringeStack = ItemStack.EMPTY;
        ItemStack potionStack = ItemStack.EMPTY;

        for (int i = 0; i < input.getSize(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (stack.isOf(ModWeapons.SYRINGE)) {
                syringeStack = stack;
            } else if (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
                potionStack = stack;
            }
        }

        if (syringeStack.isEmpty() || potionStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        Optional<net.minecraft.registry.entry.RegistryEntry.Reference<net.minecraft.enchantment.Enchantment>> infusionEntry =
                lookup.getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getOptional(ModEnchantments.CHEMICAL_INFUSION);
        boolean hasEnchantment = infusionEntry.isPresent() && net.minecraft.enchantment.EnchantmentHelper.getLevel(infusionEntry.get(), syringeStack) > 0;

        // ❌ RUNTIME INFUSION BLOCKER SAFETY NET
        PotionContentsComponent potionContents = potionStack.get(DataComponentTypes.POTION_CONTENTS);
        if (potionContents != null && hasEnchantment) {
            for (StatusEffectInstance effectInstance : potionContents.getEffects()) {
                String rawEffectId = effectInstance.getEffectType().getKey()
                        .map(key -> key.getValue().toString())
                        .orElse("minecraft:empty");
                if (rawEffectId.equals("minecraft:instant_health") || rawEffectId.equals("minecraft:instant_damage")) {
                    return ItemStack.EMPTY;
                }
            }
        }

        ItemStack result = syringeStack.copyWithCount(1);

        Optional<net.minecraft.registry.entry.RegistryEntry.Reference<net.minecraft.enchantment.Enchantment>> burstEntry =
                lookup.getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT).getOptional(ModEnchantments.CHEMICAL_BURST);
        boolean hasBurst = burstEntry.isPresent() && net.minecraft.enchantment.EnchantmentHelper.getLevel(burstEntry.get(), syringeStack) > 0;

        if (potionContents != null) {
            SyringeContents currentContents = syringeStack.getOrDefault(ModComponents.SYRINGE_CONTENTS, SyringeContents.EMPTY);
            ArrayList<SyringeContents.Payload> updatedPayloads = new ArrayList<>(currentContents.payloads());

            updatedPayloads.removeIf(p -> p.effectId().equals("minecraft:empty"));

            int uniquePotionsBeforeCraft = updatedPayloads.size();
            int maxSlots = hasEnchantment ? 3 : 1;

            int addedDuration = 0;
            boolean allowedToInjectThisBottle = false;
            boolean isInstantPotion = false;

            if (uniquePotionsBeforeCraft < maxSlots) {
                allowedToInjectThisBottle = true;
            }

            if (allowedToInjectThisBottle) {
                for (StatusEffectInstance effectInstance : potionContents.getEffects()) {
                    String rawEffectId = effectInstance.getEffectType().getKey()
                            .map(key -> key.getValue().toString())
                            .orElse("minecraft:empty");

                    if (!rawEffectId.equals("minecraft:empty")) {
                        if (rawEffectId.equals("minecraft:instant_health") || rawEffectId.equals("minecraft:instant_damage")) {
                            isInstantPotion = true;
                        }

                        boolean alreadyExists = updatedPayloads.stream().anyMatch(p -> p.effectId().equals(rawEffectId));

                        if (!alreadyExists) {
                            int baseAmplifier = effectInstance.getAmplifier();
                            if (hasBurst) {
                                baseAmplifier += 1;
                            }
                            updatedPayloads.add(new SyringeContents.Payload(rawEffectId, baseAmplifier));
                        }

                        if (effectInstance.getDuration() > addedDuration) {
                            addedDuration = effectInstance.getDuration();
                        }
                    }
                }
            }

            if (hasBurst) {
                addedDuration = addedDuration / 2;
            }

            // 🧪 5-USE DURATION REDUCTION: Sets flat 10 ticks capacity (0.5 seconds)
            // This ensures that dividing by 5 reduces it exactly by 2 ticks per strike!
            int finalDurationPool;
            if (isInstantPotion) {
                finalDurationPool = 10;
            } else {
                finalDurationPool = currentContents.durationLeft() + addedDuration;
            }

            result.set(ModComponents.SYRINGE_CONTENTS, new SyringeContents(updatedPayloads, finalDurationPool));
        }

        if (hasEnchantment) {
            result.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, SyringeItem.createEnchantedSyringeAttributes());
        } else {
            result.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, SyringeItem.createSyringeAttributes(3.0, -1.8));
        }

        return result;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput input) {
        DefaultedList<ItemStack> remainders = DefaultedList.ofSize(input.getSize(), ItemStack.EMPTY);

        for (int i = 0; i < input.getSize(); ++i) {
            ItemStack stack = input.getStackInSlot(i);

            if (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
                remainders.set(i, new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        return remainders;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SYRINGE_LOADING_SERIALIZER;
    }
}
