package eyeliss.particle.mod.network;

import dev.emi.trinkets.api.TrinketsApi;
import eyeliss.particle.mod.item.trinkets.NamespaceWarperItem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class NamespaceWarperNetwork {

    @SuppressWarnings("unchecked")
    public static void initializePayloads() {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(
                NamespaceWarperPayloads.ApplyIdentityPayload.ID,
                NamespaceWarperPayloads.ApplyIdentityPayload.CODEC
        );
    }

    @SuppressWarnings("unchecked")
    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(NamespaceWarperPayloads.ApplyIdentityPayload.ID, (NamespaceWarperPayloads.ApplyIdentityPayload payload, ServerPlayNetworking.Context context) -> {
            var player = context.player();
            String chosenName = payload.customName().trim();
            boolean hideNameplate = payload.hideNameplate();

            context.server().execute(() -> {
                var compOpt = TrinketsApi.getTrinketComponent(player);
                if (compOpt.isPresent()) {
                    for (var equip : compOpt.get().getAllEquipped()) {
                        var stack = equip.getRight();
                        if (stack.getItem() instanceof NamespaceWarperItem) {
                            NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                            NbtCompound nbt = nbtComponent.copyNbt();

                            nbt.putString("IdentityCustomName", chosenName);
                            nbt.putBoolean("IdentityHideNameplate", hideNameplate);
                            nbt.putString("OwnerUUIDString", player.getUuidAsString());

                            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                            player.sendMessage(Text.literal("§b[Identity] Profile synchronized successfully."), true);
                            break;
                        }
                    }
                }

                PlayerListS2CPacket updatePacket = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
                context.server().getPlayerManager().sendToAll(updatePacket);

                player.getServerWorld().playSound(
                        null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS,
                        0.8f, 1.2f
                );
            });
        });
    }
}
