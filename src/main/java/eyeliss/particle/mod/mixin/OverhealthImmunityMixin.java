package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.effect.ModEffects;
import net.minecraft.entity.Entity; // Imported for the multi-parameter signature lookup
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class OverhealthImmunityMixin {

    @Inject(
            method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void blockNegativeEffectsDuringOverhealth(StatusEffectInstance effectInstance, @Nullable Entity source, CallbackInfoReturnable<Boolean> cir) {
        if (effectInstance == null) return;

        LivingEntity entity = (LivingEntity) (Object) this;
        var overhealthEntry = Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH);

        if (entity.hasStatusEffect(overhealthEntry)) {

            StatusEffectCategory incomingCategory = effectInstance.getEffectType().value().getCategory();

            if (incomingCategory == StatusEffectCategory.HARMFUL) {
                cir.setReturnValue(false);
            }
        }
    }
}
