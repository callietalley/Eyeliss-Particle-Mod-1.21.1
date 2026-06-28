package eyeliss.particle.mod.network;

import dev.emi.trinkets.api.TrinketsApi;
import eyeliss.particle.mod.item.trinkets.NamespaceWarperItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class TrinketProfileUpdater {

    public static void tickPlayerProfiles(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player == null || !player.isAlive()) continue;

            var trinketComp = TrinketsApi.getTrinketComponent(player);

            if (trinketComp.isPresent()) {
                for (var equip : trinketComp.get().getAllEquipped()) {
                    var stack = equip.getRight();

                    if (stack.getItem() instanceof NamespaceWarperItem) {
                        NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                        NbtCompound nbt = nbtComponent.copyNbt();

                        boolean hideNameplate = nbt.getBoolean("IdentityHideNameplate");

                        if (hideNameplate) {
                            if (player.isCustomNameVisible()) player.setCustomNameVisible(false);
                        } else {
                            if (!player.isCustomNameVisible()) player.setCustomNameVisible(true);
                        }
                        break;
                    }
                }
            }
        }
    }
}
