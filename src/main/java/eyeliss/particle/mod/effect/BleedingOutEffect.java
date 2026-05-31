package eyeliss.particle.mod.effect;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

public class BleedingOutEffect extends StatusEffect {

    public static final RegistryKey<DamageType> BYPASS_DAMAGE_KEY =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, net.minecraft.util.Identifier.of(EyelisssParticleMod.MOD_ID, "bleeding_out"));

    public BleedingOutEffect() {
        super(StatusEffectCategory.HARMFUL, 0x8A0303); // Deep Red Color
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        RegistryEntry<StatusEffect> effectEntry = Registries.STATUS_EFFECT.getEntry(this);
        StatusEffectInstance instance = entity.getStatusEffect(effectEntry);

        if (instance == null) {
            return false;
        }

        int duration = instance.getDuration();

        int realDamageCooldown = 15 - (Math.min(amplifier, 4) * 3);

        RegistryEntry<DamageType> damageTypeEntry = entity.getWorld().getRegistryManager()
                .get(RegistryKeys.DAMAGE_TYPE)
                .entryOf(BYPASS_DAMAGE_KEY);

        DamageSource source = new DamageSource(damageTypeEntry);

        if (duration % realDamageCooldown == 0) {
            entity.damage(source, 1.0f);
        } else if (amplifier < 4) {
            // FALSE DAMAGE TICK SCALING
            // Amp 0, 1, 2 -> every 3 ticks | Amp 3 -> every 2 ticks | Amp 4+ -> never procs
            int falseDamageCooldown = (amplifier == 3) ? 2 : 3;

            if (duration % falseDamageCooldown == 0) {
                entity.damage(source, 0.00001f);
            }
        }

        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}