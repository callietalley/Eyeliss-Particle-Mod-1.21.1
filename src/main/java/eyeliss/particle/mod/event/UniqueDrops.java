package eyeliss.particle.mod.event;

import eyeliss.particle.mod.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.LocationCheckLootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition; // Added for the 5% chance
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class UniqueDrops {

    public static void registerDrops() {
        // Core Target Identifiers
        final Identifier GHAST_LOOT_TABLE_ID = Identifier.of("minecraft", "entities/ghast");
        final Identifier ENDERMITE_LOOT_TABLE_ID = Identifier.of("minecraft", "entities/endermite");

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {

            if (GHAST_LOOT_TABLE_ID.equals(key.getValue())) {
                LootPool.Builder poolBuilder = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1.0f))
                        .with(ItemEntry.builder(ModItems.DIMENSIONAL_DUST))
                        .conditionally(LocationCheckLootCondition.builder(
                                LocationPredicate.Builder.create()
                                        .dimension(RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "the_end")))
                        ));

                tableBuilder.pool(poolBuilder);
            }

            if (ENDERMITE_LOOT_TABLE_ID.equals(key.getValue())) {
                LootPool.Builder poolBuilder = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1.0f))
                        .with(ItemEntry.builder(ModItems.DIMENSIONAL_DUST))
                        .conditionally(LocationCheckLootCondition.builder(
                                LocationPredicate.Builder.create()
                                        .dimension(RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "the_nether")))
                        ))
                        .conditionally(RandomChanceLootCondition.builder(0.0025f));

                tableBuilder.pool(poolBuilder);
            }
        });
    }
}
