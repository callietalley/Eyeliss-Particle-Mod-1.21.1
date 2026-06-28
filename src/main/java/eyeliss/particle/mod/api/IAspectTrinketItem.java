package eyeliss.particle.mod.api;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public interface IAspectTrinketItem {
    /**
     * Triggers when pressing the SECONDARY trinket key (Default: V)
     */
    void onAspectKeybindPressed(ServerPlayerEntity player, ItemStack stack, boolean isSneaking);
}
