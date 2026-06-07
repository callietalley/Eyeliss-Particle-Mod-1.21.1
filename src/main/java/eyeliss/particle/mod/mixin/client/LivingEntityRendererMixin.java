package eyeliss.particle.mod.mixin.client;

import eyeliss.particle.mod.effect.ModEffects;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "getOverlay", at = @At("HEAD"), cancellable = true)
    private static void hideStyxPoisonRedFlashForAll(LivingEntity entity, float whiteOverlayProgress, CallbackInfoReturnable<Integer> cir) {
        if (entity == null) return;

        boolean isPoisoned = entity.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(ModEffects.STYX_POISON));

        if (isPoisoned) {
            boolean isBleeding = entity.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(ModEffects.BLEEDING_OUT));

            if (!isBleeding) {
                int cleanOverlay = OverlayTexture.packUv(OverlayTexture.getU(whiteOverlayProgress), OverlayTexture.getV(false));
                cir.setReturnValue(cleanOverlay);
            }
        }
    }
}