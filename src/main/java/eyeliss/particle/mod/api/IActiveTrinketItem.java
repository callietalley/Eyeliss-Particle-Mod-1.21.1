package eyeliss.particle.mod.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public interface IActiveTrinketItem {
    /**
     * Called on the server when the user presses the trinket keybind while this is equipped.
     * @param player The player who pressed the key.
     * @param stack The item stack instance of this trinket.
     * @param isSneaking True if the player was shifting.
     */
    void onTrinketKeybindPressed(ServerPlayerEntity player, ItemStack stack, boolean isSneaking);
}