package eyeliss.particle.mod.util.tab;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class ShadowBookTabHelper {

    /**
     * Dynamically populates custom shadow books with specific level filters into a creative tab entries loop.
     *
     * @param entries         The Creative Tab entries manager list provider
     * @param registries      The RegistryWrapper context tracker required for data-driven 1.21.1 lookups
     * @param namespace       The mod id domain (e.g., "eyelisspartmod" or "minecraft")
     * @param path            The registry path to the enchantment (e.g., "dagger/entomophage")
     * @param maxLevel        The ceiling level layer to calculate loops against
     * @param allLevels       If true, generates a shadow book for every level from 1 to maxLevel
     * @param extremeLevels   If true (and allLevels is false), generates only the lowest (1) and highest (maxLevel) shadow books
     */
    public static void addShadowBooks(ItemGroup.Entries entries, RegistryWrapper.WrapperLookup registries, String namespace, String path, int maxLevel, boolean allLevels, boolean extremeLevels) {
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
        // Fetch the raw custom shadow book item from the game registry safely
        var shadowBookItem = Registries.ITEM.get(Identifier.of("eyelisspartmod", "shadow_book"));
        ItemStack shadowBook = new ItemStack(shadowBookItem);

        // 💡 1.21.1 Registry Lookup: Pulls the enchantment sub-registry from the world context wrapper
        var enchantmentLookup = registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);

        RegistryKey<Enchantment> customEnchantKey = RegistryKey.of(
                RegistryKeys.ENCHANTMENT,
                Identifier.of(namespace, path)
        );

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        builder.add(enchantmentLookup.getOrThrow(customEnchantKey), level);

        // Sets DataComponentTypes.ENCHANTMENTS since this acts as a real tool/book with active enchant attributes applied
        shadowBook.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        return shadowBook;
    }
}