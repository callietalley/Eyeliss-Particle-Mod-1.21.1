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

    public static final Identifier SHADOW_BUNDLE_INSERT_ID = Identifier.of(EyelisssParticleMod.MOD_ID, "item.shadow_bundle.insert");
    public static final SoundEvent SHADOW_BUNDLE_INSERT_EVENT = SoundEvent.of(SHADOW_BUNDLE_INSERT_ID);

    public static final Identifier SHADOW_BUNDLE_INSERT_FAIL_ID = Identifier.of(EyelisssParticleMod.MOD_ID, "item.shadow_bundle.insert_fail");
    public static final SoundEvent SHADOW_BUNDLE_INSERT_FAIL_EVENT = SoundEvent.of(SHADOW_BUNDLE_INSERT_FAIL_ID);

    public static final Identifier SHADOW_BUNDLE_WITHDRAW_ID = Identifier.of(EyelisssParticleMod.MOD_ID, "item.shadow_bundle.withdraw");
    public static final SoundEvent SHADOW_BUNDLE_WITHDRAW_EVENT = SoundEvent.of(SHADOW_BUNDLE_WITHDRAW_ID);

    public static final Identifier HARMONIOUS_ESSENCE_ID = Identifier.of(EyelisssParticleMod.MOD_ID, "item.harmonious_essence.music");
    public static final SoundEvent HARMONIOUS_ESSENCE_EVENT = SoundEvent.of(HARMONIOUS_ESSENCE_ID);

    public static final Identifier BLOOD_STEAL_ID = Identifier.of(EyelisssParticleMod.MOD_ID, "item.bloodstone.steal");
    public static final SoundEvent BLOOD_STEAL_EVENT = SoundEvent.of(BLOOD_STEAL_ID);

    public static final Identifier BLOOD_STONE_ID = Identifier.of(EyelisssParticleMod.MOD_ID, "item.bloodstone.activate");
    public static final SoundEvent BLOOD_STONE_EVENT = SoundEvent.of(BLOOD_STONE_ID);

    // New Source Block Sound Identifiers & Events 🪨
    public static final Identifier SOURCE_BLOCK_BREAK_ID = Identifier.of(EyelisssParticleMod.MOD_ID, "block.source_block.break");
    public static final SoundEvent SOURCE_BLOCK_BREAK_EVENT = SoundEvent.of(SOURCE_BLOCK_BREAK_ID);

    public static final Identifier SOURCE_BLOCK_STEP_ID = Identifier.of(EyelisssParticleMod.MOD_ID, "block.source_block.step");
    public static final SoundEvent SOURCE_BLOCK_STEP_EVENT = SoundEvent.of(SOURCE_BLOCK_STEP_ID);

    public static final Identifier SOURCE_BLOCK_PLACE_ID = Identifier.of(EyelisssParticleMod.MOD_ID, "block.source_block.place");
    public static final SoundEvent SOURCE_BLOCK_PLACE_EVENT = SoundEvent.of(SOURCE_BLOCK_PLACE_ID);

    public static final Identifier SOURCE_BLOCK_HIT_ID = Identifier.of(EyelisssParticleMod.MOD_ID, "block.source_block.hit");
    public static final SoundEvent SOURCE_BLOCK_HIT_EVENT = SoundEvent.of(SOURCE_BLOCK_HIT_ID);

    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, FLOCK_ID, FLOCK_EVENT);
        Registry.register(Registries.SOUND_EVENT, SWARM_ID, SWARM_EVENT);
        Registry.register(Registries.SOUND_EVENT, SHADOW_BUNDLE_WITHDRAW_ID, SHADOW_BUNDLE_WITHDRAW_EVENT);
        Registry.register(Registries.SOUND_EVENT, SHADOW_BUNDLE_INSERT_ID, SHADOW_BUNDLE_INSERT_EVENT);
        Registry.register(Registries.SOUND_EVENT, SHADOW_BUNDLE_INSERT_FAIL_ID, SHADOW_BUNDLE_INSERT_FAIL_EVENT);
        Registry.register(Registries.SOUND_EVENT, BLOOD_STONE_ID, BLOOD_STONE_EVENT);
        Registry.register(Registries.SOUND_EVENT, BLOOD_STEAL_ID, BLOOD_STEAL_EVENT);
        Registry.register(Registries.SOUND_EVENT, HARMONIOUS_ESSENCE_ID, HARMONIOUS_ESSENCE_EVENT); // Added missing registration from your list!

        // Register the new block interactions 🛠️
        Registry.register(Registries.SOUND_EVENT, SOURCE_BLOCK_BREAK_ID, SOURCE_BLOCK_BREAK_EVENT);
        Registry.register(Registries.SOUND_EVENT, SOURCE_BLOCK_STEP_ID, SOURCE_BLOCK_STEP_EVENT);
        Registry.register(Registries.SOUND_EVENT, SOURCE_BLOCK_PLACE_ID, SOURCE_BLOCK_PLACE_EVENT);
        Registry.register(Registries.SOUND_EVENT, SOURCE_BLOCK_HIT_ID, SOURCE_BLOCK_HIT_EVENT);

        EyelisssParticleMod.LOGGER.info("Registering Sounds for " + EyelisssParticleMod.MOD_ID);
    }
}
