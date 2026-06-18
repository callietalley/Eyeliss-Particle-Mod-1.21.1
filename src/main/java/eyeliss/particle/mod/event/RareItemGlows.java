package eyeliss.particle.mod.event;

import eyeliss.particle.mod.EyelisssParticleMod;
import eyeliss.particle.mod.component.ModComponents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class RareItemGlows {

    private static final String PURPLE_TEAM_NAME = "purple_glow_team";
    private static final String RED_TEAM_NAME = "red_glow_team";

    private static final TagKey<Item> PURPLE_GLOW_TAG = TagKey.of(
            RegistryKeys.ITEM,
            Identifier.of(EyelisssParticleMod.MOD_ID, "glows_purple")
    );

    private static final TagKey<Item> RED_GLOW_TAG = TagKey.of(
            RegistryKeys.ITEM,
            Identifier.of(EyelisssParticleMod.MOD_ID, "glows_red")
    );

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            Scoreboard scoreboard = server.getScoreboard();

            Team purpleTeam = scoreboard.getTeam(PURPLE_TEAM_NAME);
            if (purpleTeam == null) {
                purpleTeam = scoreboard.addTeam(PURPLE_TEAM_NAME);
                purpleTeam.setColor(Formatting.LIGHT_PURPLE);
                purpleTeam.setDisplayName(Text.literal("Purple Glow Team"));
            }

            Team redTeam = scoreboard.getTeam(RED_TEAM_NAME);
            if (redTeam == null) {
                redTeam = scoreboard.addTeam(RED_TEAM_NAME);
                redTeam.setColor(Formatting.RED);
                redTeam.setDisplayName(Text.literal("Red Glow Team"));
            }

            for (var world : server.getWorlds()) {
                for (ItemEntity itemEntity : world.getEntitiesByType(EntityType.ITEM, itemEntity -> true)) {
                    ItemStack stack = itemEntity.getStack();
                    String scoreHolderName = itemEntity.getNameForScoreboard();

                    if (stack.isIn(PURPLE_GLOW_TAG) || Boolean.TRUE.equals(stack.get(ModComponents.IS_CURSED))) {
                        if (redTeam.getPlayerList().contains(scoreHolderName)) {
                            scoreboard.removeScoreHolderFromTeam(scoreHolderName, redTeam);
                        }
                        if (!purpleTeam.getPlayerList().contains(scoreHolderName)) {
                            scoreboard.addScoreHolderToTeam(scoreHolderName, purpleTeam);
                        }
                        itemEntity.setGlowing(true);

                    } else if (stack.isIn(RED_GLOW_TAG)) {
                        if (purpleTeam.getPlayerList().contains(scoreHolderName)) {
                            scoreboard.removeScoreHolderFromTeam(scoreHolderName, purpleTeam);
                        }
                        if (!redTeam.getPlayerList().contains(scoreHolderName)) {
                            scoreboard.addScoreHolderToTeam(scoreHolderName, redTeam);
                        }
                        itemEntity.setGlowing(true);
                    }
                }
            }
        });
    }
}
