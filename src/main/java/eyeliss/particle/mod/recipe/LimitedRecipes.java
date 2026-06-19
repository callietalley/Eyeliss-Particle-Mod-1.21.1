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
            return HardLimitedRecipe.CODEC; // Ensure your HardLimitedRecipe.CODEC matches both fields via Serialization MapCodecs
        }

        @Override
        public PacketCodec<net.minecraft.network.RegistryByteBuf, HardLimitedRecipe> packetCodec() {
            return PacketCodec.ofStatic(
                    (buf, recipe) -> {
                        ShapedRecipe.Serializer.PACKET_CODEC.encode(buf, recipe);
                        buf.writeInt(recipe.getGlobalLimit()); // Write 1
                        buf.writeInt(recipe.getPlayerLimit()); // Write 2
                    },
                    buf -> {
                        ShapedRecipe parent = ShapedRecipe.Serializer.PACKET_CODEC.decode(buf);
                        int globalLimit = buf.readInt(); // Read 1
                        int playerLimit = buf.readInt(); // Read 2

                        return new HardLimitedRecipe(
                                parent.getGroup(), parent.getCategory(),
                                HardLimitedRecipe.getRawDataFromParent(parent),
                                parent.getResult(buf.getRegistryManager()), // FIXED: Removed null crash exploit
                                parent.showNotification(),
                                globalLimit,
                                playerLimit
                        );
                    }
            );
        }
    };

    public static void registerLimits() {
        Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(EyelisssParticleMod.MOD_ID, "hard_limited"), HARD_LIMITED_SERIALIZER);
    }
}
