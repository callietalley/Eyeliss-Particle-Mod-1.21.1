package eyeliss.particle.mod.block;

import eyeliss.particle.mod.EyelisssParticleMod;
import eyeliss.particle.mod.sound.ModSounds;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final BlockSoundGroup SOURCE_BLOCK_SOUNDS = new BlockSoundGroup(
            1.0F, 1.0F,
            ModSounds.SOURCE_BLOCK_BREAK_EVENT,
            ModSounds.SOURCE_BLOCK_STEP_EVENT,
            ModSounds.SOURCE_BLOCK_PLACE_EVENT,
            ModSounds.SOURCE_BLOCK_HIT_EVENT,
            ModSounds.SOURCE_BLOCK_STEP_EVENT // Reusing step event for fall sounds
    );

    public static final Block SLOP_BLOCK = registerBlock("slop_block",
            new Block(
                    AbstractBlock.Settings.create()
                            .strength(2f)
                            .sounds(BlockSoundGroup.MUD)
                            .burnable()
            ));

    public static final Block COOKED_SLOP_BLOCK = registerBlock("cooked_slop_block",
            new Block(
                    AbstractBlock.Settings.create()
                            .strength(2f)
                            .sounds(BlockSoundGroup.MANGROVE_ROOTS)
                            .burnable()
            ));

    public static final Block SOURCE_BLOCK = registerBlock("source_block",
            new Block(
                    AbstractBlock.Settings.create()
                            .strength(2f)
                            .luminance(state -> 15)
                            .pistonBehavior(PistonBehavior.BLOCK)
                            .sounds(SOURCE_BLOCK_SOUNDS)
                            .postProcess((state, world, pos) -> true)
                            .emissiveLighting((state, world, pos) -> true)
            ));

    public static final Block DEEP_SOURCE_BLOCK = registerBlock("deep_source_block",
            new Block(
                    AbstractBlock.Settings.create()
                            .strength(-1.0f, 3600000.0f)
                            .dropsNothing()
                            .pistonBehavior(PistonBehavior.BLOCK)
                            .sounds(SOURCE_BLOCK_SOUNDS)
                            .postProcess((state, world, pos) -> true)
                            .emissiveLighting((state, world, pos) -> true)
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
    }
}
