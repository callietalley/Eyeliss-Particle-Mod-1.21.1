package eyeliss.particle.mod.item.trinkets;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
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
            new MidasGoldItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)) {
                @Override
                public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
                    if (net.fabricmc.api.EnvType.CLIENT == net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType()) {

                        if (eyeliss.particle.mod.util.TooltipHelper.isControlPressed()) {
                            tooltip.add(Text.literal("Grants the highest item pickup priority")
                                    .formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("Reduces pickup cooldown on dropped items by 33%")
                                    .formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("Killed mobs have a 25% chance to drop gold")
                                    .formatted(Formatting.GRAY));
                            tooltip.add(Text.literal(" ")
                                    .formatted(Formatting.GRAY));
                        } else {
                            tooltip.add(Text.literal("Makes you pick up items faster").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("Mobs can drop gold").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                            eyeliss.particle.mod.util.TooltipHelper.addControlPrompt(tooltip);
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                        }

                    }

                    super.appendTooltip(stack, context, tooltip, type);
                }
            }
    );

    public static final Item SADIMS_IRON = Registry.register(
            Registries.ITEM,
            Identifier.of("eyelisspartmod", "sadims_iron"),
            new SadimsIronItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)) {
                @Override
                public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
                        tooltip.add(Text.literal("Heavy, slows you to equip").formatted(Formatting.GRAY));
                        tooltip.add(Text.literal("Increased melee damage").formatted(Formatting.GRAY));
                        tooltip.add(Text.literal("Increased pickup range").formatted(Formatting.GRAY));
                        tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                    super.appendTooltip(stack, context, tooltip, type);
                }
            }
    );

    public static final Item HIDDEN_CLOAK = Registry.register(
            Registries.ITEM,
            Identifier.of("eyelisspartmod", "hidden_cloak"),
            new HiddenCloakItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)) {
                @Override
                public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
                    tooltip.add(Text.literal("Crafted at an Advanced Smithing Table").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("Hides your true form").formatted(Formatting.GRAY));
                    if (net.fabricmc.api.EnvType.CLIENT == net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType()) {
                        if (eyeliss.particle.mod.util.TooltipHelper.isControlPressed()) {
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("Changes your skin to a black cloaked skin").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("Changes your picture in the tab list as well").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("Toggleable with the Trinket Active key.").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                        } else {
                            eyeliss.particle.mod.util.TooltipHelper.addControlPrompt(tooltip);
                        }
                    }
                    super.appendTooltip(stack, context, tooltip, type);
                }
            }
    );

    public static final BloodStoneItem BLOOD_STONE_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of("eyelisspartmod", "bloodstone"),
            new BloodStoneItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)) {
                @Override
                public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
                    if (net.fabricmc.api.EnvType.CLIENT == net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType()) {

                        if (eyeliss.particle.mod.util.TooltipHelper.isControlPressed()) {
                            tooltip.add(Text.literal("Grants +5 hearts")
                                    .formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("If you go below 20% max hp, you steal health")
                                    .formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("from nearby enemies, with a 200 second cooldown")
                                    .formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("Killing most mobs gives regeneration 2 for 4 seconds")
                                    .formatted(Formatting.GRAY));
                            tooltip.add(Text.literal(" ")
                                    .formatted(Formatting.GRAY));
                        } else {
                            tooltip.add(Text.literal("Increases health, steals health when low").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("Killing heals you").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                            eyeliss.particle.mod.util.TooltipHelper.addControlPrompt(tooltip);
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                        }

                    }

                    super.appendTooltip(stack, context, tooltip, type);
                }
            }
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

                    if (net.fabricmc.api.EnvType.CLIENT == net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType()) {

                        if (eyeliss.particle.mod.util.TooltipHelper.isControlPressed()) {
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
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                        } else {
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                            eyeliss.particle.mod.util.TooltipHelper.addControlPrompt(tooltip);
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                        }

                    }

                    super.appendTooltip(stack, context, tooltip, type);
                }
            }
    );

    public static final Item NAMESPACE_WARPER = Registry.register(
            Registries.ITEM,
            Identifier.of("eyelisspartmod", "namespace_warper"),
            new NamespaceWarperItem(new Item.Settings().maxCount(1).maxDamage(380).rarity(Rarity.EPIC)) {
                @Override
                public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
                    tooltip.add(Text.literal("Allows the user to change their name").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("or the visibility of their nameplate").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("with the Secondary Trinket Key").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                    tooltip.add(Text.literal("Only takes damage if hit by a player").formatted(Formatting.GRAY));

                    if (net.fabricmc.api.EnvType.CLIENT == net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType()) {

                        if (eyeliss.particle.mod.util.TooltipHelper.isControlPressed()) {
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("Can click reset in the menu to set their name back").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("to their default name").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("Automatically resets name when unequipping").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("Due to the Trinkets mod's limitations").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("unequipping while in creative mode will only").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal("reset the custom name, half of the time").formatted(Formatting.GRAY));
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                        } else {
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                            eyeliss.particle.mod.util.TooltipHelper.addControlPrompt(tooltip);
                            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
                        }

                    }

                    super.appendTooltip(stack, context, tooltip, type);
                }
            }
    );

    public static void registerModTrinkets() {
        EyelisssParticleMod.LOGGER.info("Registering Custom Trinket Items for " + EyelisssParticleMod.MOD_ID);
    }
}