package eyeliss.particle.mod.recipe;

import com.mojang.serialization.MapCodec;
import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRecipes {

    public static final RecipeSerializer<SyringeLoadingRecipe> SYRINGE_LOADING_SERIALIZER =
            register("crafting_special_syringe_loading",
                    new SpecialRecipeSerializer<>(SyringeLoadingRecipe::new));

    public static RecipeType<WeaponSmithingRecipe> WEAPON_SMITHING_TYPE;
    public static RecipeSerializer<WeaponSmithingRecipe> WEAPON_SMITHING_SERIALIZER;

    public static RecipeSerializer<HardLimitedSmithingRecipe> HARD_LIMITED_SMITHING_SERIALIZER;

    private static <S extends RecipeSerializer<?>> S register(String id, S serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of("eyelisspartmod", id), serializer);
    }

    public static void registerRecipes() {
        EyelisssParticleMod.LOGGER.info("Registering Custom Recipes for " + EyelisssParticleMod.MOD_ID);

        // 4. Register the unique recipe category type channel (Shared by both standard and limited smithing)
        WEAPON_SMITHING_TYPE = Registry.register(Registries.RECIPE_TYPE,
                Identifier.of("eyelisspartmod", "weapon_smithing"),
                new RecipeType<>() {
                    @Override
                    public String toString() { return "weapon_smithing"; }
                });

        // 5. Register the Standard Weapon Smithing Codec Serializer
        WEAPON_SMITHING_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER,
                Identifier.of("eyelisspartmod", "weapon_smithing"),
                new RecipeSerializer<>() {
                    @Override
                    public MapCodec<WeaponSmithingRecipe> codec() { return WeaponSmithingRecipe.CODEC; }

                    @Override
                    public PacketCodec<RegistryByteBuf, WeaponSmithingRecipe> packetCodec() { return WeaponSmithingRecipe.PACKET_CODEC; }
                });

        // 6. NEW: Register the Hard Limited Weapon Smithing Codec Serializer
        HARD_LIMITED_SMITHING_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER,
                Identifier.of("eyelisspartmod", "hard_limited_smithing"),
                new RecipeSerializer<>() {
                    @Override
                    public MapCodec<HardLimitedSmithingRecipe> codec() { return HardLimitedSmithingRecipe.CODEC; }

                    @Override
                    public PacketCodec<RegistryByteBuf, HardLimitedSmithingRecipe> packetCodec() { return HardLimitedSmithingRecipe.PACKET_CODEC; }
                });
    }
}
