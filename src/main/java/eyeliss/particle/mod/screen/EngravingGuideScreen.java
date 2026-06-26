package eyeliss.particle.mod.screen;

import eyeliss.particle.mod.EyelisssParticleMod;
import eyeliss.particle.mod.component.EngravingContents;
import eyeliss.particle.mod.component.ModComponents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import java.util.List;

public class EngravingGuideScreen extends HandledScreen<EngravingGuideScreenHandler> {

    private static final Identifier CUSTOM_BOOK_TEXTURE = Identifier.of(EyelisssParticleMod.MOD_ID, "textures/gui/engraving_guide.png");

    private double scrollAmount = 0.0;
    private int maxScrollHeight = 0;

    private boolean wasItemPresentLastFrame = false;

    public EngravingGuideScreen(EngravingGuideScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 310;
        this.backgroundHeight = 273;

        // FIX: Moved title text to the right by 10 pixels (13 -> 23)
        this.titleX = 23;
        this.titleY = 12;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.maxScrollHeight > 0) {
            this.scrollAmount = MathHelper.clamp(this.scrollAmount - (verticalAmount * 12.0), 0.0, this.maxScrollHeight);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(CUSTOM_BOOK_TEXTURE, x, y, 0f, 0f, this.backgroundWidth, this.backgroundHeight, 512, 512);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x300060, false);

        ItemStack inspectedStack = this.handler.getInspectedStack();
        boolean isItemPresentThisFrame = !inspectedStack.isEmpty() && inspectedStack.contains(ModComponents.ENGRAVING_CONTENTS);

        if (isItemPresentThisFrame != this.wasItemPresentLastFrame) {
            this.scrollAmount = 0.0;
            this.maxScrollHeight = 0;
            this.wasItemPresentLastFrame = isItemPresentThisFrame;
        }

        // FIX: Moved the entire right page workspace to the right by 10 pixels (159 -> 169)
        int rightPageX = 169;
        int rightPageMaxTextWidth = 117;
        int minVisibleY = 18;
        int maxVisibleY = 162;
        int visibleHeight = maxVisibleY - minVisibleY;

        if (!isItemPresentThisFrame) {
            // FIX: Repositioned the slot hint text down by 4 and right by 7 pixels to match the slot adjustments (75 -> 81, 104 -> 108)
            int slotMidpointX = 81;
            String slotHint = Text.translatable("gui.eyelisspartmod.insert_slot_hint").getString();
            int slotHintWidth = this.textRenderer.getWidth(slotHint);
            context.drawText(this.textRenderer, slotHint, slotMidpointX - (slotHintWidth / 2), 108, 0x999999, false);

            String rawGuide = Text.translatable("gui.eyelisspartmod.survival_guide_content").getString();
            String[] guideParagraphs = rawGuide.split("\\{r\\}");

            int totalGuideHeight = 0;
            for (String paragraph : guideParagraphs) {
                List<OrderedText> lines = this.textRenderer.wrapLines(Text.literal(paragraph), rightPageMaxTextWidth);
                totalGuideHeight += (lines.size() * 9);
                totalGuideHeight += 5;
            }
            if (totalGuideHeight > 0) totalGuideHeight -= 5;

            this.maxScrollHeight = Math.max(0, totalGuideHeight - visibleHeight);

            int absoluteLeft = this.x + rightPageX;
            int absoluteTop = this.y + minVisibleY;
            int absoluteWidth = rightPageMaxTextWidth + 12;
            context.enableScissor(absoluteLeft, absoluteTop, absoluteLeft + absoluteWidth, absoluteTop + visibleHeight);

            int currentGuideY = minVisibleY - (int) this.scrollAmount;

            for (String paragraph : guideParagraphs) {
                List<OrderedText> wrappedLines = this.textRenderer.wrapLines(Text.literal(paragraph), rightPageMaxTextWidth);
                for (OrderedText line : wrappedLines) {
                    context.drawText(this.textRenderer, line, rightPageX, currentGuideY, 0x444444, false);
                    currentGuideY += 9;
                }
                currentGuideY += 5;
            }

            context.disableScissor();

            if (this.maxScrollHeight > 0) {
                int scrollbarX = rightPageX + rightPageMaxTextWidth + 4;
                double scrollPercentage = this.scrollAmount / (double) this.maxScrollHeight;
                int barHeight = 20;
                int barY = minVisibleY + (int) ((visibleHeight - barHeight) * scrollPercentage);

                context.fill(scrollbarX, minVisibleY, scrollbarX + 2, maxVisibleY, 0x22000000);
                context.fill(scrollbarX, barY, scrollbarX + 2, barY + barHeight, 0xFF5000A0);
            }
            return;
        }

        List<EngravingContents> engravings = inspectedStack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());

        int totalContentHeight = 0;
        for (EngravingContents e : engravings) {
            totalContentHeight += 11;
            String rawDescription = Text.translatable("engraving.description.eyelisspartmod." + e.engravingId()).getString();
            String[] manualParagraphs = rawDescription.split("\\{r\\}");

            for (String paragraph : manualParagraphs) {
                List<OrderedText> lines = this.textRenderer.wrapLines(Text.literal(paragraph), rightPageMaxTextWidth);
                totalContentHeight += (lines.size() * 9);
            }
            totalContentHeight += 6;
        }

        this.maxScrollHeight = Math.max(0, totalContentHeight - visibleHeight);

        int absoluteLeft = this.x + rightPageX;
        int absoluteTop = this.y + minVisibleY;
        int absoluteWidth = rightPageMaxTextWidth + 12;
        context.enableScissor(absoluteLeft, absoluteTop, absoluteLeft + absoluteWidth, absoluteTop + visibleHeight);

        int currentDrawY = minVisibleY - (int) this.scrollAmount;

        for (EngravingContents e : engravings) {
            String id = e.engravingId();

            Text nameText = Text.translatable("engraving.eyelisspartmod." + id).formatted(net.minecraft.util.Formatting.BOLD);
            context.drawText(this.textRenderer, nameText, rightPageX, currentDrawY, 0x300060, false);

            currentDrawY += 11;

            String rawDescription = Text.translatable("engraving.description.eyelisspartmod." + id).getString();
            String[] manualParagraphs = rawDescription.split("\\{r\\}");

            for (String paragraph : manualParagraphs) {
                List<OrderedText> wrappedLines = this.textRenderer.wrapLines(Text.literal(paragraph), rightPageMaxTextWidth);
                for (OrderedText line : wrappedLines) {
                    context.drawText(this.textRenderer, line, rightPageX, currentDrawY, 0x444444, false);
                    currentDrawY += 9;
                }
            }
            currentDrawY += 6;
        }

        context.disableScissor();

        if (this.maxScrollHeight > 0) {
            int scrollbarX = rightPageX + rightPageMaxTextWidth + 4;
            double scrollPercentage = this.scrollAmount / (double) this.maxScrollHeight;
            int barHeight = 20;
            int barY = minVisibleY + (int) ((visibleHeight - barHeight) * scrollPercentage);

            context.fill(scrollbarX, minVisibleY, scrollbarX + 2, maxVisibleY, 0x22000000);
            context.fill(scrollbarX, barY, scrollbarX + 2, barY + barHeight, 0xFF5000A0);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
