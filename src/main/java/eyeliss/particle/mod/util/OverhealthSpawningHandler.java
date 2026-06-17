package eyeliss.particle.mod.util;

import eyeliss.particle.mod.effect.ModEffects;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class OverhealthSpawningHandler {

    private static final String ORANGE_TEAM_NAME = "OH_Radar_Orange";

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!world.isClient() && entity instanceof LivingEntity livingEntity && !(livingEntity instanceof PlayerEntity)) {

                if (livingEntity.getMaxHealth() >= 100.0f) return;

                var overhealthEntry = Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH);

                if (!livingEntity.hasStatusEffect(overhealthEntry)) {
                    if (livingEntity.getRandom().nextFloat() * 100.0f < 13.0f) {
                        StatusEffectInstance overhealthInstance = new StatusEffectInstance(
                                overhealthEntry,
                                StatusEffectInstance.INFINITE,
                                0, false, true
                        );
                        livingEntity.addStatusEffect(overhealthInstance);
                    }
                }
            }
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            Scoreboard scoreboard = server.getScoreboard();
            Team orangeTeam = scoreboard.getTeam(ORANGE_TEAM_NAME);

            if (orangeTeam == null) {
                orangeTeam = scoreboard.addTeam(ORANGE_TEAM_NAME);
                orangeTeam.setColor(Formatting.GOLD);
                orangeTeam.setDisplayName(Text.literal("Overhealth Radar Orange"));
            }

            for (ServerWorld world : server.getWorlds()) {
                for (Entity entity : world.iterateEntities()) {
                    if (entity instanceof LivingEntity livingEntity && !(livingEntity instanceof PlayerEntity)) {

                        var overhealthEntry = Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH);
                        String scoreHolderName = livingEntity.getNameForScoreboard();

                        if (livingEntity.hasStatusEffect(overhealthEntry)) {
                            boolean playerIsNear = false;

                            for (ServerPlayerEntity player : world.getPlayers()) {
                                if (livingEntity.squaredDistanceTo(player) <= 256.0) {
                                    playerIsNear = true;
                                    break;
                                }
                            }

                            if (playerIsNear) {
                                if (!orangeTeam.getPlayerList().contains(scoreHolderName)) {
                                    scoreboard.addScoreHolderToTeam(scoreHolderName, orangeTeam);
                                }
                                livingEntity.setGlowing(true);
                            } else {
                                if (orangeTeam.getPlayerList().contains(scoreHolderName)) {
                                    scoreboard.removeScoreHolderFromTeam(scoreHolderName, orangeTeam);
                                }
                                livingEntity.setGlowing(false);
                            }
                        } else {
                            if (orangeTeam.getPlayerList().contains(scoreHolderName)) {
                                scoreboard.removeScoreHolderFromTeam(scoreHolderName, orangeTeam);
                                livingEntity.setGlowing(false);
                            }
                        }
                    }
                }
            }
        });
    }
}
