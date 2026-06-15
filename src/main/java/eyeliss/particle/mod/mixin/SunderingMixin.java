package eyeliss.particle.mod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance; // Added for safety
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Static import from your effect registration class
import static eyeliss.particle.mod.effect.ModEffects.SUNDERED;

@Mixin(LivingEntity.class)
public class SunderingMixin {

    @Inject(method = "modifyAppliedDamage", at = @At("RETURN"), cancellable = true)
    private void applySunderedModifier(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        float currentDamage = cir.getReturnValue();

        // 1. Fetch the RegistryEntry wrapper
        RegistryEntry<StatusEffect> sunderedEntry = Registries.STATUS_EFFECT.getEntry(SUNDERED);

        // 2. Fetch the effect instance safely instead of checking twice
        StatusEffectInstance sunderedInstance = entity.getStatusEffect(sunderedEntry);

        // 3. Null-check the instance directly to satisfy the IDE and prevent crashes
        if (sunderedInstance != null) {
            int sunderedAmplifier = sunderedInstance.getAmplifier() + 1;

            // Calculate base damage before standard Resistance touched it
            float originalDamage = currentDamage;
            if (entity.hasStatusEffect(StatusEffects.RESISTANCE)) {
                StatusEffectInstance resistanceInstance = entity.getStatusEffect(StatusEffects.RESISTANCE);
                if (resistanceInstance != null) {
                    int resAmplifier = resistanceInstance.getAmplifier() + 1;
                    float resistanceModifier = 1.0F - (resAmplifier * 0.2F);
                    if (resistanceModifier > 0.0F) {
                        originalDamage = currentDamage / resistanceModifier;
                    } else {
                        originalDamage = amount; // Fallback if resistance was 100%+
                    }
                }
            }

            // 4. Apply the 10% damage increase per level based on the base damage
            float damageIncrease = originalDamage * (sunderedAmplifier * 0.10F);
            float finalDamage = currentDamage + damageIncrease;

            if (finalDamage < 0.0F) {
                finalDamage = 0.0F;
            }

            cir.setReturnValue(finalDamage);
        }
    }
}
