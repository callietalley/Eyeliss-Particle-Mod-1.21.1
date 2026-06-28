package eyeliss.particle.mod.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.screen.ScreenHandler;

public class NamespaceWarperScreenHandler extends ScreenHandler {
    private final String savedCustomName;
    private final boolean savedHideNameplate;

    public record InitialSyncData(String name, boolean hide) {
        public static final PacketCodec<RegistryByteBuf, InitialSyncData> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, InitialSyncData::name,
                PacketCodecs.BOOL, InitialSyncData::hide,
                InitialSyncData::new
        );
    }

    public NamespaceWarperScreenHandler(int syncId, PlayerInventory playerInventory, InitialSyncData data) {
        super(ModScreenHandlers.NAMESPACE_WARPER_HANDLER, syncId);
        this.savedCustomName = data.name();
        this.savedHideNameplate = data.hide();
    }

    public String getSavedCustomName() {
        return this.savedCustomName != null ? this.savedCustomName : "";
    }

    public boolean getSavedHideNameplate() {
        return this.savedHideNameplate;
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
