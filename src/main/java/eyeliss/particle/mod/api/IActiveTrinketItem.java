package eyeliss.particle.mod.api;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public interface IActiveTrinketItem {
    /**
     * Triggers when pressing the PRIMARY trinket key (Default: Unknown/Unbound)
     */
    void onTrinketKeybindPressed(ServerPlayerEntity player, ItemStack stack, boolean isSneaking);
}
