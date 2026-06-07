package eyeliss.particle.mod.item;

import eyeliss.particle.mod.EyelisssParticleMod;
import eyeliss.particle.mod.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static eyeliss.particle.mod.item.ModItems.*;
import static eyeliss.particle.mod.item.ModWeapons.*;

public class ModItemGroups {
    public static final ItemGroup EYELISS_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(EyelisssParticleMod.MOD_ID, "eyeliss_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.SLOP))
                    .displayName(Text.translatable("itemgroup.eyelisspartmod.eyeliss_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.SLOP);
                        entries.add(ModItems.COOKED_SLOP);
                        entries.add(ModItems.RED_POWER_STONE);
                        entries.add(ModItems.BLUE_POWER_STONE);
                        entries.add(ModItems.GREEN_POWER_STONE);
                        entries.add(ModItems.YELLOW_POWER_STONE);
                        entries.add(ModItems.PURPLE_POWER_STONE);
                        entries.add(ModItems.BIRD_STONE);
                        entries.add(POWER_CORE);
                        entries.add(SHADOW_TOUCHED_FEATHER);
                        entries.add(LONG_STICK);
                        entries.add(SHADOW_BOOK);

                    }).build());

    public static final ItemGroup EYELISS_BLOCKS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(EyelisssParticleMod.MOD_ID, "eyeliss_blocks"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModBlocks.SLOP_BLOCK))
                    .displayName(Text.translatable("itemgroup.eyelisspartmod.eyeliss_blocks"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.SLOP_BLOCK);
                        entries.add(ModBlocks.COOKED_SLOP_BLOCK);

                    }).build());

    public static final ItemGroup EYELISS_WEAPONS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(EyelisssParticleMod.MOD_ID, "eyeliss_weapons"),
            FabricItemGroup.builder().icon(() -> new ItemStack(NETHERITE_DAGGER))
                    .displayName(Text.translatable("itemgroup.eyelisspartmod.eyeliss_weapons"))
                    .entries((displayContext, entries) -> {
                        RegistryWrapper.WrapperLookup registries = displayContext.lookup();
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

                        //Dagger Enchantments
                        addEnchantedBook(entries, registries, "eyelisspartmod", "dagger/entomophage", 6, false, true);
                        addEnchantedBook(entries, registries, "eyelisspartmod", "dagger/razor_edge", 6, false, true);
                        addEnchantedBook(entries, registries, "eyelisspartmod", "dagger/turning", 6, false, true);
                        addEnchantedBook(entries, registries, "eyelisspartmod", "dagger/filleting", 5, false, true);
                        addEnchantedBook(entries, registries, "eyelisspartmod", "dagger/piercing", 3, false, true);
                        addEnchantedBook(entries, registries, "eyelisspartmod", "dagger/thousand_cuts", 3, false, true);
                        addEnchantedBook(entries, registries, "eyelisspartmod", "dagger/styx_curse", 3, false, true);
                        //Spear Enchantments
                        addEnchantedBook(entries, registries, "eyelisspartmod", "spear/jabber", 1, false, false);
                        addEnchantedBook(entries, registries, "eyelisspartmod", "spear/ethereal_reach", 1, false, false);
                        addEnchantedBook(entries, registries, "eyelisspartmod", "spear/pointed", 5, false, true);
                    }).build());

    public static final ItemGroup EYELISS_FUN_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(EyelisssParticleMod.MOD_ID, "eyeliss_zfun"),
            FabricItemGroup.builder().icon(() -> new ItemStack(CUSTOM_BIRD))
                    .displayName(Text.translatable("itemgroup.eyelisspartmod.eyeliss_fun"))
                    .entries((displayContext, entries) -> {
                        RegistryWrapper.WrapperLookup registries = displayContext.lookup();
                        //enchants
                        addShadowBook(entries, registries, "eyelisspartmod", "spear/jabber", 10, true, false);
                        addShadowBook(entries, registries, "eyelisspartmod", "spear/jabber", 20, false, false);
                        addShadowBook(entries, registries, "eyelisspartmod", "spear/ethereal_reach", 5, true, false);
                        addShadowBook(entries, registries, "eyelisspartmod", "dagger/thousand_cuts", 18, false, false);
                        addShadowBook(entries, registries, "eyelisspartmod", "dagger/styx_curse", 35, false, false);
                        entries.add(ModSpawnEggs.UMBERWITHER_SPAWN_EGG);

                    }).build());

    public static void registerItemGroups() {
        EyelisssParticleMod.LOGGER.info("Registering Item Groups for " + EyelisssParticleMod.MOD_ID);
    }

    private static void addShadowBook(ItemGroup.Entries entries, RegistryWrapper.WrapperLookup registries, String namespace, String path, int maxLevel, boolean allLevels, boolean extremeLevels) {
        if (allLevels) {
            for (int level = 1; level <= maxLevel; level++) {
                entries.add(createShadowBook(registries, namespace, path, level));
            }
        } else {
            if (extremeLevels && maxLevel > 1) {
                entries.add(createShadowBook(registries, namespace, path, 1));
                entries.add(createShadowBook(registries, namespace, path, maxLevel));
            } else {
                entries.add(createShadowBook(registries, namespace, path, maxLevel));
            }
        }
    }

    private static ItemStack createShadowBook(RegistryWrapper.WrapperLookup registries, String namespace, String path, int level) {
        var shadowBookItem = Registries.ITEM.get(Identifier.of("eyelisspartmod", "shadow_book"));
        ItemStack shadowBook = new ItemStack(shadowBookItem);

        var enchantmentLookup = registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);

        RegistryKey<Enchantment> customEnchantKey = RegistryKey.of(
                RegistryKeys.ENCHANTMENT,
                Identifier.of(namespace, path)
        );

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        builder.add(enchantmentLookup.getOrThrow(customEnchantKey), level);

        shadowBook.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        return shadowBook;
    }

    private static void addEnchantedBook(ItemGroup.Entries entries, RegistryWrapper.WrapperLookup registries, String namespace, String path, int maxLevel, boolean allLevels, boolean extremeLevels) {
        if (allLevels) {
            for (int level = 1; level <= maxLevel; level++) {
                entries.add(createEnchantedBook(registries, namespace, path, level));
            }
        } else {
            if (extremeLevels && maxLevel > 1) {
                entries.add(createEnchantedBook(registries, namespace, path, 1));
                entries.add(createEnchantedBook(registries, namespace, path, maxLevel));
            } else {
                entries.add(createEnchantedBook(registries, namespace, path, maxLevel));
            }
        }
    }

    private static ItemStack createEnchantedBook(RegistryWrapper.WrapperLookup registries, String namespace, String path, int level) {
        ItemStack enchantedBook = new ItemStack(net.minecraft.item.Items.ENCHANTED_BOOK);

        var enchantmentLookup = registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);

        RegistryKey<Enchantment> customEnchantKey = RegistryKey.of(
                RegistryKeys.ENCHANTMENT,
                Identifier.of(namespace, path)
        );

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        builder.add(enchantmentLookup.getOrThrow(customEnchantKey), level);

        enchantedBook.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());
        return enchantedBook;
    }
}