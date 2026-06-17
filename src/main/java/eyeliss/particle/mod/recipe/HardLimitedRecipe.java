package eyeliss.particle.mod.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class HardLimitedRecipe extends ShapedRecipe {
    private final int recipeLimit;

    public HardLimitedRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe raw, ItemStack result, boolean showNotification, int recipeLimit) {
        super(group, category, raw, result, showNotification);
        this.recipeLimit = recipeLimit;
    }

    public static final MapCodec<HardLimitedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ShapedRecipe.Serializer.CODEC.codec().fieldOf("shaped_data").forGetter(recipe -> recipe),
            Codec.INT.optionalFieldOf("limit", 9999).forGetter(recipe -> recipe.recipeLimit)
    ).apply(instance, (parent, limit) -> new HardLimitedRecipe(
            parent.getGroup(), parent.getCategory(), getRawDataFromParent(parent), parent.getResult(null), parent.showNotification(), limit
    )));

    public static RawShapedRecipe getRawDataFromParent(ShapedRecipe parent) {
        try {
            java.lang.reflect.Field field = ShapedRecipe.class.getDeclaredField("raw");
            field.setAccessible(true);
            return (RawShapedRecipe) field.get(parent);
        } catch (Exception e) {
            EyelisssParticleMod.LOGGER.error("Failed to extract raw recipe data layout from parent via reflection", e);
            return null;
        }
    }

    public static MutableText getTranslatableName(String recipeId) {
        String langKey = "recipe." + recipeId.replace(":", ".");
        return Text.translatable(langKey);
    }

    public int getRecipeLimit() {
        return this.recipeLimit;
    }

    private boolean isLimitReached(CraftingRecipeInput inventory) {
        MinecraftServer server = EyelisssParticleMod.CURRENT_SERVER;

        if (server != null) {
            CraftCounterState state = CraftCounterState.getServerState(server);
            var entryOpt = server.getRecipeManager().getFirstMatch(net.minecraft.recipe.RecipeType.CRAFTING, inventory, server.getOverworld());
            if (entryOpt.isPresent()) {
                String id = entryOpt.get().id().toString();
                return state.getCraftCount(id) >= this.recipeLimit;
            }
        }
        return false;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput inventory, RegistryWrapper.WrapperLookup lookup) {
        if (isLimitReached(inventory)) {
            return getDepletedPlaceholder();
        }
        return super.craft(inventory, lookup);
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registries) {
        return super.getResult(registries);
    }

    public static ItemStack getDepletedPlaceholder() {
        var placeholderItem = Registries.ITEM.get(Identifier.of(EyelisssParticleMod.MOD_ID, "depleted_essence"));
        return new ItemStack(placeholderItem);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return LimitedRecipes.HARD_LIMITED_SERIALIZER;
    }
}
