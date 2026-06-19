package eyeliss.particle.mod.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class RiftGemScreenHandler extends ScreenHandler {

    public RiftGemScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(RiftGemScreens.RIFT_GEM_SCREEN_HANDLER, syncId);
    }

    public RiftGemScreenHandler(int syncId, PlayerInventory playerInventory, ItemStack stack) {
        super(RiftGemScreens.RIFT_GEM_SCREEN_HANDLER, syncId);
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
