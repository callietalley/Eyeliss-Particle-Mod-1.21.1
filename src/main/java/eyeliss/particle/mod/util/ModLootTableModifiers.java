package eyeliss.particle.mod.util;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModLootTableModifiers {

    private static final RegistryKey<LootTable> CHICKEN_ID =
            RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("minecraft", "entities/chicken"));

    private static final RegistryKey<LootTable> CUSTOM_DROP_ID =
            RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("eyelisspartmod", "inject/shadow_feather"));

    public static void modifyLootTables() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {

            if (CHICKEN_ID.equals(key) && source.isBuiltin()) {

                LootPool.Builder poolBuilder = LootPool.builder()
                        .with(LootTableEntry.builder(CUSTOM_DROP_ID));

                tableBuilder.pool(poolBuilder);
            }
        });
    }
}