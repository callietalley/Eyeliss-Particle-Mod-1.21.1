package eyeliss.particle.mod.util;

import eyeliss.particle.mod.item.ModItems; // Ensure this points to your item registry class!
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld; // Needed for casting
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BloodShardDropHandler {

    // 💡 Maps individual Player UUIDs to their unique kill counts
    private static final Map<UUID, Integer> PLAYER_KILLS = new HashMap<>();
    private static final int KILLS_REQUIRED = 100;

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((target, damageSource) -> {
            // Rule 1: Attacker must be a ServerPlayer, target must NOT be a player
            if (damageSource.getAttacker() instanceof ServerPlayerEntity player && !(target instanceof PlayerEntity)) {

                UUID playerId = player.getUuid();

                // Fetch current kills for this specific player, defaulting to 0 if new
                int currentKills = PLAYER_KILLS.getOrDefault(playerId, 0) + 1;
                PLAYER_KILLS.put(playerId, currentKills);

                // Trigger item reward on exactly their 100th kill
                if (currentKills >= KILLS_REQUIRED) {
                    PLAYER_KILLS.put(playerId, 0); // Reset only this player's tracker

                    // 1.21.1 FIX: Use player.getServerWorld() to get the correct ServerWorld instance
                    ServerWorld serverWorld = player.getServerWorld();

                    ItemStack shardStack = new ItemStack(ModItems.BLOOD_SHARD);
                    ItemEntity dropEntity = new ItemEntity(
                            serverWorld,
                            target.getX(), target.getY() + 0.5, target.getZ(),
                            shardStack
                    );

                    // Apply a physical velocity pop up toss
                    dropEntity.setVelocity(
                            (target.getRandom().nextDouble() - 0.5) * 0.1,
                            0.2,
                            (target.getRandom().nextDouble() - 0.5) * 0.1
                    );

                    serverWorld.spawnEntity(dropEntity);

                    // Notify the individual player above their hotbar
                    player.sendMessage(
                            Text.literal("A Blood Shard has materialized from your victims!")
                                    .formatted(Formatting.RED, Formatting.BOLD),
                            true // Displays in action bar
                    );
                }
            }
        });
    }
}
