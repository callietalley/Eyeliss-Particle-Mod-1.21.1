package eyeliss.particle.mod.mixin.client;

import eyeliss.particle.mod.fluid.ModFluids;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import net.minecraft.fluid.FluidState;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void applySourceSauceFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        if (camera.getFocusedEntity() != null && camera.getFocusedEntity().getWorld() != null) {
            FluidState fluidState = camera.getFocusedEntity().getWorld().getFluidState(camera.getBlockPos());

            if (fluidState.getFluid() == ModFluids.STILL_SOURCE_SAUCE || fluidState.getFluid() == ModFluids.FLOWING_SOURCE_SAUCE) {

                RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);

                RenderSystem.setShaderFogStart(-2.0F);
                RenderSystem.setShaderFogEnd(3.0F);
                RenderSystem.setShaderFogShape(FogShape.SPHERE);

                ci.cancel();
            }
        }
    }
}
