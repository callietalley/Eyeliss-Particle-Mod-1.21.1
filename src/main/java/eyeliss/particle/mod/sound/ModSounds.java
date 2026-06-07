package eyeliss.particle.mod.sound;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final Identifier FLOCK_ID = Identifier.of(EyelisssParticleMod.MOD_ID, "flock");
    public static final SoundEvent FLOCK_EVENT = SoundEvent.of(FLOCK_ID);

    public static final Identifier SWARM_ID = Identifier.of(EyelisssParticleMod.MOD_ID, "weapon_dagger_swarm");
    public static final SoundEvent SWARM_EVENT = SoundEvent.of(SWARM_ID);

    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, FLOCK_ID, FLOCK_EVENT);
        Registry.register(Registries.SOUND_EVENT, SWARM_ID, SWARM_EVENT);
        EyelisssParticleMod.LOGGER.info("Registering Sounds for " + EyelisssParticleMod.MOD_ID);
    }
}