package eyeliss.particle.mod.item;

import eyeliss.particle.mod.EyelisssParticleMod;
import eyeliss.particle.mod.block.ModBlocks;
import eyeliss.particle.mod.fluid.ModFluids;
import eyeliss.particle.mod.item.trinkets.ModTrinkets;
import eyeliss.particle.mod.potion.ModPotions;
import eyeliss.particle.mod.util.tab.EnchantedBookTabHelper;
import eyeliss.particle.mod.util.tab.PotionTabHelper;
import eyeliss.particle.mod.util.tab.ShadowBookTabHelper;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static eyeliss.particle.mod.item.ModItems.*;
import static eyeliss.particle.mod.item.ModWeapons.*;

public class ModItemGroups {
    public static final ItemGroup EYELISS_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(EyelisssParticleMod.MOD_ID, "eyeliss_aitems"),
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
                        entries.add(DIMENSIONAL_DUST);
                        entries.add(SUSPENSEFUL_ESSENCE);
                        entries.add(HARMONIOUS_ESSENCE);
                        entries.add(BLOOD_SHARD);
                        entries.add(LONG_STICK);
                        entries.add(SHADOW_BOOK);
                        entries.add(SHADOW_BUNDLE);
                        entries.add(DEPLETED_ESSENCE);
                        entries.add(ENGRAVING_GUIDE_BOOK);
                        entries.add(SHRIVING_CORE);
                        entries.add(MAGICAL_SHRIVING_STONE);
                        entries.add(GEOLOGIC_SHRIVING_STONE);
                        entries.add(DENSE_SHRIVING_STONE);
                        entries.add(BLESSED_SHRIVING_STONE);
                        entries.add(DWARVEN_CHISEL);

                    }).build());

    public static final ItemGroup EYELISS_TRINKETS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(EyelisssParticleMod.MOD_ID, "eyeliss_btrinkets"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModTrinkets.MIDAS_GOLD))
                    .displayName(Text.translatable("itemgroup.eyelisspartmod.eyeliss_trinkets"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModTrinkets.MIDAS_GOLD);
                        entries.add(ModTrinkets.SADIMS_IRON);
                        entries.add(ModTrinkets.BLOOD_STONE_ITEM);
                        entries.add(ModTrinkets.RIFT_GEM);
                        entries.add(ModTrinkets.NAMESPACE_WARPER);
                        entries.add(ModTrinkets.HIDDEN_CLOAK);
                    })
                    .build()
    );


    public static final ItemGroup EYELISS_BLOCKS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(EyelisssParticleMod.MOD_ID, "eyeliss_cblocks"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModBlocks.SLOP_BLOCK))
                    .displayName(Text.translatable("itemgroup.eyelisspartmod.eyeliss_blocks"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.SLOP_BLOCK);
                        entries.add(ModBlocks.COOKED_SLOP_BLOCK);
                        entries.add(ModBlocks.ADVANCED_WEAPON_SMITHING_BLOCK);
                        entries.add(ModBlocks.SOURCE_BLOCK);
                        entries.add(ModBlocks.DEEP_SOURCE_BLOCK);
                        entries.add(ModFluids.SOURCE_SAUCE_BUCKET);

                    }).build());

    public static final ItemGroup EYELISS_WEAPONS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(EyelisssParticleMod.MOD_ID, "eyeliss_dweapons"),
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

                        //Special
                        entries.add(ANCIENT_KHOPESH);
                        entries.add(SYRINGE);

                        //Dagger Enchantments
                        EnchantedBookTabHelper.addEnchantedBooks(entries, registries, "eyelisspartmod", "dagger/entomophage", 6, false, true);
                        EnchantedBookTabHelper.addEnchantedBooks(entries, registries, "eyelisspartmod", "dagger/razor_edge", 6, false, true);
                        EnchantedBookTabHelper.addEnchantedBooks(entries, registries, "eyelisspartmod", "dagger/turning", 6, false, true);
                        EnchantedBookTabHelper.addEnchantedBooks(entries, registries, "eyelisspartmod", "dagger/filleting", 5, false, true);
                        EnchantedBookTabHelper.addEnchantedBooks(entries, registries, "eyelisspartmod", "dagger/piercing", 3, false, true);
                        EnchantedBookTabHelper.addEnchantedBooks(entries, registries, "eyelisspartmod", "dagger/thousand_cuts", 3, false, true);
                        EnchantedBookTabHelper.addEnchantedBooks(entries, registries, "eyelisspartmod", "dagger/styx_curse", 3, false, true);
                        //Spear Enchantments
                        EnchantedBookTabHelper.addEnchantedBooks(entries, registries, "eyelisspartmod", "spear/jabber", 1, false, false);
                        EnchantedBookTabHelper.addEnchantedBooks(entries, registries, "eyelisspartmod", "spear/ethereal_reach", 1, false, false);
                        EnchantedBookTabHelper.addEnchantedBooks(entries, registries, "eyelisspartmod", "spear/pointed", 5, false, true);
                        //Special Enchantments
                        EnchantedBookTabHelper.addEnchantedBooks(entries, registries, "eyelisspartmod", "khopesh/hooking", 1, false, false);
                        EnchantedBookTabHelper.addEnchantedBooks(entries, registries, "eyelisspartmod", "syringe/chemical_infusion", 1, false, false);
                        EnchantedBookTabHelper.addEnchantedBooks(entries, registries, "eyelisspartmod", "syringe/chemical_burst", 1, false, false);
                    }).build());

    public static final ItemGroup EYELISS_FUN_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(EyelisssParticleMod.MOD_ID, "eyeliss_zfun"),
            FabricItemGroup.builder().icon(() -> new ItemStack(CUSTOM_BIRD))
                    .displayName(Text.translatable("itemgroup.eyelisspartmod.eyeliss_fun"))
                    .entries((displayContext, entries) -> {
                        RegistryWrapper.WrapperLookup registries = displayContext.lookup();
                        //enchants
                        ShadowBookTabHelper.addShadowBooks(entries, registries, "eyelisspartmod", "spear/jabber", 10, true, false);
                        ShadowBookTabHelper.addShadowBooks(entries, registries, "eyelisspartmod", "spear/jabber", 20, false, false);
                        ShadowBookTabHelper.addShadowBooks(entries, registries, "eyelisspartmod", "spear/ethereal_reach", 5, true, false);
                        ShadowBookTabHelper.addShadowBooks(entries, registries, "eyelisspartmod", "dagger/thousand_cuts", 18, false, false);
                        ShadowBookTabHelper.addShadowBooks(entries, registries, "eyelisspartmod", "dagger/styx_curse", 35, false, false);
                        ShadowBookTabHelper.addShadowBooks(entries, registries, "eyelisspartmod", "khopesh/hooking", 4, false, false);
                        entries.add(ModSpawnEggs.UMBERWITHER_SPAWN_EGG);
                        PotionTabHelper.addGroupedPotionTiers(entries, List.of(
                                ModPotions.STYX_POISON_POTION,
                                ModPotions.STYX_POISON_IV_POTION
                        ));
                        PotionTabHelper.addGroupedPotionTiers(entries, List.of(
                                ModPotions.BLEEDING_OUT_POTION,
                                ModPotions.BLEEDING_OUT_III_POTION
                        ));
                        PotionTabHelper.addGroupedPotionTiers(entries, List.of(
                                ModPotions.OVERHEALTH_POTION
                        ));
                        PotionTabHelper.addGroupedPotionTiers(entries, List.of(
                                ModPotions.SUNDERED_POTION,
                                ModPotions.SUNDERED_III_POTION
                        ));
                    }).build());

    public static void registerItemGroups() {
        EyelisssParticleMod.LOGGER.info("Registering Item Groups for " + EyelisssParticleMod.MOD_ID);
    }
}