package eyeliss.particle.mod.mixin.client;

import eyeliss.particle.mod.effect.ModEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

        if (isBleeding || isPoisoned) {
            // FIXED CONDITIONS:
            // If it's a real hit frame AND it is specifically Styx Poison (NOT Bleeding),
            // return early to apply full vanilla screen tilt.
            if (isRealDamageHitFrame(player, isBleeding) && !isBleeding) {
                return;
            }

            // This block now catches:
            // 1. All false hits from Styx Poison
            // 2. All false hits from Bleeding Out
            // 3. All REAL hits from Bleeding Out
            float f = (float) player.hurtTime - tickDelta;
            if (f >= 0.0F) {
                f /= (float) player.maxHurtTime;

                // Scaled to your custom dampening value (0.2f)
                float shakeIntensity = 0.2f;
                float angle = MathHelper.sin(f * f * f * f * (float) Math.PI) * f * 14.0F * shakeIntensity;

                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-angle));
            }

            ci.cancel();
        }
    }

    @Unique
    private boolean isRealDamageHitFrame(ClientPlayerEntity player, boolean isBleeding) {
        StatusEffectInstance activeEffect = player.getStatusEffect(
                Registries.STATUS_EFFECT.getEntry(isBleeding ? ModEffects.BLEEDING_OUT : ModEffects.STYX_POISON)
        );

        if (activeEffect == null) {
            return false;
        }

        int duration = activeEffect.getDuration();
        int amplifier = activeEffect.getAmplifier();

        if (isBleeding) {
            int realDamageCooldown = 15 - (Math.min(amplifier, 4) * 3);
            return (duration % realDamageCooldown == 0);
        } else { // Styx Poison
            int hitInterval = (amplifier >= 1) ? 5 : 10;
            int damageInterval = hitInterval * 2;
            return (duration % damageInterval == 0);
        }
    }
}