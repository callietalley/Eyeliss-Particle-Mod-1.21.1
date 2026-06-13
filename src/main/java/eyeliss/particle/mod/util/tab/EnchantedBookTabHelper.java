package eyeliss.particle.mod.util.tab;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class EnchantedBookTabHelper {

    /**
     * Dynamically populates custom or vanilla enchanted books with specific level filters into a creative tab entries loop.
     *
     * @param entries         The Creative Tab entries manager list provider
     * @param registries      The RegistryWrapper context tracker required for data-driven 1.21.1 lookups
     * @param namespace       The mod id domain (e.g., "eyelisspartmod" or "minecraft")
     * @param path            The sub-nested or flat registry path to the enchantment (e.g., "syringe/chemical_infusion")
     * @param maxLevel        The ceiling level layer to calculate loops against
     * @param allLevels       If true, generates a book for every level from 1 to maxLevel
     * @param extremeLevels   If true (and allLevels is false), generates only the lowest (1) and highest (maxLevel) books
     */
    public static void addEnchantedBooks(ItemGroup.Entries entries, RegistryWrapper.WrapperLookup registries, String namespace, String path, int maxLevel, boolean allLevels, boolean extremeLevels) {
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
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);

        // 💡 1.21.1 Registry Lookup: Pulls the registry context dynamically from the world wrapper provider
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