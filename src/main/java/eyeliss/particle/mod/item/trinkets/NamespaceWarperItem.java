package eyeliss.particle.mod.item.trinkets;

import dev.emi.trinkets.api.TrinketItem;
import eyeliss.particle.mod.api.IAspectTrinketItem;
import eyeliss.particle.mod.screen.NamespaceWarperScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class NamespaceWarperItem extends TrinketItem implements IAspectTrinketItem {
    public NamespaceWarperItem(Settings settings) {
        super(settings);
    }

    @Override
    public void onAspectKeybindPressed(ServerPlayerEntity player, ItemStack stack, boolean isSneaking) {
        player.openHandledScreen(new ExtendedScreenHandlerFactory<NamespaceWarperScreenHandler.InitialSyncData>() {
            @Override
            public NamespaceWarperScreenHandler.InitialSyncData getScreenOpeningData(ServerPlayerEntity p) {
                NbtComponent component = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                NbtCompound nbt = component.copyNbt();
                return new NamespaceWarperScreenHandler.InitialSyncData(
                        nbt.getString("IdentityCustomName"),
                        nbt.getBoolean("IdentityHideNameplate")
                );
            }

            @Override
            public Text getDisplayName() {
                return Text.literal("Identity Alteration Terminal");
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                return new NamespaceWarperScreenHandler(syncId, playerInventory, getScreenOpeningData((ServerPlayerEntity) playerEntity));
            }
        });
    }
}
