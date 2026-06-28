package eyeliss.particle.mod.network;

import dev.emi.trinkets.api.TrinketsApi;
import eyeliss.particle.mod.api.IActiveTrinketItem;
import eyeliss.particle.mod.api.IAspectTrinketItem;
import eyeliss.particle.mod.item.trinkets.HiddenCloakItem; // IMPORTED: Your new item class
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class TrinketKeybindNetwork {

    public static void initializePayloads() {
        PayloadTypeRegistry.playC2S().register(
                TrinketKeybindPayloads.OpenRiftScreenPayload.ID,
                TrinketKeybindPayloads.OpenRiftScreenPayload.CODEC
        );
        PayloadTypeRegistry.playC2S().register(
                TrinketKeybindPayloads.OpenAspectScreenPayload.ID,
                TrinketKeybindPayloads.OpenAspectScreenPayload.CODEC
        );
    }

    public static void registerServerReceivers() {

        ServerPlayNetworking.registerGlobalReceiver(TrinketKeybindPayloads.OpenRiftScreenPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                var component = TrinketsApi.getTrinketComponent(player);
                if (component.isEmpty()) return;

                for (var equip : component.get().getAllEquipped()) {
                    ItemStack stack = equip.getRight();
                    if (stack.getItem() instanceof IActiveTrinketItem activeTrinket) {
                        activeTrinket.onTrinketKeybindPressed(player, stack, payload.isSneaking());
                        return;
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TrinketKeybindPayloads.OpenAspectScreenPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                var component = TrinketsApi.getTrinketComponent(player);
                if (component.isEmpty()) return;

                for (var equip : component.get().getAllEquipped()) {
                    ItemStack stack = equip.getRight();
                    if (stack.getItem() instanceof IAspectTrinketItem aspectTrinket) {
                        aspectTrinket.onAspectKeybindPressed(player, stack, payload.isSneaking());
                        return;
                    }
                }
            });
        });
    }
}
