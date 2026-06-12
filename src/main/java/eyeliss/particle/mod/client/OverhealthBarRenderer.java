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

        // Verify player possesses active custom overhealth potion effect
        if (client.player.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH))) {

            // Fetch the exact uncapped percentage float (Current Shield / Max Multiplied Shield Bounds)
            float pct = ClientOverhealthTracker.getPercentage();
            if (pct <= 0.0f) return;

            int screenWidth = drawContext.getScaledWindowWidth();
            int screenHeight = drawContext.getScaledWindowHeight();

            // =========================================================================
            //   ⚡ DYNAMIC DOWNWARD HEART SCALING ENGINE
            // =========================================================================
            int playerHearts = Math.round(client.player.getMaxHealth() / 2.0f);

            // CAPPED ROW MAX: Scales DOWN if under 10 hearts, but never scales UP past 10!
            int activeHeartCount = Math.min(10, playerHearts);

            // FIXED WIDTH MODIFIER: Removed the +1 trailing pixel boundary to make it 1 pixel shorter.
            // Default 10 hearts is now exactly 80 pixels. 9 hearts is exactly 72 pixels.
            int dynamicBarWidth = (activeHeartCount * 8);
            int barHeight = 9;

            // True texture sheet dimensions parameters (keeps source files safe from corruption)
            int textureWidth = 81;
            int textureHeight = 18;

            // Hardcoded screen anchor points aligning perfectly directly on top of red vanilla health rows
            int x = (screenWidth / 2) - 91;
            int y = screenHeight - 39;

            if (client.player.getVehicle() != null) {
                // Readjust offsets if player is riding a horse or pig
                y -= 10;
            }

            RenderSystem.setShaderTexture(0, OVERHEALTH_BAR_TEX);

            // 1. Draw the empty heart background container layer scaled dynamically to active heart width limits
            drawContext.drawTexture(OVERHEALTH_BAR_TEX, x, y, 0, 0, dynamicBarWidth, barHeight, textureWidth, textureHeight);

            // 2. Convert raw percentage into exact pixel steps capped strictly inside our active container limits
            int filledWidth = Math.round(pct * dynamicBarWidth);

            if (filledWidth > 0) {
                // Slices the blue filling from row Y=9 and paints it up to the precise dynamic bar edge bounds
                drawContext.drawTexture(OVERHEALTH_BAR_TEX, x, y, 0, 9, filledWidth, barHeight, textureWidth, textureHeight);
            }

            // Return render pipeline control back to standard vanilla icon textures smoothly
            RenderSystem.setShaderTexture(0, Identifier.ofVanilla("textures/gui/icons.png"));
        }
    }
}
