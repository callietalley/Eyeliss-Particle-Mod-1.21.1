package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.effect.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class OverhealthExclusionMixin {

    @Inject(method = "canHaveStatusEffect", at = @At("HEAD"), cancellable = true)
    private void blockOverhealthOnCreepers(StatusEffectInstance effectInstance, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof CreeperEntity) {
            RegistryEntry<StatusEffect> overhealthEntry = Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH);

            if (effectInstance.getEffectType().equals(overhealthEntry)) {
                cir.setReturnValue(false);
            }
        }
    }
}
