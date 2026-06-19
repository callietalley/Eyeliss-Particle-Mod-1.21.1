package eyeliss.particle.mod.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Set;

import static eyeliss.particle.mod.component.TrackingID.TRACKING_ID;

public class ItemDuplicationChecker {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ItemDuplicationChecker::checkInventories);
    }

    private static void checkInventories(MinecraftServer server) {
        Set<String> trackedIdsThisTick = new HashSet<>();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.isSpectator()) continue;

            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);

                if (stack.isEmpty()) continue;

                if (stack.contains(TRACKING_ID)) {
                    String trackingId = stack.get(TRACKING_ID);

                    if (trackingId == null) continue;

                    if (trackedIdsThisTick.contains(trackingId)) {

                        // 1. Play the lava burn sound directly at the player's current location before deleting it
                        player.getServerWorld().playSound(
                                null,
                                player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ENTITY_GENERIC_BURN, // The vanilla burn/lava sizzle sound
                                SoundCategory.PLAYERS,
                                1.0F, // Volume
                                1.0F  // Pitch
                        );

                        // 2. Delete the duplicate item stack out of their inventory slot
                        player.getInventory().setStack(i, ItemStack.EMPTY);
                        player.getInventory().markDirty();

                        String itemName = stack.getName().getString();

                        player.sendMessage(
                                Text.literal("[Security] ").copy().formatted(Formatting.RED, Formatting.BOLD)
                                        .append(Text.literal("Your " + itemName + " turned to ash! ").copy().formatted(Formatting.GRAY))
                                        .append(Text.literal("An item with this identical signature tracking ID already exists on the server.").formatted(Formatting.RED)),
                                false
                        );
                    } else {
                        trackedIdsThisTick.add(trackingId);
                    }
                }
            }
        }
    }
}