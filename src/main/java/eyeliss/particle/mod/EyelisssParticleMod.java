package eyeliss.particle.mod;

import eyeliss.particle.mod.block.ModBlocks;
import eyeliss.particle.mod.effect.ModEffects;
import eyeliss.particle.mod.entity.ModEntities;
import eyeliss.particle.mod.item.ModItemGroups;
import eyeliss.particle.mod.item.ModItems;
import eyeliss.particle.mod.particle.ModParticles;
import eyeliss.particle.mod.sound.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EyelisssParticleMod implements ModInitializer {
	public static final String MOD_ID = "eyelisspartmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final String TEAM_NAME = "purple_glow_team";

	private static final TagKey<Item> PURPLE_GLOW_TAG = TagKey.of(
			RegistryKeys.ITEM,
			Identifier.of(MOD_ID, "glows_purple")
	);

	@Override
	public void onInitialize() {
		ModEntities.registerEntities();

		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModParticles.registerParticles();
		ModEffects.register();
		ModSounds.registerSounds();

		ServerTickEvents.START_SERVER_TICK.register(server -> {
			Scoreboard scoreboard = server.getScoreboard();
			Team purpleTeam = scoreboard.getTeam(TEAM_NAME);

			if (purpleTeam == null) {
				purpleTeam = scoreboard.addTeam(TEAM_NAME);
				purpleTeam.setColor(Formatting.LIGHT_PURPLE);
				purpleTeam.setDisplayName(Text.literal("Purple Glow Team"));
			}

			for (var world : server.getWorlds()) {
				for (ItemEntity itemEntity : world.getEntitiesByType(EntityType.ITEM, itemEntity -> true)) {

					if (itemEntity.getStack().isIn(PURPLE_GLOW_TAG)) {
						String scoreHolderName = itemEntity.getNameForScoreboard();

						if (!purpleTeam.getPlayerList().contains(scoreHolderName)) {
							scoreboard.addScoreHolderToTeam(scoreHolderName, purpleTeam);
						}

						itemEntity.setGlowing(true);
					}
				}
			}
		});
	}
}