package eyeliss.particle.mod.screen;

import eyeliss.particle.mod.network.RiftGemPayloads;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Set;

@Environment(EnvType.CLIENT)
public class RiftGemScreen extends HandledScreen<RiftGemScreenHandler> {
    private TextFieldWidget typeField;

    private static final Set<String> VALID_NAMES = Set.of(
            "forest", "Forest",
            "desert", "Desert",
            "tundra", "Tundra",
            "deepforest", "Deepforest",
            "shore", "Shore",
            "origin", "Origin",
            "hell", "Hell"
    );
    private static final Identifier TEXTBOX_TEXTURE = Identifier.of("eyelisspartmod", "textures/gui/rift_textbox.png");

    public RiftGemScreen(RiftGemScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 85;
    }

    @Override
    protected void init() {
        super.init();
        this.typeField = new TextFieldWidget(this.textRenderer, this.x + 20, this.y + 45, 136, 20, Text.literal("Type Destination..."));
        this.typeField.setMaxLength(16);
        this.typeField.setDrawsBackground(false);

        this.addSelectableChild(this.typeField);
        this.typeField.setFocused(true);
        this.setFocused(this.typeField);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.typeField.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.close();
            return true;
        }

        if (keyCode == 257) {
            String text = this.typeField.getText().trim();
            if (VALID_NAMES.contains(text)) {
                ClientPlayNetworking.send(new RiftGemPayloads.TypeWarpPayload(text));
            } else {
                ClientPlayNetworking.send(new RiftGemPayloads.TypeWarpPayload("INVALID_RIFT_FREQUENCY_PENALTY"));
            }
            this.close();
            return true;
        }

        if (this.typeField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return true;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        String titleText = "Enter Rift Location:";
        int textWidth = this.textRenderer.getWidth(titleText);
        int centeredX = (this.backgroundWidth - textWidth) / 2;
        context.drawText(this.textRenderer, Text.literal(titleText), centeredX, 22, 0xFF9A5CC6, true);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(TEXTBOX_TEXTURE,
                this.x + 12,
                this.y + 36,
                0, 0,
                151,
                26,
                151,
                26);
        super.render(context, mouseX, mouseY, delta);
        this.typeField.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
