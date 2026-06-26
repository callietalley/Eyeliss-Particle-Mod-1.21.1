package eyeliss.particle.mod.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ServerVeinMineTracker {
    // Stores the UUIDs of players who are actively holding down the key bind
    private static final Set<UUID> activeHolders = new HashSet<>();

    public static void initialize() {
        ServerPlayNetworking.registerGlobalReceiver(VeinMineKeyPayload.ID, (payload, context) -> {
            UUID playerUUID = context.player().getUuid();
            if (payload.isHeldDown()) {
                activeHolders.add(playerUUID);
            } else {
                activeHolders.remove(playerUUID);
            }
        });
    }

    public static boolean isPlayerHoldingKey(UUID playerUUID) {
        return activeHolders.contains(playerUUID);
    }
}
