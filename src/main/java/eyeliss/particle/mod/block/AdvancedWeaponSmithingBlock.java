package eyeliss.particle.mod.block;

import com.mojang.serialization.MapCodec;
import eyeliss.particle.mod.block.entity.AdvancedWeaponSmithingBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AdvancedWeaponSmithingBlock extends BlockWithEntity implements BlockEntityProvider {
    public static final MapCodec<AdvancedWeaponSmithingBlock> CODEC = createCodec(AdvancedWeaponSmithingBlock::new);

    private static final VoxelShape BASE_PEDESTAL = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 2.0, 15.0);

    private static final VoxelShape CENTER_PILLAR = Block.createCuboidShape(5.0, 2.0, 5.0, 11.0, 9.0, 11.0);

    private static final VoxelShape MAIN_BASIN   = Block.createCuboidShape(0.0, 9.0, 0.0, 16.0, 16.0, 16.0);

    private static final VoxelShape ADVANCED_SMITHING_SHAPE = VoxelShapes.union(
            BASE_PEDESTAL,
            CENTER_PILLAR,
            MAIN_BASIN
    );

    public AdvancedWeaponSmithingBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void appendTooltip(net.minecraft.item.ItemStack stack, net.minecraft.item.Item.TooltipContext context, java.util.List<net.minecraft.text.Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
        tooltip.add(net.minecraft.text.Text.translatable("tooltip.eyelisspartmod.advanced_weapon_smithing_table.desc").formatted(net.minecraft.util.Formatting.GRAY));

        tooltip.add(net.minecraft.text.Text.translatable("tooltip.eyelisspartmod.advanced_weapon_smithing_table.flavor").formatted(net.minecraft.util.Formatting.GOLD));

        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    @Deprecated
    public int getOpacity(BlockState state, net.minecraft.world.BlockView world, BlockPos pos) {
        return 0;
    }

    @Override
    @Deprecated
    public boolean isSideInvisible(BlockState state, BlockState neighborState, net.minecraft.util.math.Direction direction) {
        return false;
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    @Deprecated
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return ADVANCED_SMITHING_SHAPE;
    }


    @Override
    @Deprecated
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return ADVANCED_SMITHING_SHAPE;
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedWeaponSmithingBlockEntity(pos, state);
    }

    @Override
    protected ItemActionResult onUseWithItem(net.minecraft.item.ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AdvancedWeaponSmithingBlockEntity smithingEntity) {
                player.openHandledScreen(smithingEntity);
            }
        }
        return ItemActionResult.SUCCESS;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AdvancedWeaponSmithingBlockEntity smithingEntity) {
                player.openHandledScreen(smithingEntity);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AdvancedWeaponSmithingBlockEntity smithingEntity) {
                net.minecraft.util.ItemScatterer.spawn(world, pos, smithingEntity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
}
