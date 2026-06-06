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
        // 1. Mark our custom component tag as true
        stack.set(ModComponents.IS_CURSED, true);

        // 2. Add Red Lore Warning tooltip info text
        Text curseWarning = Text.literal("Cursed")
                .formatted(Formatting.DARK_PURPLE)
                .formatted(Formatting.BOLD)
                .styled(style -> style.withItalic(false));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(curseWarning)));

        // 3. HARD DISABLE GLINT: This stops vanilla from drawing any purple shine on this item
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
    }
}