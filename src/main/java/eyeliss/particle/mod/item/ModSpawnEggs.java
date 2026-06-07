package eyeliss.particle.mod.item;

import eyeliss.particle.mod.entity.ModEntities; // Replace with your entity registry class path
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModSpawnEggs {

    // 1. Instantiate the spawn egg using 1.21.1 property builders
    // Parameters: Primary background color (hex), Secondary spots color (hex)
    public static final Item UMBERWITHER_SPAWN_EGG = new SpawnEggItem(
            ModEntities.UMBERWITHER, // Your custom EntityType reference
            0xFFFFFF,                // Primary color (Dark Umber Brown)
            0x5C4033,                // Secondary color (Lighter Accent Brown)
            new Item.Settings()
    );

    public static void registerModSpawnEggs() {
        // 2. Register the item under your mod's namespace
        Registry.register(
                Registries.ITEM,
                Identifier.of("eyelisspartmod", "umberwither_spawn_egg"),
                UMBERWITHER_SPAWN_EGG
        );
    }
}