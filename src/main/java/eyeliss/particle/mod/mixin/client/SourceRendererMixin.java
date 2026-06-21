package eyeliss.particle.mod.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class SourceRendererMixin {

    @Unique
    private static final Identifier CUSTOM_SUN = Identifier.of("eyelisspartmod", "textures/environment/source_sun.png");

    @Unique
    private static final Identifier VANILLA_SUN = Identifier.of("minecraft", "textures/environment/sun.png");
    @Unique
    private static final Identifier VANILLA_MOON = Identifier.of("minecraft", "textures/environment/moon_phases.png");
    @Unique
    private static final Identifier VANILLA_STARS = Identifier.of("minecraft", "textures/environment/environment.png");

    @Unique
    private static final Identifier BLANK_TEXTURE = Identifier.of("eyelisspartmod", "textures/environment/blank.png");

    @Unique
    private boolean renderingSourceSun = false;
    @Unique private static final int TOTAL_FRAMES = 21;
    @Unique private static final int TICKS_PER_FRAME = 10;
    @Unique private int vertexIndex = 0;

    @Redirect(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V"),
            remap = true
    )
    private void redirectSkyAndSunTextures(int textureSlot, Identifier originalTexture) {
        MinecraftClient client = MinecraftClient.getInstance();
        renderingSourceSun = false;

        if (client.world != null) {
            String currentDim = client.world.getRegistryKey().getValue().toString();

            if ("eyelisspartmod:the_source".equals(currentDim)) {
                if (originalTexture.equals(VANILLA_SUN)) {
                    RenderSystem.setShaderTexture(textureSlot, CUSTOM_SUN);
                    renderingSourceSun = true;
                    vertexIndex = 0; // Reset counter for the 4 corners of the quad
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                    Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrix();
                    if (modelViewMatrix != null) {
                        modelViewMatrix.rotateZ((float) Math.toRadians(45.0));
                        RenderSystem.applyModelViewMatrix();
                    }
                    return;
                }

                if (originalTexture.equals(VANILLA_MOON) || originalTexture.equals(VANILLA_STARS)) {
                    RenderSystem.setShaderTexture(textureSlot, BLANK_TEXTURE);
                    return;
                }
            }
        }

        RenderSystem.setShaderTexture(textureSlot, originalTexture);
    }

    @Redirect(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V"),
            remap = true
    )
    private void overrideShaderColors(float red, float green, float blue, float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && "eyelisspartmod:the_source".equals(client.world.getRegistryKey().getValue().toString())) {

            if (renderingSourceSun) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                return;
            }

            if (red == green && green == blue && alpha > 0.0F) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                return;
            }
        }
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    @Redirect(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;color(FFFF)Lnet/minecraft/client/render/VertexConsumer;"),
            remap = true
    )
    private VertexConsumer overrideVertexColors(VertexConsumer instance, float red, float green, float blue, float alpha) {
        if (renderingSourceSun) {
            return instance.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        return instance.color(red, green, blue, alpha);
    }

    @Redirect(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;texture(FF)Lnet/minecraft/client/render/VertexConsumer;"),
            remap = true
    )
    private VertexConsumer injectSunTextureAnimation(VertexConsumer instance, float u, float v) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (renderingSourceSun && client.world != null) {
            long time = client.world.getTime();
            int currentFrame = (int) ((time / TICKS_PER_FRAME) % TOTAL_FRAMES);

            float frameHeight = 1.0F / TOTAL_FRAMES;
            float frameVMin = currentFrame * frameHeight;
            float frameVMax = frameVMin + frameHeight;

            int quadVertexIndex = vertexIndex % 4;
            float animatedV = v;

            if (quadVertexIndex == 0 || quadVertexIndex == 1) {
                animatedV = frameVMin;
            } else if (quadVertexIndex == 2 || quadVertexIndex == 3) {
                animatedV = frameVMax;
            }

            vertexIndex = (vertexIndex + 1) % 4;
            return instance.texture(u, animatedV);
        }

        return instance.texture(u, v);
    }
}
