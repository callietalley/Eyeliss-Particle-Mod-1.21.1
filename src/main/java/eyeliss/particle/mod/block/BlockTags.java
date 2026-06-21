package eyeliss.particle.mod.block;

import net.minecraft.block.Block;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockTags {
    public static final net.minecraft.registry.tag.TagKey<Block> EVAPORATES_OUTSIDE_SOURCE = net.minecraft.registry.tag.TagKey.of(
            net.minecraft.registry.RegistryKeys.BLOCK,
            Identifier.of("eyelisspartmod", "evaporates_outside_source")
    );

    // Unified helper method to handle the sizzle sound and smoke particles
    public static boolean tryEvaporate(World world, BlockPos pos, Block block) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            if (block.getDefaultState().isIn(EVAPORATES_OUTSIDE_SOURCE)) {
                Identifier targetDim = Identifier.of("eyelisspartmod", "the_source");

                if (!world.getRegistryKey().getValue().equals(targetDim)) {
                    // Play the custom fizzle sound
                    serverWorld.playSound(
                            null, pos,
                            SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS,
                            0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F
                    );

                    // Spawn a puff of smoke particles
                    for (int i = 0; i < 8; ++i) {
                        serverWorld.spawnParticles(
                                ParticleTypes.LARGE_SMOKE,
                                (double)pos.getX() + world.random.nextDouble(),
                                (double)pos.getY() + world.random.nextDouble(),
                                (double)pos.getZ() + world.random.nextDouble(),
                                1, 0.0D, 0.0D, 0.0D, 0.0D
                        );
                    }
                    return true; // Successfully evaporated!
                }
            }
        }
        return false; // Safe to place, do not evaporate.
    }
}