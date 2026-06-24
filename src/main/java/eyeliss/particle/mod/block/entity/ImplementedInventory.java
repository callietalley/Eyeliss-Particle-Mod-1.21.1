package eyeliss.particle.mod.block.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories; // Added this import
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface ImplementedInventory extends Inventory {
    DefaultedList<ItemStack> getItems();

    @Override
    default int size() { return getItems().size(); }

    @Override
    default boolean isEmpty() {
        for (ItemStack stack : getItems()) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    default ItemStack getStack(int slot) { return getItems().get(slot); }

    @Override
    default ItemStack removeStack(int slot, int count) {
        ItemStack result = Inventories.splitStack(getItems(), slot, count);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    default ItemStack removeStack(int slot) { return Inventories.removeStack(getItems(), slot); }

    @Override
    default void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) stack.setCount(getMaxCountPerStack());
        markDirty();
    }

    @Override
    default void markDirty() {}

    @Override
    default boolean canPlayerUse(PlayerEntity player) { return true; }

    @Override
    default void clear() { getItems().clear(); }
}
