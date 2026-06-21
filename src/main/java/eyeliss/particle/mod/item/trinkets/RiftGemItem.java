package eyeliss.particle.mod.item.trinkets;

import dev.emi.trinkets.api.TrinketItem;
import eyeliss.particle.mod.api.IActiveTrinketItem;
import eyeliss.particle.mod.screen.RiftGemBindScreenHandler;
import eyeliss.particle.mod.screen.RiftGemScreenHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class RiftGemItem extends TrinketItem implements IActiveTrinketItem {

    public RiftGemItem(Settings settings) {
        super(settings);
    }

    @Override
    public void onTrinketKeybindPressed(ServerPlayerEntity player, ItemStack stack, boolean isSneaking) {
        if (isSneaking) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, inv, p) -> new RiftGemBindScreenHandler(syncId, inv, stack),
                    Text.literal("Anchor Configuration")
            ));
        } else {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, inv, p) -> new RiftGemScreenHandler(syncId, inv, stack),
                    Text.literal("Rift Terminal")
            ));
        }
    }
}
