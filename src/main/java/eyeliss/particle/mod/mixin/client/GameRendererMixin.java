package eyeliss.particle.mod.mixin.client;

import eyeliss.particle.mod.fluid.SauceDamageTracker;
import eyeliss.particle.mod.effect.ModEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final MinecraftClient client;

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    void adjustCustomDamageShake(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        ClientPlayerEntity player = this.client.player;
        if (player == null) return;

        boolean isBleeding = player.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(ModEffects.BLEEDING_OUT));
        boolean isPoisoned = player.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(ModEffects.STYX_POISON));

        boolean isSourceSauce = ((SauceDamageTracker) player).eyelisssParticleMod$wasRecentlyDamagedBySauce();

        if (isBleeding || isPoisoned || isSourceSauce) {
            float f = (float) player.hurtTime - tickDelta;
            if (f >= 0.0F) {
                f /= (float) player.maxHurtTime;

                float shakeIntensity = 0.1f;
                float angle = MathHelper.sin(f * f * f * f * (float) Math.PI) * f * 14.0F * shakeIntensity;

                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-angle));
            }

            ci.cancel();
        }
    }
}
