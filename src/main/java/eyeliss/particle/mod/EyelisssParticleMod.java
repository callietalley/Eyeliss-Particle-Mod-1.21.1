package eyeliss.particle.mod;

import eyeliss.particle.mod.block.ModBlocks;
import eyeliss.particle.mod.effect.ModEffects;
import eyeliss.particle.mod.entity.ModEntities; // Make sure this is imported!
import eyeliss.particle.mod.item.ModItemGroups;
import eyeliss.particle.mod.item.ModItems;
import eyeliss.particle.mod.particle.ModParticles;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EyelisssParticleMod implements ModInitializer {
	public static final String MOD_ID = "eyelisspartmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// 1. MUST register entities and attributes first to prevent NullPointerException crashes
		ModEntities.registerEntities();

		// 2. Load the rest of your mod files
		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModParticles.registerParticles();
		ModEffects.register();
	}
}