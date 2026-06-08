package eyeliss.particle.mod.item;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {
    public static final Item SHADOW_BUNDLE = registerItem("shadow_bundle",
            new ShadowBundleItem(new Item.Settings()
                    .maxCount(1)
                    .component(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT)
                    .rarity(Rarity.RARE)
                    .fireproof()
            )
    );

    public static final Item SLOP = registerItem("slop", new Item(new Item.Settings()));
    public static final Item COOKED_SLOP = registerItem("cooked_slop", new Item(new Item.Settings()));
    public static final Item SHADOW_TOUCHED_FEATHER = registerItem("shadow_feather", new Item(new Item.Settings().rarity(Rarity.RARE)));
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

    public static void registerModItems () {
        EyelisssParticleMod.LOGGER.info("Registering Mod Items for " + EyelisssParticleMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(SLOP);
            entries.add(COOKED_SLOP);
        });
    }
}