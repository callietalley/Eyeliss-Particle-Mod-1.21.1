package eyeliss.particle.mod.fluid;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class SourceSauceFluid extends FlowableFluid {
    @Override
    public Fluid getFlowing() { return ModFluids.FLOWING_SOURCE_SAUCE; }

    @Override
    public Fluid getStill() { return ModFluids.STILL_SOURCE_SAUCE; }

    @Override
    public Item getBucketItem() {
        return ModFluids.SOURCE_SAUCE_BUCKET;
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == ModFluids.STILL_SOURCE_SAUCE || fluid == ModFluids.FLOWING_SOURCE_SAUCE;
    }

    @Override
    protected float getBlastResistance() { return 100.0F; }

    @Override
    public int getLevelDecreasePerBlock(WorldView world) { return 1; }

    @Override
    public int getTickRate(WorldView world) { return 4; }

    @Override
    protected boolean isInfinite(World world) {
        return true;
    }

    @Override
    protected int getMaxFlowDistance(WorldView world) {
        return 8;
    }

    @Nullable
    @Override
    protected ParticleEffect getParticle() {
        return null;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        Block.dropStacks(state, world, pos, world.getBlockEntity(pos));
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !fluid.matchesType(this);
    }

    @Override
    public Optional<SoundEvent> getBucketFillSound() {
        return Optional.of(SoundEvents.ITEM_BUCKET_FILL_LAVA);
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return ModFluids.SOURCE_SAUCE_BLOCK.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
    }

    public static class Block extends FluidBlock {
        private static int lastDamageTick = -1;
        private static int lastDamagedEntityId = -1;

        public Block(FlowableFluid fluid, Settings settings) {
            super(fluid, settings);
        }

        @Override
        public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
            if (!world.isClient && entity instanceof PlayerEntity player) {
                if (player.isCreative() || player.isSpectator()) {
                    return;
                }

                long currentTick = world.getTime();

                if (currentTick % 5 == 0) {

                    if (lastDamageTick == currentTick && lastDamagedEntityId == player.getId()) {
                        return;
                    }

                    net.minecraft.entity.damage.DamageSource source = new net.minecraft.entity.damage.DamageSource(
                            world.getRegistryManager().get(net.minecraft.registry.RegistryKeys.DAMAGE_TYPE)
                                    .entryOf(ModFluids.ModDamageTypes.SOURCE_SAUCE_DAMAGE)
                    );

                    if (player.damage(source, 1.0f)) {
                        world.playSound(
                                null,
                                player.getX(), player.getY(), player.getZ(),
                                net.minecraft.sound.SoundEvents.ENTITY_PLAYER_HURT_DROWN,
                                net.minecraft.sound.SoundCategory.PLAYERS,
                                1.0F,
                                0.2F
                        );
                    }

                    lastDamageTick = (int) currentTick;
                    lastDamagedEntityId = player.getId();
                }
            }
        }
    }

    public static class Still extends SourceSauceFluid {
        @Override public int getLevel(FluidState state) { return 8; }
        @Override public boolean isStill(FluidState state) { return true; }
    }

    public static class Flowing extends SourceSauceFluid {
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }
        @Override public int getLevel(FluidState state) { return state.get(LEVEL); }
        @Override public boolean isStill(FluidState state) { return false; }
    }
}
