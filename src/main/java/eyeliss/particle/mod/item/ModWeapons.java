package eyeliss.particle.mod.item;

import eyeliss.particle.mod.EyelisssParticleMod;
import eyeliss.particle.mod.item.specialweapons.KhopeshItem;
import eyeliss.particle.mod.item.specialweapons.ModKhopeshMaterials;
import eyeliss.particle.mod.item.specialweapons.SyringeItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModWeapons {
    public static final Item TRAINING_DAGGER = registerItem("training_dagger", new DaggerItem(ModDaggerMaterials.TRAINING_DAGGER, new Item.Settings()));
    public static final Item COPPER_DAGGER = registerItem("copper_dagger", new DaggerItem(ModDaggerMaterials.COPPER_DAGGER, new Item.Settings()));
    public static final Item QUARTZ_DAGGER = registerItem("quartz_dagger", new DaggerItem(ModDaggerMaterials.QUARTZ_DAGGER, new Item.Settings()));
    public static final Item AMETHYST_DAGGER = registerItem("amethyst_dagger", new DaggerItem(ModDaggerMaterials.AMETHYST_DAGGER, new Item.Settings()));
    public static final Item EMERALD_DAGGER = registerItem("emerald_dagger", new DaggerItem(ModDaggerMaterials.EMERALD_DAGGER, new Item.Settings()));

    public static final Item NETHERITE_DAGGER = registerItem("netherite_dagger", new DaggerItem(ModDaggerMaterials.NETHERITE_DAGGER, new Item.Settings().fireproof()));

    public static final Item TRAINING_SPEAR = registerItem("training_spear", new SpearItem(ModSpearMaterials.TRAINING_SPEAR, new Item.Settings()));
    public static final Item COPPER_SPEAR = registerItem("copper_spear", new SpearItem(ModSpearMaterials.COPPER_SPEAR, new Item.Settings()));
    public static final Item QUARTZ_SPEAR = registerItem("quartz_spear", new SpearItem(ModSpearMaterials.QUARTZ_SPEAR, new Item.Settings()));
    public static final Item AMETHYST_SPEAR = registerItem("amethyst_spear", new SpearItem(ModSpearMaterials.AMETHYST_SPEAR, new Item.Settings()));
    public static final Item EMERALD_SPEAR = registerItem("emerald_spear", new SpearItem(ModSpearMaterials.EMERALD_SPEAR, new Item.Settings()));

    public static final Item NETHERITE_SPEAR = registerItem("netherite_spear", new SpearItem(ModSpearMaterials.NETHERITE_SPEAR, new Item.Settings().fireproof()));

    public static final Item ANCIENT_KHOPESH = registerItem("ancient_khopesh", new KhopeshItem(ModKhopeshMaterials.ANCIENT_KHOPESH, new Item.Settings().fireproof()));

    public static final Item SYRINGE = registerItem("syringe", new SyringeItem(
            250,      // Max Durability
            3.0,      // Base Attack Damage
            -1.8,     // Attack Speed modifier
            22,       // Enchantability rating
            new Item.Settings().fireproof().rarity(Rarity.UNCOMMON)
    ));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(EyelisssParticleMod.MOD_ID, name), item);
    }

    public static void registerModWeapons () {
        EyelisssParticleMod.LOGGER.info("Registering Mod Weapons for " + EyelisssParticleMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            //Daggers
            entries.add(TRAINING_DAGGER);
            entries.add(COPPER_DAGGER);
            entries.add(QUARTZ_DAGGER);
            entries.add(AMETHYST_DAGGER);
            entries.add(EMERALD_DAGGER);
            entries.add(NETHERITE_DAGGER);
            //Spears
            entries.add(TRAINING_SPEAR);
            entries.add(COPPER_SPEAR);
            entries.add(QUARTZ_SPEAR);
            entries.add(AMETHYST_SPEAR);
            entries.add(EMERALD_SPEAR);
            entries.add(NETHERITE_SPEAR);
            //Special
            entries.add(ANCIENT_KHOPESH);
        });
    }
}