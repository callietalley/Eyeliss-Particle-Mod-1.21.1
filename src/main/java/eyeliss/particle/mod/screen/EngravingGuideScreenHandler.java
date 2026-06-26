package eyeliss.particle.mod.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class EngravingGuideScreenHandler extends ScreenHandler {
    private final SimpleInventory inputInventory = new SimpleInventory(1) {
        @Override
        public void markDirty() {
            super.markDirty();
            EngravingGuideScreenHandler.this.onContentChanged(this);
        }
    };

    public EngravingGuideScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(1));
    }

    public EngravingGuideScreenHandler(int syncId, PlayerInventory playerInventory, SimpleInventory inventory) {
        super(ModScreenHandlers.ENGRAVING_GUIDE_HANDLER, syncId);

        this.addSlot(new Slot(inputInventory, 0, 73, 84) {
            @Override
            public int getMaxItemCount() { return 1; }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 75 + col * 18, 188 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 75 + col * 18, 246));
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.dropInventory(player, this.inputInventory);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (slotIndex == 0) {
                if (!this.insertItem(itemStack2, 1, 37, true)) return ItemStack.EMPTY;
            } else {
                if (!this.insertItem(itemStack2, 0, 1, false)) return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();
        }
        return itemStack;
    }

    public ItemStack getInspectedStack() {
        return this.slots.get(0).getStack();
    }
}
