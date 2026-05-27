package eyeliss.particle.mod.block;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block SLOP_BLOCK = registerBlock("slop_block",
            new Block(
                    AbstractBlock.Settings.create()
                            .strength(2f)
                            .sounds(BlockSoundGroup.MUD)
                            .burnable()
            )
    );

    public static final Block COOKED_SLOP_BLOCK = registerBlock("cooked_slop_block",
            new Block(
                    AbstractBlock.Settings.create()
                            .strength(2f)
                            .sounds(BlockSoundGroup.MANGROVE_ROOTS)
                            .burnable()
            ));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(EyelisssParticleMod.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(EyelisssParticleMod.MOD_ID, name),
            new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks() {
        EyelisssParticleMod.LOGGER.info("Registering Mod Blocks for " + EyelisssParticleMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(ModBlocks.SLOP_BLOCK);
            entries.add(ModBlocks.COOKED_SLOP_BLOCK);
        });
    }
}
