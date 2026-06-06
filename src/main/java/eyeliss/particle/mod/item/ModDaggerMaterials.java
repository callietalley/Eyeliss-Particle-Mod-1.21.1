package eyeliss.particle.mod.item;

import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import java.util.function.Supplier;

public enum ModDaggerMaterials implements ToolMaterial {
    TRAINING_DAGGER(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 59, 2.0F, 0.0F, 15, () -> Ingredient.fromTag(net.minecraft.registry.tag.ItemTags.PLANKS)),
    COPPER_DAGGER(BlockTags.INCORRECT_FOR_STONE_TOOL, 160, 4.5F, 0.5F, 10, () -> Ingredient.ofItems(Items.COPPER_INGOT)),
    QUARTZ_DAGGER(BlockTags.INCORRECT_FOR_IRON_TOOL, 200, 6.0F, 1.25F, 18, () -> Ingredient.ofItems(Items.QUARTZ)),
    AMETHYST_DAGGER(BlockTags.INCORRECT_FOR_IRON_TOOL, 450, 6.5F, 1.75F, 25, () -> Ingredient.ofItems(Items.AMETHYST_SHARD)),
    EMERALD_DAGGER(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1200, 7.5F, 2.0F, 14, () -> Ingredient.ofItems(Items.EMERALD)),
    NETHERITE_DAGGER(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2031, 9.0F, 3.0F, 15, () -> Ingredient.ofItems(Items.NETHERITE_INGOT));

    private final TagKey<Block> inverseTag;
    private final int itemDurability;
    private final float miningSpeed;
    private final float attackDamage;
    private final int enchantability;
    private final Supplier<Ingredient> repairIngredient;

    ModDaggerMaterials(TagKey<Block> inverseTag, int itemDurability, float miningSpeed, float attackDamage, int enchantability, Supplier<Ingredient> repairIngredient) {
        this.inverseTag = inverseTag;
        this.itemDurability = itemDurability;
        this.miningSpeed = miningSpeed;
        this.attackDamage = attackDamage;
        this.enchantability = enchantability;
        this.repairIngredient = repairIngredient;
    }

    // 2. Override interface methods to feed attributes seamlessly to our weapon objects
    @Override
    public int getDurability() {
        return this.itemDurability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return this.miningSpeed;
    }

    @Override
    public float getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public TagKey<Block> getInverseTag() {
        return this.inverseTag;
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
}