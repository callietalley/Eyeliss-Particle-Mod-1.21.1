package eyeliss.particle.mod.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static eyeliss.particle.mod.block.BlockTags.tryEvaporate;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void evaporateSolidBlocks(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        BlockItem item = (BlockItem) (Object) this;

        if (tryEvaporate(context.getWorld(), context.getBlockPos(), item.getBlock())) {
            PlayerEntity user = context.getPlayer();

            if (user != null && !user.isCreative()) {
                Hand activeHand = context.getHand();
                user.getStackInHand(activeHand).decrement(1);
            }

            cir.setReturnValue(ActionResult.success(context.getWorld().isClient));
        }
    }
}
