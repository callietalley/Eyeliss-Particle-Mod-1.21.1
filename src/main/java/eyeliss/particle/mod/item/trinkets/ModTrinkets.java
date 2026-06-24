package eyeliss.particle.mod.item.trinkets;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.List;

public class ModTrinkets {
    public static final Item MIDAS_GOLD = Registry.register(
            Registries.ITEM,
            Identifier.of("eyelisspartmod", "midas_gold"),
            new MidasGoldItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE))
    );

    public static final Item SADIMS_IRON = Registry.register(
            Registries.ITEM,
            Identifier.of("eyelisspartmod", "sadims_iron"),
            new SadimsIronItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE))
    );

    public static final BloodStoneItem BLOOD_STONE_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of("eyelisspartmod", "bloodstone"),
            new BloodStoneItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE))
    );

    public static final RiftGemItem RIFT_GEM = Registry.register(
            Registries.ITEM,
            Identifier.of(EyelisssParticleMod.MOD_ID, "rift_gem"),
            new RiftGemItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC)) {
                @Override
                public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
                    tooltip.add(Text.literal("Allows the user to teleport between 5 custom bound locations").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("Open teleport menu with the Trinket Keybind").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("Open the binding menu with Shift + Trinket Keybind").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("Has 4 static locations:").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
                    tooltip.add(Text.literal("Hell - Takes you to the safest position").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("nearest to your position in the nether").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("Origin - Highest block at 0, 0 in the overworld").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("Return - Returns you to a safe place near your overworld position").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("Source - A pitch white dimension, with a 1 to 15 block ratio").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("compared to the overworld").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("End - Takes you to the entrance of The End dimension").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("if you have killed the Enderdragon before").formatted(Formatting.GRAY));

                    super.appendTooltip(stack, context, tooltip, type);
                }
            }
    );

    public static void registerModTrinkets() {
        EyelisssParticleMod.LOGGER.info("Registering Custom Trinket Items for " + EyelisssParticleMod.MOD_ID);
    }
}