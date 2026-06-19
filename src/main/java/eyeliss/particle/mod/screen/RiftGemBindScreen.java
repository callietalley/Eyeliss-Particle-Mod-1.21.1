package eyeliss.particle.mod.screen;

import eyeliss.particle.mod.network.RiftGemPayloads;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RiftGemBindScreen extends HandledScreen<RiftGemBindScreenHandler> {
    private static final String[] ANCHORS = {"Forest", "Desert", "Tundra", "Deepforest", "Shore"};

    // NEW: Path pointer to your unified container frame asset
    private static final Identifier BIND_BACKGROUND = Identifier.of("eyelisspartmod", "textures/gui/bind_background.png");

    public RiftGemBindScreen(RiftGemBindScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 144;
        this.backgroundHeight = 158;
    }

    @Override
    protected void init() {
        super.init();

        for (int i = 0; i < ANCHORS.length; i++) {
            final String anchorName = ANCHORS[i];
            Identifier textureLocation = Identifier.of("eyelisspartmod", "textures/gui/buttons/" + anchorName.toLowerCase() + "_btn.png");

            this.addDrawableChild(new BiomeTexturedButton(
                    this.x + 12, this.y + 30 + (i * 23), 120, 20, // Perfectly padds inside your card
                    Text.literal(anchorName),
                    textureLocation,
                    (button) -> {
                        ClientPlayNetworking.send(new RiftGemPayloads.BindEnvironmentPayload(anchorName));
                        this.close();
                    }
            ));
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        String headerText = "Bind Rift Channel";
        int textWidth = this.textRenderer.getWidth(headerText);
        int centeredX = (this.backgroundWidth - textWidth) / 2;

        context.drawText(this.textRenderer, Text.literal(headerText), centeredX, 14, 0xFFC5A3E6, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(
                BIND_BACKGROUND,
                this.x,
                this.y,
                0, 0,
                144, 158,
                144, 158
        );

        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Environment(EnvType.CLIENT)
    private static class BiomeTexturedButton extends ButtonWidget {
        private final Identifier buttonTexture;

        public BiomeTexturedButton(int x, int y, int width, int height, Text message, Identifier buttonTexture, PressAction onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.buttonTexture = buttonTexture;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int vOffset = this.isSelected() ? this.height : 0;

            context.drawTexture(this.buttonTexture, this.getX(), this.getY(), 0, vOffset, this.width, this.height, this.width, this.height * 2);

            int textX = this.getX() + (this.width - net.minecraft.client.MinecraftClient.getInstance().textRenderer.getWidth(this.getMessage())) / 2;
            int textY = this.getY() + (this.height - 8) / 2;
            context.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer, this.getMessage(), textX, textY, 0xFFFFFF, true);
        }
    }
}
