package eyeliss.particle.mod.client;

import eyeliss.particle.mod.EyelisssParticleMod;
import eyeliss.particle.mod.effect.ModEffects;
import eyeliss.particle.mod.util.ClientOverhealthTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class OverhealthBarRenderer implements HudRenderCallback {
    private static final Identifier OVERHEALTH_BAR_TEX = Identifier.of(EyelisssParticleMod.MOD_ID, "textures/gui/overhealth_bar.png");

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || client.player.isCreative() || client.player.isSpectator()) return;

        if (client.player.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH))) {

            float pct = ClientOverhealthTracker.getPercentage();
            if (pct <= 0.0f) return;

            int screenWidth = drawContext.getScaledWindowWidth();
            int screenHeight = drawContext.getScaledWindowHeight();

            int playerHearts = Math.round(client.player.getMaxHealth() / 2.0f);

            int activeHeartCount = Math.min(10, playerHearts);

            int dynamicBarWidth = (activeHeartCount * 8);
            int barHeight = 9;

            int textureWidth = 81;
            int textureHeight = 18;

            int x = (screenWidth / 2) - 91;
            int y = screenHeight - 39;

            if (client.player.getVehicle() != null) {
                y -= 10;
            }

            RenderSystem.setShaderTexture(0, OVERHEALTH_BAR_TEX);

            drawContext.drawTexture(OVERHEALTH_BAR_TEX, x, y, 0, 0, dynamicBarWidth, barHeight, textureWidth, textureHeight);

            int filledWidth = Math.round(pct * dynamicBarWidth);

            if (filledWidth > 0) {
                drawContext.drawTexture(OVERHEALTH_BAR_TEX, x, y, 0, 9, filledWidth, barHeight, textureWidth, textureHeight);
            }

            RenderSystem.setShaderTexture(0, Identifier.ofVanilla("textures/gui/icons.png"));
        }
    }
}
