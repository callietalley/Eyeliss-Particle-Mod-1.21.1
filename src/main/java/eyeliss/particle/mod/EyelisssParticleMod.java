package eyeliss.particle.mod;

import eyeliss.particle.mod.block.ModBlocks;
import eyeliss.particle.mod.item.ModItemGroups;
import eyeliss.particle.mod.item.ModItems;
import eyeliss.particle.mod.particle.ModParticles;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Text
public class EyelisssParticleMod implements ModInitializer {
	public static final String MOD_ID = "eyelisspartmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItemGroups.registerItemGroups();

		ModItems.registerModItems();
		ModBlocks.registerModBlocks();

		ModParticles.registerParticles();
	}
}