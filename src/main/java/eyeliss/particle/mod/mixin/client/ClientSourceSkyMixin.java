package eyeliss.particle.mod.mixin.client;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class ClientSourceSkyMixin {

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void injectCustomSourceSkyColors(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        ClientWorld world = (ClientWorld) (Object) this;
        String currentDim = world.getRegistryKey().getValue().toString();

        if ("eyelisspartmod:the_source".equals(currentDim)) {
            float skyAngle = world.getSkyAngle(tickDelta);
            float dayFactor = net.minecraft.util.math.MathHelper.cos(skyAngle * ((float)Math.PI * 2.0F)) * 2.0F + 0.5F;
            dayFactor = net.minecraft.util.math.MathHelper.clamp(dayFactor, 0.0F, 1.0F);

            double dayR = 3.0 / 255.0;
            double dayG = 0.0;
            double dayB = 12.0 / 255.0;

            double nightR = 3.0 / 255.0;
            double nightG = 0.0;
            double nightB = 12.0 / 255.0;

            double finalR = net.minecraft.util.math.MathHelper.lerp(dayFactor, nightR, dayR);
            double finalG = net.minecraft.util.math.MathHelper.lerp(dayFactor, nightG, dayG);
            double finalB = net.minecraft.util.math.MathHelper.lerp(dayFactor, nightB, dayB);

            cir.setReturnValue(new Vec3d(finalR, finalG, finalB));
        }
    }
}
