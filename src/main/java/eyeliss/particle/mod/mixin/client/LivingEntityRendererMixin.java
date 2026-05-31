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
        // Safe check to ensure the entity object exists
        if (entity == null) return;

        // Check if the entity is currently afflicted with Styx Poison
        boolean isPoisoned = entity.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(ModEffects.STYX_POISON));

        if (isPoisoned) {
            // Check if they are simultaneously suffering from Bleeding Out
            boolean isBleeding = entity.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(ModEffects.BLEEDING_OUT));

            // If they are poisoned and NOT bleeding, completely strip the red damage overlay tint
            if (!isBleeding) {
                // OverlayTexture.getV(false) tells the engine that hurtTime is effectively 0 for the render layer
                int cleanOverlay = OverlayTexture.packUv(OverlayTexture.getU(whiteOverlayProgress), OverlayTexture.getV(false));
                cir.setReturnValue(cleanOverlay);
            }
        }
    }
}