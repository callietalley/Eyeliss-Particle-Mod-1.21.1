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

public record WeaponSmithingRecipe(int order, List<ItemStack> ingredients, ItemStack output) implements Recipe<RecipeInput> {

    public static final MapCodec<WeaponSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("order").forGetter(WeaponSmithingRecipe::order), // Parses the new order field
            ItemStack.CODEC.listOf().fieldOf("ingredients").forGetter(WeaponSmithingRecipe::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(WeaponSmithingRecipe::output)
    ).apply(instance, WeaponSmithingRecipe::new));

    public static final PacketCodec<RegistryByteBuf, WeaponSmithingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, WeaponSmithingRecipe::order,
            ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()), WeaponSmithingRecipe::ingredients,
            ItemStack.PACKET_CODEC, WeaponSmithingRecipe::output,
            WeaponSmithingRecipe::new
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
    public RecipeSerializer<?> getSerializer() { return ModRecipes.WEAPON_SMITHING_SERIALIZER; }

    @Override
    public RecipeType<?> getType() { return ModRecipes.WEAPON_SMITHING_TYPE; }
}
