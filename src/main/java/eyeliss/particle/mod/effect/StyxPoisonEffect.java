package eyeliss.particle.mod.effect;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;

public class StyxPoisonEffect extends StatusEffect {

    public static final RegistryKey<DamageType> BYPASS_DAMAGE_KEY =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, net.minecraft.util.Identifier.of(EyelisssParticleMod.MOD_ID, "styx_poison"));

    public StyxPoisonEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x168F56);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        RegistryEntry<StatusEffect> effectEntry = Registries.STATUS_EFFECT.getEntry(this);
        StatusEffectInstance instance = entity.getStatusEffect(effectEntry);

        if (instance == null) {
            return false;
        }

        int duration = instance.getDuration();
        int hitInterval = (amplifier >= 1) ? 5 : 10;

        if (duration % hitInterval == 0) {

            if (entity instanceof PlayerEntity) {
                // FIXED: Retains 100% of vertical movement by passing entity.getVelocity().y cleanly.
                // This stops the effect from halting jumps or freezing falling momentum.
                entity.setVelocity(new Vec3d(0, entity.getVelocity().y, 0));
                entity.velocityModified = true;
            }

            RegistryEntry<DamageType> damageTypeEntry = entity.getWorld().getRegistryManager()
                    .get(RegistryKeys.DAMAGE_TYPE)
                    .entryOf(BYPASS_DAMAGE_KEY);

            DamageSource source = new DamageSource(damageTypeEntry);
            int damageInterval = hitInterval * 2;

            if (duration % damageInterval == 0) {
                float damageAmount = 1.0f;
                if (amplifier >= 2) {
                    damageAmount += (amplifier - 1);
                }

                entity.damage(source, damageAmount);
            } else {
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