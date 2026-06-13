package eyeliss.particle.mod.util.tab;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.List;
import java.util.Optional;

public class PotionTabHelper {

    /**
     * Groups multiple tiers of the same potion effect side-by-side by item type.
     *
     * @param entries      The Creative Tab entries manager list provider
     * @param potionTiers  A list of your registered ModPotions reference entries for this effect
     */
    public static void addGroupedPotionTiers(ItemGroup.Entries entries, List<RegistryEntry.Reference<Potion>> potionTiers) {

        // 🧪 STEP 1: Loop through all tiers and place their Regular Drinkable Potion Bottles side-by-side
        for (RegistryEntry.Reference<Potion> potionEntry : potionTiers) {
            PotionContentsComponent contents = new PotionContentsComponent(
                    Optional.of(potionEntry),
                    Optional.empty(),
                    List.of()
            );
            ItemStack regularPotion = new ItemStack(Items.POTION);
            regularPotion.set(DataComponentTypes.POTION_CONTENTS, contents);
            entries.add(regularPotion);
        }

        // 🧪 STEP 2: Loop through all tiers and place their Splash Potion Bottles side-by-side
        for (RegistryEntry.Reference<Potion> potionEntry : potionTiers) {
            PotionContentsComponent contents = new PotionContentsComponent(
                    Optional.of(potionEntry),
                    Optional.empty(),
                    List.of()
            );
            ItemStack splashPotion = new ItemStack(Items.SPLASH_POTION);
            splashPotion.set(DataComponentTypes.POTION_CONTENTS, contents);
            entries.add(splashPotion);
        }

        // 🧪 STEP 3: Loop through all tiers and place their Lingering Potion Bottles side-by-side
        for (RegistryEntry.Reference<Potion> potionEntry : potionTiers) {
            PotionContentsComponent contents = new PotionContentsComponent(
                    Optional.of(potionEntry),
                    Optional.empty(),
                    List.of()
            );
            ItemStack lingeringPotion = new ItemStack(Items.LINGERING_POTION);
            lingeringPotion.set(DataComponentTypes.POTION_CONTENTS, contents);
            entries.add(lingeringPotion);
        }
    }
}
