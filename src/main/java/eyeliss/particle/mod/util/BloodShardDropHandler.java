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

    private static final Map<UUID, Integer> PLAYER_KILLS = new HashMap<>();
    private static final int KILLS_REQUIRED = 100;

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((target, damageSource) -> {
            if (damageSource.getAttacker() instanceof ServerPlayerEntity player && !(target instanceof PlayerEntity)) {

                UUID playerId = player.getUuid();

                int currentKills = PLAYER_KILLS.getOrDefault(playerId, 0) + 1;
                PLAYER_KILLS.put(playerId, currentKills);

                if (currentKills >= KILLS_REQUIRED) {
                    PLAYER_KILLS.put(playerId, 0);

                    ServerWorld serverWorld = player.getServerWorld();

                    ItemStack shardStack = new ItemStack(ModItems.BLOOD_SHARD);
                    ItemEntity dropEntity = new ItemEntity(
                            serverWorld,
                            target.getX(), target.getY() + 0.5, target.getZ(),
                            shardStack
                    );

                    dropEntity.setVelocity(
                            (target.getRandom().nextDouble() - 0.5) * 0.1,
                            0.2,
                            (target.getRandom().nextDouble() - 0.5) * 0.1
                    );

                    serverWorld.spawnEntity(dropEntity);

                    player.sendMessage(
                            Text.literal("A Blood Shard has materialized from your victims!")
                                    .formatted(Formatting.RED, Formatting.BOLD),
                            true
                    );
                }
            }
        });
    }
}
