package eyeliss.particle.mod.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;

public class TooltipHelper {

    @Environment(EnvType.CLIENT)
    public static boolean isControlPressed() {
        long windowHandle = net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle();
        return InputUtil.isKeyPressed(windowHandle, 341) || InputUtil.isKeyPressed(windowHandle, 345);
    }

    public static void addControlPrompt(List<Text> tooltip) {
        Text prompt = Text.literal("[CTRL]").formatted(Formatting.BOLD, Formatting.WHITE)
                .append(Text.literal(" for detailed information").setStyle(net.minecraft.text.Style.EMPTY.withBold(false).withColor(Formatting.GRAY)));
        tooltip.add(prompt);
    }
}
