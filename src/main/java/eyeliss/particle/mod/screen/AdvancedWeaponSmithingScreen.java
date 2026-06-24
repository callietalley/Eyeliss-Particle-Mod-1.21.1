package eyeliss.particle.mod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import eyeliss.particle.mod.network.SelectWeaponC2SPayload;
import eyeliss.particle.mod.recipe.WeaponSmithingRecipe;
import eyeliss.particle.mod.recipe.HardLimitedSmithingRecipe;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.recipe.RecipeEntry;

import java.util.ArrayList;
import java.util.List;

import static eyeliss.particle.mod.EyelisssParticleMod.MOD_ID;

public class AdvancedWeaponSmithingScreen extends HandledScreen<AdvancedWeaponSmithingScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/gui/advanced_weapon_smithing.png");

    public AdvancedWeaponSmithingScreen(AdvancedWeaponSmithingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 240;
        this.backgroundHeight = 245;
        this.playerInventoryTitleY = 151;
        this.playerInventoryTitleX = 45;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.updateDynamicSlotCoordinates();
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        this.renderTopRecipeTooltips(context, mouseX, mouseY);
    }

    private void updateDynamicSlotCoordinates() {
        int activeCount = this.handler.getRequiredIngredientCount();
        int areaWidth = 202;
        int slotSpacing = 18;
        int totalSlotsWidth = activeCount * slotSpacing;
        int startX = 12 + ((areaWidth - totalSlotsWidth) / 2);

        for (int i = 0; i < 7; i++) {
            net.minecraft.screen.slot.Slot inputSlot = this.handler.getSlot(i);
            if (i < activeCount) {
                int finalX = startX + (i * slotSpacing);
                inputSlot.x = finalX;
                inputSlot.y = 135;
            } else {
                inputSlot.x = -2000;
                inputSlot.y = -2000;
            }
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 512);

        if (MinecraftClient.getInstance().world == null) return;
        List<RecipeEntry<?>> recipes = this.handler.getFilteredRecipesList();

        for (int i = 0; i < recipes.size(); i++) {
            int gridSlotX = x + 12 + (i * 22);
            int gridSlotY = y + 18;
            boolean isSelected = (this.handler.getSelectedRecipeIndex() == i);

            var recipe = recipes.get(i).value();
            int colorIndex = (recipe instanceof HardLimitedSmithingRecipe limited) ? limited.color() : 0;

            int clampedColor = Math.clamp(colorIndex, 0, 14);
            int textureU = clampedColor * 18;

            if (isSelected) {
                context.drawTexture(TEXTURE, gridSlotX - 1, gridSlotY - 1, textureU, 318, 18, 18, 256, 512);
            } else {
                context.drawTexture(TEXTURE, gridSlotX - 1, gridSlotY - 1, textureU, 336, 18, 18, 256, 512);
            }

            ItemStack outputStack = (recipe instanceof HardLimitedSmithingRecipe l) ? l.output() : ((WeaponSmithingRecipe)recipe).output();
            context.drawItem(outputStack, gridSlotX, gridSlotY);
        }

        int activeCount = this.handler.getRequiredIngredientCount();
        int selection = this.handler.getSelectedRecipeIndex();
        var activeEntry = (selection >= 0 && selection < recipes.size()) ? recipes.get(selection) : null;

        int activeCraftColor = 0;
        if (activeEntry != null && activeEntry.value() instanceof HardLimitedSmithingRecipe limitedCraft) {
            activeCraftColor = Math.clamp(limitedCraft.color(), 0, 14);
        }
        int materialSlotU = activeCraftColor * 18;

        for (int i = 0; i < activeCount && i < 7; i++) {
            net.minecraft.screen.slot.Slot inputSlot = this.handler.getSlot(i);
            int slotRenderX = x + inputSlot.x - 1;
            int slotRenderY = y + inputSlot.y - 1;

            context.drawTexture(TEXTURE, slotRenderX, slotRenderY, materialSlotU, 300, 18, 18, 256, 512);

            if (inputSlot.getStack().isEmpty() && activeEntry != null) {
                var recipe = activeEntry.value();
                List<ItemStack> ingredients = (recipe instanceof HardLimitedSmithingRecipe l) ? l.ingredients() : ((WeaponSmithingRecipe)recipe).ingredients();

                if (i < ingredients.size()) {
                    long systemTime = System.currentTimeMillis();
                    double wave = Math.sin(systemTime * 0.003);
                    float pulseAlpha = 0.2F + (float)(((wave + 1.0) / 2.0) * 0.2F);

                    ItemStack ingredientStack = ingredients.get(i);
                    net.minecraft.client.render.item.ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
                    net.minecraft.client.render.model.BakedModel model = renderer.getModel(ingredientStack, null, null, 0);
                    net.minecraft.client.texture.Sprite sprite = model.getParticleSprite();

                    if (sprite != null) {
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, pulseAlpha);
                        RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);

                        boolean isBlock = net.minecraft.block.Block.getBlockFromItem(ingredientStack.getItem()) != net.minecraft.block.Blocks.AIR;
                        if (isBlock) {
                            context.drawSprite(x + inputSlot.x + 1, y + inputSlot.y + 1, 0, 14, 14, sprite);
                        } else {
                            context.drawSprite(x + inputSlot.x, y + inputSlot.y, 0, 16, 16, sprite);
                        }

                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        RenderSystem.disableBlend();
                    }
                }
            }
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0xFFFFFFFF, false);

        if (MinecraftClient.getInstance().world == null) return;
        List<RecipeEntry<?>> recipes = this.handler.getFilteredRecipesList();

        int selection = this.getSelectedRecipeIndex();
        if (selection >= 0 && selection < recipes.size()) {
            var recipe = recipes.get(selection).value();
            List<ItemStack> ingredients = (recipe instanceof HardLimitedSmithingRecipe l) ? l.ingredients() : ((WeaponSmithingRecipe)recipe).ingredients();
            int activeCount = this.handler.getRequiredIngredientCount();

            for (int i = 0; i < ingredients.size() && i < activeCount && i < 7; i++) {
                ItemStack requiredIngredient = ingredients.get(i);
                String countText = String.valueOf(requiredIngredient.getCount());
                net.minecraft.screen.slot.Slot inputSlot = this.handler.getSlot(i);

                ItemStack currentPlacedStack = inputSlot.getStack();

                boolean matchAndSufficient = !currentPlacedStack.isEmpty()
                        && ItemStack.areItemsEqual(currentPlacedStack, requiredIngredient)
                        && currentPlacedStack.getCount() >= requiredIngredient.getCount();

                int textRGBColor = matchAndSufficient ? 0xFF55FF55 : 0xFFFFFF55;

                int textX = inputSlot.x + (8 - this.textRenderer.getWidth(countText) / 2);

                context.drawText(this.textRenderer, countText, textX, 123, textRGBColor, false);
            }
        }
    }

    private void renderTopRecipeTooltips(DrawContext context, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        if (MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null) return;
        List<RecipeEntry<?>> recipes = this.handler.getFilteredRecipesList();
        net.minecraft.entity.player.PlayerInventory playerInv = MinecraftClient.getInstance().player.getInventory();

        for (int i = 0; i < recipes.size(); i++) {
            int gridSlotX = x + 12 + (i * 22);
            int gridSlotY = y + 18;

            if (mouseX >= gridSlotX && mouseX <= gridSlotX + 16 && mouseY >= gridSlotY && mouseY <= gridSlotY + 16) {
                var recipe = recipes.get(i).value();
                ItemStack outputStack = (recipe instanceof HardLimitedSmithingRecipe l) ? l.output() : ((WeaponSmithingRecipe)recipe).output();
                List<ItemStack> ingredients = (recipe instanceof HardLimitedSmithingRecipe l) ? l.ingredients() : ((WeaponSmithingRecipe)recipe).ingredients();

                List<Text> tooltipLines = new ArrayList<>();
                tooltipLines.add(outputStack.getName());
                tooltipLines.add(Text.literal("§7Requires:"));

                for (ItemStack ingredient : ingredients) {
                    int totalPlayerHas = 0;

                    for (int slot = 0; slot < playerInv.size(); slot++) {
                        ItemStack currentStack = playerInv.getStack(slot);
                        if (!currentStack.isEmpty() && ItemStack.areItemsEqual(currentStack, ingredient)) {
                            totalPlayerHas += currentStack.getCount();
                        }
                    }

                    for (int slot = 0; slot < 7; slot++) {
                        net.minecraft.screen.slot.Slot stationSlot = this.handler.getSlot(slot);
                        if (stationSlot != null && stationSlot.hasStack()) {
                            ItemStack currentStack = stationSlot.getStack();
                            if (ItemStack.areItemsEqual(currentStack, ingredient)) {
                                totalPlayerHas += currentStack.getCount();
                            }
                        }
                    }

                    boolean hasEnough = totalPlayerHas >= ingredient.getCount();
                    String countColor = hasEnough ? "§a" : "§c";

                    tooltipLines.add(Text.literal("§8• " + countColor + ingredient.getCount() + "x §f" + ingredient.getName().getString()));
                }

                context.drawTooltip(this.textRenderer, tooltipLines, mouseX, mouseY);
                break;
            }
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        if (MinecraftClient.getInstance().world != null) {
            List<RecipeEntry<?>> recipes = this.handler.getFilteredRecipesList();

            for (int i = 0; i < recipes.size(); i++) {
                int gridSlotX = x + 12 + (i * 22);
                int gridSlotY = y + 18;

                if (mouseX >= gridSlotX && mouseX <= gridSlotX + 16 && mouseY >= gridSlotY && mouseY <= gridSlotY + 16) {
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

                    String exactRecipeId = recipes.get(i).id().toString();
                    ClientPlayNetworking.send(new SelectWeaponC2SPayload(exactRecipeId));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int getSelectedRecipeIndex() {
        return this.handler.getSelectedRecipeIndex();
    }
}
