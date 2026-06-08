package eyeliss.particle.mod.item;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class VanillaItemGroupAdditions {

    public static void registerItemGroups() {
        EyelisssParticleMod.LOGGER.info("Adding items to Vanilla Tabs for " + EyelisssParticleMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {

            ItemStack vanillaBundle = new ItemStack(Items.BUNDLE);

            vanillaBundle.set(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);

            entries.add(vanillaBundle);
        });
    }
}