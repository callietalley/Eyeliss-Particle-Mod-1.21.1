package eyeliss.particle.mod.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.List;

public record HardLimitedSmithingRecipe(int order, int color, int globalLimit, int playerLimit, List<ItemStack> ingredients, ItemStack output) implements Recipe<RecipeInput> {

    public static final MapCodec<HardLimitedSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("order").forGetter(HardLimitedSmithingRecipe::order),
            Codec.INT.optionalFieldOf("color", 0).forGetter(HardLimitedSmithingRecipe::color),
            Codec.INT.fieldOf("global_limit").forGetter(HardLimitedSmithingRecipe::globalLimit),
            Codec.INT.fieldOf("player_limit").forGetter(HardLimitedSmithingRecipe::playerLimit),
            ItemStack.CODEC.listOf().fieldOf("ingredients").forGetter(HardLimitedSmithingRecipe::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(HardLimitedSmithingRecipe::output)
    ).apply(instance, HardLimitedSmithingRecipe::new));

    public static final PacketCodec<RegistryByteBuf, HardLimitedSmithingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, HardLimitedSmithingRecipe::order,
            PacketCodecs.VAR_INT, HardLimitedSmithingRecipe::color,
            PacketCodecs.VAR_INT, HardLimitedSmithingRecipe::globalLimit,
            PacketCodecs.VAR_INT, HardLimitedSmithingRecipe::playerLimit,
            ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()), HardLimitedSmithingRecipe::ingredients,
            ItemStack.PACKET_CODEC, HardLimitedSmithingRecipe::output,
            HardLimitedSmithingRecipe::new
    );

    @Override
    public boolean matches(RecipeInput input, World world) { return true; }

    @Override
    public ItemStack craft(RecipeInput input, RegistryWrapper.WrapperLookup registries) { return this.output.copy(); }

    @Override
    public boolean fits(int width, int height) { return true; }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registries) { return this.output; }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.HARD_LIMITED_SMITHING_SERIALIZER; }

    @Override
    public RecipeType<?> getType() { return ModRecipes.WEAPON_SMITHING_TYPE; }
}
