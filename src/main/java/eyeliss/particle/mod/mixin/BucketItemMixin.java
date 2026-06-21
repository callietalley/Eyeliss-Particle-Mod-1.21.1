package eyeliss.particle.mod.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static eyeliss.particle.mod.block.BlockTags.tryEvaporate;

@Mixin(BucketItem.class)
public class BucketItemMixin {
    @Shadow private net.minecraft.fluid.Fluid fluid;

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void evaporateFluidBuckets(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<net.minecraft.item.ItemStack>> cir) {
        BlockHitResult hitResult = ItemInvoker.callRaycast(world, user, RaycastContext.FluidHandling.NONE);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            net.minecraft.util.math.BlockPos targetPos = hitResult.getBlockPos().offset(hitResult.getSide());

            net.minecraft.block.Block fluidBlock = this.fluid.getDefaultState().getBlockState().getBlock();

            if (tryEvaporate(world, targetPos, fluidBlock)) {
                net.minecraft.item.ItemStack emptyBucket = new net.minecraft.item.ItemStack(net.minecraft.item.Items.BUCKET);
                cir.setReturnValue(TypedActionResult.success(emptyBucket, world.isClient()));
            }
        }
    }
}
