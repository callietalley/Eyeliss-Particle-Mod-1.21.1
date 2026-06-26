package eyeliss.particle.mod.item;

import eyeliss.particle.mod.EyelisssParticleMod;
import eyeliss.particle.mod.item.specialitems.DenseShrivingStoneItem;
import eyeliss.particle.mod.item.specialitems.DwarvenChiselItem;
import eyeliss.particle.mod.item.specialitems.GeologicShrivingStoneItem;
import eyeliss.particle.mod.item.specialitems.MagicalShrivingStoneItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.List;

public class ModItems {
    public static final Item SHADOW_BUNDLE = registerItem("shadow_bundle",
            new ShadowBundleItem(new Item.Settings()
                    .maxCount(1)
                    .component(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT)
                    .rarity(Rarity.RARE)
                    .fireproof()
            )
    );

    public static final Item DEPLETED_ESSENCE = registerItem("depleted_essence", new DepletedEssenceItem(new Item.Settings()) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
            tooltip.add(Text.literal("Obtained attempting to craft an item that cannot be crafted anymore").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Use to turn into a material used in any of the rare items").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Rarely becomes the rare materials from these items").formatted(Formatting.GRAY));

            super.appendTooltip(stack, context, tooltip, type);
        }
    });

    public static final Item SLOP = registerItem("slop", new Item(new Item.Settings()));
    public static final Item COOKED_SLOP = registerItem("cooked_slop", new Item(new Item.Settings()));
    public static final Item DIMENSIONAL_DUST = registerItem("dimensional_dust", new Item(new Item.Settings().rarity(Rarity.RARE)) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
            tooltip.add(Text.literal("Obtained by killing a Ghast in the end with a 100% chance").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Or by killing an Endermite in the Nether with a 0.25% chance").formatted(Formatting.GRAY));

            super.appendTooltip(stack, context, tooltip, type);
        }
    });
    public static final Item SUSPENSEFUL_ESSENCE = registerItem("suspenseful_essence", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
            tooltip.add(Text.literal("Obtained by killing a Creeper that is 75%").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("or more through it's exploding process").formatted(Formatting.GRAY));

            super.appendTooltip(stack, context, tooltip, type);
        }
    });

    public static final Item BLOOD_SHARD = registerItem("blood_shard", new Item(new Item.Settings().fireproof().rarity(Rarity.UNCOMMON)) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
            tooltip.add(Text.literal("Obtained by killing 100 mobs").formatted(Formatting.GRAY));
            tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));

            super.appendTooltip(stack, context, tooltip, type);
        }
    });

    public static final Item HARMONIOUS_ESSENCE = registerItem("harmonious_essence",
            new HarmoniousEssenceItem(new Item.Settings()));
    public static final Item SHADOW_TOUCHED_FEATHER = registerItem("shadow_feather", new Item(new Item.Settings().rarity(Rarity.RARE)) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
            // Adds the text line and tints it light purple (aqua/purple/italic styles look great for end loot!)
            tooltip.add(Text.literal("Obtained by killing a chicken, with a low chance").formatted(Formatting.GRAY));

            super.appendTooltip(stack, context, tooltip, type);
        }
    });
    public static final Item CUSTOM_BIRD = registerItem("custom_bird", new Item(new Item.Settings()));
    public static final Item LONG_STICK = registerItem("long_stick", new Item(new Item.Settings().maxCount(16)));

    private static final FoodComponent POWER_STONE_COMPONENT = new FoodComponent.Builder()
            .nutrition(0)
            .saturationModifier(0f)
            .alwaysEdible()
            .build();

    public static final Item RED_POWER_STONE = registerItem("red_power_stone", new PowerStoneItem(new Item.Settings().food(POWER_STONE_COMPONENT).fireproof().maxCount(1)));
    public static final Item BLUE_POWER_STONE = registerItem("blue_power_stone", new PowerStoneItem(new Item.Settings().food(POWER_STONE_COMPONENT).fireproof().maxCount(1)));
    public static final Item GREEN_POWER_STONE = registerItem("green_power_stone", new PowerStoneItem(new Item.Settings().food(POWER_STONE_COMPONENT).fireproof().maxCount(1)));
    public static final Item YELLOW_POWER_STONE = registerItem("yellow_power_stone", new PowerStoneItem(new Item.Settings().food(POWER_STONE_COMPONENT).fireproof().maxCount(1)));
    public static final Item PURPLE_POWER_STONE = registerItem("purple_power_stone", new PowerStoneItem(new Item.Settings().food(POWER_STONE_COMPONENT).fireproof().maxCount(1)));
    public static final Item BIRD_STONE = registerItem("bird_stone", new PowerStoneItem(new Item.Settings().food(POWER_STONE_COMPONENT).fireproof().maxCount(1)));
    public static final Item POWER_CORE = registerItem("power_core", new PowerCoreItem(new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(EyelisssParticleMod.MOD_ID, name), item);
    }

    public static final Item SHADOW_BOOK = registerItem("shadow_book", new EnchantedBookItem(new Item.Settings().maxCount(1).fireproof().rarity(Rarity.UNCOMMON)));

    public static final Item SHRIVING_CORE = registerItem("shriving_core", new Item(new Item.Settings().maxCount(16).fireproof()) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
            tooltip.add(Text.literal("Used in creating a Shriving Stone.").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Crafted in an Advanced Smithing Table").formatted(Formatting.GRAY));

            super.appendTooltip(stack, context, tooltip, type);
        }
    });

    public static final Item MAGICAL_SHRIVING_STONE = registerItem("magical_shriving_stone", new MagicalShrivingStoneItem(new Item.Settings().maxCount(4).fireproof().rarity(Rarity.UNCOMMON)) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
            tooltip.add(Text.literal("Applies a Blood thirst to the item, which once fulfilled, applies an engraving.").formatted(Formatting.GRAY));

            super.appendTooltip(stack, context, tooltip, type);
        }
    });
    public static final Item GEOLOGIC_SHRIVING_STONE = registerItem("geologic_shriving_stone", new GeologicShrivingStoneItem(new Item.Settings().maxCount(4).fireproof().rarity(Rarity.UNCOMMON)) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
            tooltip.add(Text.literal("Applies a Geologic hunger to the item, which once fulfilled, applies an engraving.").formatted(Formatting.GRAY));

            super.appendTooltip(stack, context, tooltip, type);
        }
    });
    public static final Item DENSE_SHRIVING_STONE = registerItem("dense_shriving_stone", new DenseShrivingStoneItem(new Item.Settings().maxCount(4).fireproof().rarity(Rarity.UNCOMMON)) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
            tooltip.add(Text.literal("Removes the currently applied shriving stone.").formatted(Formatting.GRAY));

            super.appendTooltip(stack, context, tooltip, type);
        }
    });
    public static final Item DWARVEN_CHISEL = registerItem("dwarven_chisel", new DwarvenChiselItem(new Item.Settings().maxCount(1).fireproof().rarity(Rarity.EPIC)) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
            tooltip.add(Text.literal("Instantly applies a random engraving applicable to the item.").formatted(Formatting.GRAY));

            super.appendTooltip(stack, context, tooltip, type);
        }
    });
    public static final Item BLESSED_SHRIVING_STONE = registerItem("blessed_shriving_stone", new eyeliss.particle.mod.item.specialitems.BlessedShrivingStoneItem(new Item.Settings().maxCount(4).fireproof().rarity(Rarity.RARE)) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
            tooltip.add(Text.literal("Cleanses cursed engravings.").formatted(Formatting.GRAY));

            super.appendTooltip(stack, context, tooltip, type);
        }
    });
    public static final Item ENGRAVING_GUIDE_BOOK = registerItem("engraving_guide_book", new eyeliss.particle.mod.item.specialitems.EngravingGuideBookItem(new Item.Settings().maxCount(1)) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
            tooltip.add(Text.literal("Tells you everything you need to know about engravings.").formatted(Formatting.GRAY));

            super.appendTooltip(stack, context, tooltip, type);
        }
    });

    public static void registerModItems () {
        EyelisssParticleMod.LOGGER.info("Registering Mod Items for " + EyelisssParticleMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(SLOP);
            entries.add(COOKED_SLOP);
        });
    }
}