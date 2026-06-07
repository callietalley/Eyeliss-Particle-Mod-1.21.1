package eyeliss.particle.mod.item;

import eyeliss.particle.mod.component.ModComponents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ShadowCurseHelper {

    public static void applyShadowCurse(ItemStack stack) {
        stack.set(ModComponents.IS_CURSED, true);

        Text curseWarning = Text.literal("Cursed")
                .formatted(Formatting.DARK_PURPLE)
                .formatted(Formatting.BOLD)
                .styled(style -> style.withItalic(false));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(curseWarning)));

        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
    }
}