package eyeliss.particle.mod.item;

import eyeliss.particle.mod.EyelisssParticleMod;
import eyeliss.particle.mod.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static eyeliss.particle.mod.item.ModItems.POWER_CORE;
import static eyeliss.particle.mod.item.ModItems.SHADOW_TOUCHED_FEATHER;

public class ModItemGroups {
    public static final ItemGroup EYELISS_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(EyelisssParticleMod.MOD_ID, "eyeliss_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.SLOP))
                    .displayName(Text.translatable("itemgroup.eyelisspartmod.eyeliss_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.SLOP);
                        entries.add(ModItems.COOKED_SLOP);
                        entries.add(ModItems.RED_POWER_STONE);
                        entries.add(ModItems.BLUE_POWER_STONE);
                        entries.add(ModItems.GREEN_POWER_STONE);
                        entries.add(ModItems.YELLOW_POWER_STONE);
                        entries.add(ModItems.PURPLE_POWER_STONE);
                        entries.add(ModItems.BIRD_STONE);
                        entries.add(POWER_CORE);
                        entries.add(SHADOW_TOUCHED_FEATHER);

                    }).build());

    public static final ItemGroup EYELISS_BLOCKS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(EyelisssParticleMod.MOD_ID, "eyeliss_blocks"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModBlocks.SLOP_BLOCK))
                    .displayName(Text.translatable("itemgroup.eyelisspartmod.eyeliss_blocks"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.SLOP_BLOCK);
                        entries.add(ModBlocks.COOKED_SLOP_BLOCK);

                    }).build());



    public static void registerItemGroups() {
        EyelisssParticleMod.LOGGER.info("Registering Item Groups for " + EyelisssParticleMod.MOD_ID);
    }
}