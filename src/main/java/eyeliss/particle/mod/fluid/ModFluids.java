package eyeliss.particle.mod.fluid;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModFluids {

    public class ModDamageTypes {
        public static final RegistryKey<DamageType> SOURCE_SAUCE_DAMAGE = RegistryKey.of(
                RegistryKeys.DAMAGE_TYPE,
                Identifier.of(EyelisssParticleMod.MOD_ID, "source_sauce_damage")
        );
    }

    public static final FlowableFluid STILL_SOURCE_SAUCE = Registry.register(
            Registries.FLUID,
            Identifier.of(EyelisssParticleMod.MOD_ID, "source_sauce"),
            new SourceSauceFluid.Still()
    );

    public static final FlowableFluid FLOWING_SOURCE_SAUCE = Registry.register(
            Registries.FLUID,
            Identifier.of(EyelisssParticleMod.MOD_ID, "flowing_source_sauce"),
            new SourceSauceFluid.Flowing()
    );

    public static final Block SOURCE_SAUCE_BLOCK = Registry.register(
            Registries.BLOCK,
            Identifier.of(EyelisssParticleMod.MOD_ID, "source_sauce_block"),
            new SourceSauceFluid.Block(STILL_SOURCE_SAUCE, AbstractBlock.Settings.copy(Blocks.LAVA))
    );

    public static final Item SOURCE_SAUCE_BUCKET = registerBucket("source_sauce_bucket", STILL_SOURCE_SAUCE);

    private static Item registerBucket(String name, FlowableFluid fluid) {
        return Registry.register(Registries.ITEM, Identifier.of(EyelisssParticleMod.MOD_ID, name),
                new BucketItem(fluid, new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1)));
    }

    public static void registerModFluids() {
        EyelisssParticleMod.LOGGER.info("Registering Mod Fluids for " + EyelisssParticleMod.MOD_ID);
    }
}
