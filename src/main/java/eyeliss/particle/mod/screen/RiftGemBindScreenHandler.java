package eyeliss.particle.mod.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class RiftGemBindScreenHandler extends ScreenHandler {

    public RiftGemBindScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(RiftGemScreens.RIFT_GEM_BIND_HANDLER, syncId);
    }

    public RiftGemBindScreenHandler(int syncId, PlayerInventory playerInventory, ItemStack stack) {
        super(RiftGemScreens.RIFT_GEM_BIND_HANDLER, syncId);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }
}
