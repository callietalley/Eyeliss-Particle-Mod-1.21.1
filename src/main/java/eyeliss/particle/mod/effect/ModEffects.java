package eyeliss.particle.mod.effect;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEffects {
    public static final StatusEffect STYX_POISON = Registry.register(
            Registries.STATUS_EFFECT,
            net.minecraft.util.Identifier.of(EyelisssParticleMod.MOD_ID, "styx_poison"),
            new StyxPoisonEffect()
    );

    public static final StatusEffect BLEEDING_OUT = Registry.register(
            Registries.STATUS_EFFECT,
            net.minecraft.util.Identifier.of(EyelisssParticleMod.MOD_ID, "bleeding_out"),
            new BleedingOutEffect()
    );

    public static void register() {
        EyelisssParticleMod.LOGGER.info("Registering Status Effects for " + EyelisssParticleMod.MOD_ID);
    }
}