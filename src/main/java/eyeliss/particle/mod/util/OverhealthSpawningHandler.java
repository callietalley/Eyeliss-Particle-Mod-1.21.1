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
        // =========================================================================
        //   TRACK 1: INFINITE INITIAL SHIELD SPAWNER (13% Chance)
        // =========================================================================
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!world.isClient() && entity instanceof LivingEntity livingEntity && !(livingEntity instanceof PlayerEntity)) {

                // Skip elite entities / bosses with 100 or more base health points
                if (livingEntity.getMaxHealth() >= 100.0f) return;

                var overhealthEntry = Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH);

                if (!livingEntity.hasStatusEffect(overhealthEntry)) {
                    if (livingEntity.getRandom().nextFloat() * 100.0f < 13.0f) {
                        StatusEffectInstance overhealthInstance = new StatusEffectInstance(
                                overhealthEntry,
                                StatusEffectInstance.INFINITE, // Lasts forever until damage breaks it
                                0, false, true
                        );
                        livingEntity.addStatusEffect(overhealthInstance);
                    }
                }
            }
        });

        // =========================================================================
        //   TRACK 2: 16-BLOCK PROXIMITY RADAR LOOP (Scoreboard Team Based)
        // =========================================================================
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            Scoreboard scoreboard = server.getScoreboard();
            Team orangeTeam = scoreboard.getTeam(ORANGE_TEAM_NAME);

            // Dynamically instantiate the Orange Radar Team container on the server if missing
            if (orangeTeam == null) {
                orangeTeam = scoreboard.addTeam(ORANGE_TEAM_NAME);
                orangeTeam.setColor(Formatting.GOLD); // GOLD displays as bright tactical Orange
                orangeTeam.setDisplayName(Text.literal("Overhealth Radar Orange"));
            }

            // Loop through all active server dimensions
            for (ServerWorld world : server.getWorlds()) {
                // FIXED: iterateEntities() cleanly resolves the compilation error on 1.21.1 Yarn!
                for (Entity entity : world.iterateEntities()) {
                    if (entity instanceof LivingEntity livingEntity && !(livingEntity instanceof PlayerEntity)) {

                        var overhealthEntry = Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH);
                        String scoreHolderName = livingEntity.getNameForScoreboard();

                        // Condition: Only evaluate mobs that actively possess the overhealth shield
                        if (livingEntity.hasStatusEffect(overhealthEntry)) {
                            boolean playerIsNear = false;

                            // Sweep nearby players in this world to check for the 16-block radius constraint
                            for (ServerPlayerEntity player : world.getPlayers()) {
                                // 16 blocks away = 16 * 16 = 256.0 squared distance points
                                if (livingEntity.squaredDistanceTo(player) <= 256.0) {
                                    playerIsNear = true;
                                    break;
                                }
                            }

                            if (playerIsNear) {
                                // Player is close! Add the mob string to the scoreboard team and enable the glow flag
                                if (!orangeTeam.getPlayerList().contains(scoreHolderName)) {
                                    scoreboard.addScoreHolderToTeam(scoreHolderName, orangeTeam);
                                }
                                livingEntity.setGlowing(true);
                            } else {
                                // Player walked away! Turn off the glow flag and clear them from the radar team container
                                if (orangeTeam.getPlayerList().contains(scoreHolderName)) {
                                    scoreboard.removeScoreHolderFromTeam(scoreHolderName, orangeTeam);
                                }
                                livingEntity.setGlowing(false);
                            }
                        } else {
                            // CLEANUP: If the shield broke completely, wipe them from the team tracking lists immediately
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
