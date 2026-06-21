package eyeliss.particle.mod.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionEffects.class)
public class DimensionEffectsMixin {

    @Inject(method = "byDimensionType", at = @At("HEAD"), cancellable = true)
    private static void injectSourceDimensionEffects(DimensionType type, CallbackInfoReturnable<DimensionEffects> cir) {
        Identifier effectsId = Identifier.of("eyelisspartmod", "the_source_effects");

        if (type.effects().equals(effectsId)) {
            cir.setReturnValue(new DimensionEffects(
                    Float.NaN,
                    true,
                    DimensionEffects.SkyType.NORMAL,
                    false,
                    false
            ) {
                @Override
                public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.world != null) {
                        return client.world.getSkyColor(client.gameRenderer.getCamera().getPos(), client.getRenderTickCounter().getTickDelta(true));
                    }
                    return color;
                }

                @Override
                public boolean useThickFog(int camX, int camY) {
                    return false;
                }

                @Override
                public float[] getFogColorOverride(float skyAngle, float tickDelta) {
                    return null;
                }
            });
        }
    }
}
