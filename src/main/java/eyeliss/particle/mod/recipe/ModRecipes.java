package eyeliss.particle.mod.recipe;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer; // 💡 IMPORTED AS A TOP-LEVEL CLASS
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRecipes {

    public static final RecipeSerializer<SyringeLoadingRecipe> SYRINGE_LOADING_SERIALIZER =
            register("crafting_special_syringe_loading",
                    new SpecialRecipeSerializer<>(SyringeLoadingRecipe::new));

    private static <S extends RecipeSerializer<?>> S register(String id, S serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(EyelisssParticleMod.MOD_ID, id), serializer);
    }

    public static void registerRecipes() {
        EyelisssParticleMod.LOGGER.info("Registering Custom Recipes for " + EyelisssParticleMod.MOD_ID);
    }
}
