package eyeliss.particle.mod.item.specialitems;

import eyeliss.particle.mod.component.EngravingContents;
import eyeliss.particle.mod.component.ModComponents;
import eyeliss.particle.mod.component.ModEngravings;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DwarvenChiselItem extends Item {
    public DwarvenChiselItem(Settings settings) { super(settings); }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.LEFT) return false;

        ItemStack targetStack = slot.getStack();
        if (targetStack.isEmpty()) return false;

        List<EngravingContents> current = targetStack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());

        for (EngravingContents e : current) {
            if (e.engravingId().equals("stagnation")) return false;
        }

        int totalLevelSum = 0;
        int maxCapacityLimit = 3;
        for (EngravingContents e : current) {
            totalLevelSum += e.level();
            if (e.engravingId().equals("transcendence")) {
                maxCapacityLimit += 2;
            }
        }

        if (totalLevelSum >= maxCapacityLimit) return false;

        Optional<String> rolledIdOpt = ModEngravings.rollEngravingFor(targetStack, player.getRandom());
        if (rolledIdOpt.isEmpty()) return false;
        String rolledId = rolledIdOpt.get();

        // 3. Special Rule: Transcendence cannot be added if item is already at baseline max
        if (rolledId.equals("transcendence") && totalLevelSum >= 3) return false;

        List<EngravingContents> updatedList = new ArrayList<>();
        boolean modified = false;

        for (EngravingContents entry : current) {
            if (entry.engravingId().equals(rolledId)) {
                // Blessings and Curses are strictly locked to maximum level 1
                if (ModEngravings.isBlessingOrCurse(rolledId)) return false;

                updatedList.add(new EngravingContents(rolledId, entry.level() + 1));
                modified = true;
            } else {
                updatedList.add(entry);
            }
        }

        if (!modified) updatedList.add(new EngravingContents(rolledId, 1));

        targetStack.set(ModComponents.ENGRAVING_CONTENTS, updatedList);

        // 4. Force apply Mending if Restoration Blessing activates using 1.21.1 Registry specifications
        if (rolledId.equals("restoration")) {
            ItemEnchantmentsComponent enchantments = targetStack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

            // FIX: Acquired the correct registry wrapper mapping for 1.21.1 platforms
            RegistryWrapper.Impl<Enchantment> enchantmentRegistry = player.getWorld().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT);

            // FIX: Lookup entry reference using RegistryKey directly
            Optional<RegistryEntry.Reference<Enchantment>> mendingRef = enchantmentRegistry.getOptional(Enchantments.MENDING);

            if (mendingRef.isPresent()) {
                ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchantments);
                builder.set(mendingRef.get(), 1);
                targetStack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
            }
        }

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 0.5f, 1.5f);
        stack.damage(1, player, PlayerEntity.getSlotForHand(player.getActiveHand()));
        return true;
    }
}
