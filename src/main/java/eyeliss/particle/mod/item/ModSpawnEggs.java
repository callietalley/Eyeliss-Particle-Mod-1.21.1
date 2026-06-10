package eyeliss.particle.mod.item;

import eyeliss.particle.mod.entity.ModEntities; // Replace with your entity registry class path
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModSpawnEggs {

    public static final Item UMBERWITHER_SPAWN_EGG = new SpawnEggItem(
            ModEntities.UMBERWITHER,
            0xFFFFFF,
            0x5C4033,
            new Item.Settings()
    );

    public static void registerModSpawnEggs() {
        Registry.register(
                Registries.ITEM,
                Identifier.of("eyelisspartmod", "umberwither_spawn_egg"),
                UMBERWITHER_SPAWN_EGG
        );
    }
}