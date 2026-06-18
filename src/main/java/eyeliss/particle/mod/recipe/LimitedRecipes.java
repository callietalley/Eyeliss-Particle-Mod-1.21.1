package eyeliss.particle.mod.recipe;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class LimitedRecipes {

    public static final RecipeSerializer<HardLimitedRecipe> HARD_LIMITED_SERIALIZER = new RecipeSerializer<>() {
        @Override
        public com.mojang.serialization.MapCodec<HardLimitedRecipe> codec() {
            return HardLimitedRecipe.CODEC;
        }

        @Override
        public PacketCodec<net.minecraft.network.RegistryByteBuf, HardLimitedRecipe> packetCodec() {
            return PacketCodec.ofStatic(
                    (buf, recipe) -> {
                        ShapedRecipe.Serializer.PACKET_CODEC.encode(buf, recipe);
                        buf.writeInt(recipe.getRecipeLimit());
                    },
                    buf -> {
                        ShapedRecipe parent = ShapedRecipe.Serializer.PACKET_CODEC.decode(buf);
                        int limit = buf.readInt();
                        return new HardLimitedRecipe(
                                parent.getGroup(), parent.getCategory(),
                                HardLimitedRecipe.getRawDataFromParent(parent),
                                parent.getResult(null), parent.showNotification(), limit
                        );
                    }
            );
        }
    };

    public static void registerLimits() {
        Registry.register(
                Registries.RECIPE_SERIALIZER,
                Identifier.of(EyelisssParticleMod.MOD_ID, "hard_limited"),
                HARD_LIMITED_SERIALIZER
        );
    }
}
