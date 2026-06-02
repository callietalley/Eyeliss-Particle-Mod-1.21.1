package eyeliss.particle.mod.item;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    // Your original slop items
    public static final Item SLOP = registerItem("slop", new Item(new Item.Settings()));
    public static final Item COOKED_SLOP = registerItem("cooked_slop", new Item(new Item.Settings()));

    // Shared food property for power stones: consumable, always edible, no effects or hunger
    private static final FoodComponent POWER_STONE_COMPONENT = new FoodComponent.Builder()
            .nutrition(0)
            .saturationModifier(0f)
            .alwaysEdible()
            .build();

    // 5 Power stones renamed to colors
    public static final Item RED_POWER_STONE = registerItem("red_power_stone",
            new Item(new Item.Settings().food(POWER_STONE_COMPONENT)));

    public static final Item BLUE_POWER_STONE = registerItem("blue_power_stone",
            new Item(new Item.Settings().food(POWER_STONE_COMPONENT)));

    public static final Item GREEN_POWER_STONE = registerItem("green_power_stone",
            new Item(new Item.Settings().food(POWER_STONE_COMPONENT)));

    public static final Item YELLOW_POWER_STONE = registerItem("yellow_power_stone",
            new Item(new Item.Settings().food(POWER_STONE_COMPONENT)));

    public static final Item PURPLE_POWER_STONE = registerItem("purple_power_stone",
            new Item(new Item.Settings().food(POWER_STONE_COMPONENT)));

    public static final Item POWER_CORE = registerItem("power_core",
            new PowerCoreItem(new Item.Settings()));

    public static final Item CUSTOM_BIRD = registerItem("custom_bird",
            new Item(new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(EyelisssParticleMod.MOD_ID, name), item);
    }

    public static void registerModItems () {
        EyelisssParticleMod.LOGGER.info("Registering Mod Items for " + EyelisssParticleMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(SLOP);
            entries.add(COOKED_SLOP);
        });
    }
}