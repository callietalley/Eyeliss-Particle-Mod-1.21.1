package eyeliss.particle.mod.potion;

import eyeliss.particle.mod.EyelisssParticleMod;
import eyeliss.particle.mod.effect.ModEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModPotions {

    public static final RegistryEntry.Reference<Potion> STYX_POISON_POTION = Registry.registerReference(
            Registries.POTION,
            Identifier.of(EyelisssParticleMod.MOD_ID, "styx_poison"),
            new Potion("styx_poison", new StatusEffectInstance(
                    Registries.STATUS_EFFECT.getEntry(ModEffects.STYX_POISON),
                    1200,
                    0
            ))
    );

    public static final RegistryEntry.Reference<Potion> STYX_POISON_IV_POTION = Registry.registerReference(
            Registries.POTION,
            Identifier.of(EyelisssParticleMod.MOD_ID, "styx_poison_iv"),
            new Potion("styx_poison", new StatusEffectInstance(
                    Registries.STATUS_EFFECT.getEntry(ModEffects.STYX_POISON),
                    450,
                    3
            ))
    );

    public static final RegistryEntry.Reference<Potion> SUNDERED_POTION = Registry.registerReference(
            Registries.POTION,
            Identifier.of(EyelisssParticleMod.MOD_ID, "sundered"),
            new Potion("sundered", new StatusEffectInstance(
                    Registries.STATUS_EFFECT.getEntry(ModEffects.SUNDERED),
                    1200,
                    0
            ))
    );

    public static final RegistryEntry.Reference<Potion> SUNDERED_III_POTION = Registry.registerReference(
            Registries.POTION,
            Identifier.of(EyelisssParticleMod.MOD_ID, "sundered_iii"),
            new Potion("sundered", new StatusEffectInstance(
                    Registries.STATUS_EFFECT.getEntry(ModEffects.SUNDERED),
                    240,
                    2
            ))
    );

    public static final RegistryEntry.Reference<Potion> BLEEDING_OUT_POTION = Registry.registerReference(
            Registries.POTION,
            Identifier.of(EyelisssParticleMod.MOD_ID, "bleeding_out"),
            new Potion("bleeding_out", new StatusEffectInstance(
                    Registries.STATUS_EFFECT.getEntry(ModEffects.BLEEDING_OUT),
                    1200,
                    0
            ))
    );

    public static final RegistryEntry.Reference<Potion> BLEEDING_OUT_III_POTION = Registry.registerReference(
            Registries.POTION,
            Identifier.of(EyelisssParticleMod.MOD_ID, "bleeding_out_iii"),
            new Potion("bleeding_out", new StatusEffectInstance(
                    Registries.STATUS_EFFECT.getEntry(ModEffects.BLEEDING_OUT),
                    450,
                    2
            ))
    );

    public static final RegistryEntry.Reference<Potion> OVERHEALTH_POTION = Registry.registerReference(
            Registries.POTION,
            Identifier.of(EyelisssParticleMod.MOD_ID, "overhealth"),
            new Potion("overhealth", new StatusEffectInstance(
                    Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH),
                    1200,
                    0
            ))
    );

    public static void registerPotions() {
        System.out.println("Registering Custom Mod Potions for " + EyelisssParticleMod.MOD_ID);
    }
}
