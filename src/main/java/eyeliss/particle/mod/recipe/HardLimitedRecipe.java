package eyeliss.particle.mod.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.text.Text;

public class HardLimitedRecipe extends ShapedRecipe {
    private final int globalLimit;
    private final int playerLimit;

    // FIXED: Removed the '. Ding()' typo and explicitly typed the lambda instance parameter to fix ambiguity errors
    public static final MapCodec<HardLimitedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
            CraftingRecipeCategory.CODEC.fieldOf("category").forGetter(ShapedRecipe::getCategory),
            RawShapedRecipe.CODEC.forGetter(HardLimitedRecipe::getRawDataFromParent),
            ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.getResult(null)),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification),
            Codec.INT.fieldOf("global_limit").forGetter(HardLimitedRecipe::getGlobalLimit),
            Codec.INT.fieldOf("player_limit").forGetter(HardLimitedRecipe::getPlayerLimit)
    ).apply(instance, HardLimitedRecipe::new));

    public HardLimitedRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe rawOptions, ItemStack result, boolean showNotification, int globalLimit, int playerLimit) {
        super(group, category, rawOptions, result, showNotification);
        this.globalLimit = globalLimit;
        this.playerLimit = playerLimit;
    }

    public int getGlobalLimit() {
        return this.globalLimit;
    }

    public int getPlayerLimit() {
        return this.playerLimit;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return LimitedRecipes.HARD_LIMITED_SERIALIZER;
    }

    public static ItemStack getDepletedPlaceholder() {
        net.minecraft.item.Item depletedEssence = eyeliss.particle.mod.item.ModItems.DEPLETED_ESSENCE;

        ItemStack placeholderStack = new ItemStack(depletedEssence);
        return placeholderStack;
    }

    public static Text getTranslatableName(String recipeId) {
        return Text.literal(recipeId.substring(recipeId.indexOf(":") + 1).replace("_", " "));
    }

    public static RawShapedRecipe getRawDataFromParent(ShapedRecipe recipe) {
        try {
            java.lang.reflect.Field rawField = ShapedRecipe.class.getDeclaredField("raw");
            rawField.setAccessible(true);
            return (RawShapedRecipe) rawField.get(recipe);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract raw data from ShapedRecipe context", e);
        }
    }
}
