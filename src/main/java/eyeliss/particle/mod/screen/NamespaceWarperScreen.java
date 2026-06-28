package eyeliss.particle.mod.screen;

import eyeliss.particle.mod.network.NamespaceWarperPayloads;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class NamespaceWarperScreen extends HandledScreen<NamespaceWarperScreenHandler> {
    private TextFieldWidget nameField;
    private IdentityTextedButton toggleNameplateButton;
    private SmallIdentityButton resetButton;
    private SmallIdentityButton applyButton;

    private boolean hideNameplateState = false;

    private static final Identifier MENU_BG = Identifier.of("eyelisspartmod", "textures/gui/warper_background.png");
    private static final Identifier BOX_TEXTURE = Identifier.of("eyelisspartmod", "textures/gui/name_textbox.png");
    private static final Identifier BTN_VISIBLE_TEX = Identifier.of("eyelisspartmod", "textures/gui/buttons/name_btn.png");
    private static final Identifier BTN_HIDDEN_TEX = Identifier.of("eyelisspartmod", "textures/gui/buttons/name_hidden_btn.png");
    private static final Identifier SMALL_BTN_TEX = Identifier.of("eyelisspartmod", "textures/gui/buttons/small_name_btn.png");

    public NamespaceWarperScreen(NamespaceWarperScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 140;
        this.hideNameplateState = handler.getSavedHideNameplate();
    }

    @Override
    protected void init() {
        super.init();

        this.nameField = new TextFieldWidget(this.textRenderer, this.x + 21, this.y + 36, 136, 20, Text.literal("New Identity..."));
        this.nameField.setMaxLength(22);
        this.nameField.setDrawsBackground(false);
        this.nameField.setText(this.handler.getSavedCustomName());

        this.addSelectableChild(this.nameField);
        this.nameField.setFocused(true);
        this.setFocused(this.nameField);

        Text initialBtnText = Text.literal(this.hideNameplateState ? "Hidden" : "Visible");

        this.toggleNameplateButton = new IdentityTextedButton(
                this.x + 28, this.y + 70, 120, 20,
                initialBtnText,
                this.hideNameplateState,
                (btn) -> {
                    this.hideNameplateState = !this.hideNameplateState;
                    btn.setMessage(Text.literal(this.hideNameplateState ? "Hidden" : "Visible"));
                    ((IdentityTextedButton) btn).setAltTextureState(this.hideNameplateState);
                }
        );
        this.addDrawableChild(this.toggleNameplateButton);

        this.resetButton = new SmallIdentityButton(
                this.x + 28, this.y + 100, 56, 20,
                Text.literal("Reset"),
                0,
                (btn) -> {
                    this.nameField.setText("");
                    this.hideNameplateState = false;
                    this.toggleNameplateButton.setMessage(Text.literal("Visible"));
                    this.toggleNameplateButton.setAltTextureState(false);
                    ClientPlayNetworking.send(new NamespaceWarperPayloads.ApplyIdentityPayload("", false));
                    this.close();
                }
        );
        this.addDrawableChild(this.resetButton);

        this.applyButton = new SmallIdentityButton(
                this.x + 92, this.y + 100, 56, 20,
                Text.literal("Apply"),
                20,
                (btn) -> {
                    String finalName = this.nameField.getText();
                    ClientPlayNetworking.send(new NamespaceWarperPayloads.ApplyIdentityPayload(finalName, this.hideNameplateState));
                    this.close();
                }
        );
        this.addDrawableChild(this.applyButton);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.nameField.charTyped(chr, modifiers)) return true;
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.close();
            return true;
        }

        if (this.nameField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        if (net.minecraft.client.MinecraftClient.getInstance().options.inventoryKey.matchesKey(keyCode, scanCode)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(MENU_BG, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);
        context.drawTexture(BOX_TEXTURE, this.x + 13, this.y + 26, 0, 0, 151, 26, 151, 26);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        String titleText = "Identity Profile Editor";
        int textWidth = this.textRenderer.getWidth(titleText);
        int centeredTitleX = (this.backgroundWidth - textWidth) / 2;
        context.drawText(this.textRenderer, Text.literal(titleText), centeredTitleX, 12, 0xFF9A5CC6, true);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.nameField.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Environment(EnvType.CLIENT)
    private static class IdentityTextedButton extends ButtonWidget {
        private boolean useAltTextureFile = false;

        public IdentityTextedButton(int x, int y, int width, int height, Text message, boolean initialAltState, PressAction onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.useAltTextureFile = initialAltState;
        }

        public void setAltTextureState(boolean state) {
            this.useAltTextureFile = state;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int vOffset = this.isHovered() ? this.height : 0;
            Identifier activeTexture = this.useAltTextureFile ? BTN_HIDDEN_TEX : BTN_VISIBLE_TEX;

            context.drawTexture(activeTexture, this.getX(), this.getY(), 0, (float) vOffset, this.width, this.height, 120, 40);

            int textX = this.getX() + (this.width - net.minecraft.client.MinecraftClient.getInstance().textRenderer.getWidth(this.getMessage())) / 2;
            int textY = this.getY() + ((this.height - 8) / 2) + 1;
            context.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer, this.getMessage(), textX, textY, 0xFFFFFF, true);
        }
    }

    @Environment(EnvType.CLIENT)
    private static class SmallIdentityButton extends ButtonWidget {
        private final int textureVBase;

        public SmallIdentityButton(int x, int y, int width, int height, Text message, int textureVBase, PressAction onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.textureVBase = textureVBase;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            context.drawTexture(SMALL_BTN_TEX, this.getX(), this.getY(), 0, (float) this.textureVBase, this.width, this.height, 56, 40);

            int textColor = this.isHovered() ? 0xDFDFDF : 0xFFFFFF;
            int textX = this.getX() + (this.width - net.minecraft.client.MinecraftClient.getInstance().textRenderer.getWidth(this.getMessage())) / 2;
            int textY = this.getY() + ((this.height - 8) / 2) + 1;
            context.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer, this.getMessage(), textX, textY, textColor, true);
        }
    }
}
