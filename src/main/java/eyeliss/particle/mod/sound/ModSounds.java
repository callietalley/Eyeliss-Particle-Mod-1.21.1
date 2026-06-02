package eyeliss.particle.mod.sound;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    // 1. Updated for the flock sound identifier
    public static final Identifier FLOCK_ID = Identifier.of(EyelisssParticleMod.MOD_ID, "flock");
    public static final SoundEvent FLOCK_EVENT = SoundEvent.of(FLOCK_ID);

    // 2. Register it into the game
    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, FLOCK_ID, FLOCK_EVENT);
        EyelisssParticleMod.LOGGER.info("Registering Sounds for " + EyelisssParticleMod.MOD_ID);
    }
}