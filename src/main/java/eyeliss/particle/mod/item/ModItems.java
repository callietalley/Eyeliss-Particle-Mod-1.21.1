package eyeliss.particle.mod.item;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item SLOP = registerItem("slop", new Item(new Item.Settings()));
    public static final Item COOKED_SLOP = registerItem("cooked_slop", new Item(new Item.Settings()));


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
