package eyeliss.particle.mod.client;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.MathHelper;
import java.util.List;

public class EngravingColorAnimator {

    private static final int BASE_MAGIC_PURPLE = 0x6C00D8;
    private static final int NORMAL_FADE = 0x4B55D8;
    private static final int CURSE_FADE = 0xA00045;
    private static final int BLESSING_FADE = 0xF9B700;

    private static final List<String> CURSES = List.of("stagnation", "ruin");
    private static final List<String> BLESSINGS = List.of("restoration", "transcendence", "wisdom", "baptism", "ethereal", "dwarven", "shattering", "blessed", "hasted");

    public static Style getAnimatedMagicStyle(String engravingId) {
        return getAnimatedMagicStyle(engravingId, ItemStack.EMPTY);
    }

    public static Style getAnimatedMagicStyle(String engravingId, ItemStack stack) {
        long time = System.currentTimeMillis();
        float phase = (float) ((time % 2000) / 2000.0 * Math.PI * 2.0);
        float weight = (MathHelper.sin(phase) + 1.0f) / 2.0f;

        int targetColorHex = NORMAL_FADE;
        if (CURSES.contains(engravingId)) {
            targetColorHex = CURSE_FADE;
        } else if (BLESSINGS.contains(engravingId)) {
            targetColorHex = BLESSING_FADE;
        }

        int rA = (BASE_MAGIC_PURPLE >> 16) & 0xFF;
        int gA = (BASE_MAGIC_PURPLE >> 8) & 0xFF;
        int bA = BASE_MAGIC_PURPLE & 0xFF;

        int rB = (targetColorHex >> 16) & 0xFF;
        int gB = (targetColorHex >> 8) & 0xFF;
        int bB = targetColorHex & 0xFF;

        int r = (int) (rA + (rB - rA) * weight);
        int g = (int) (gA + (gB - gA) * weight);
        int b = (int) (bA + (bB - bA) * weight);

        return Style.EMPTY.withColor(TextColor.fromRgb((r << 16) | (g << 8) | b));
    }
}