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
        // FIXED: Checks the world fluid state right at the camera's precise vector location coordinates
        if (camera.getFocusedEntity() != null && camera.getFocusedEntity().getWorld() != null) {
            FluidState fluidState = camera.getFocusedEntity().getWorld().getFluidState(camera.getBlockPos());

            // Match against your custom Source Sauce fluid variants
            if (fluidState.getFluid() == ModFluids.STILL_SOURCE_SAUCE || fluidState.getFluid() == ModFluids.FLOWING_SOURCE_SAUCE) {

                // 1. Force the rendering system color matrix to pure white [R: 1, G: 1, B: 1, Alpha: 1]
                RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);

                // 2. Setup the thick fog thresholds (Lower values = more blinding)
                RenderSystem.setShaderFogStart(-2.0F); // Begins slightly behind the lens viewport
                RenderSystem.setShaderFogEnd(3.0F);    // Restricts absolute visibility to 3 blocks maximum!
                RenderSystem.setShaderFogShape(FogShape.SPHERE);

                // Cancel out remaining vanilla rendering logic to secure the white overlay
                ci.cancel();
            }
        }
    }
}
