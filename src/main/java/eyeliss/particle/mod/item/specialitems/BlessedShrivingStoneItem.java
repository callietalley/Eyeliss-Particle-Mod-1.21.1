package eyeliss.particle.mod.item;

import eyeliss.particle.mod.component.EngravingContents;
import eyeliss.particle.mod.component.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import java.util.ArrayList;
import java.util.List;

public class BlessedShrivingStoneItem extends Item {
    public BlessedShrivingStoneItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        // Run only when left-clicking this stone onto another item in the inventory grid
        if (clickType != ClickType.LEFT) return false;

        ItemStack targetStack = slot.getStack();
        if (targetStack.isEmpty()) return false;

        List<EngravingContents> current = targetStack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());
        if (current.isEmpty()) return false;

        List<EngravingContents> updatedList = new ArrayList<>();
        boolean foundAndRemovedCurse = false;

        for (EngravingContents entry : current) {
            String id = entry.engravingId();

            // Catch the first curse type encountered
            if (!foundAndRemovedCurse && (id.equals("stagnation") || id.equals("ruin"))) {
                // Replace it one-to-one with a level 1 Baptism blessing slot filler
                updatedList.add(new EngravingContents("baptism", 1));
                foundAndRemovedCurse = true;
            } else {
                updatedList.add(entry);
            }
        }

        // Exit out safely if the item had no curses to break
        if (!foundAndRemovedCurse) return false;

        // Apply updated component list onto target item stack data
        targetStack.set(ModComponents.ENGRAVING_CONTENTS, updatedList);

        // Play a satisfying holy/glass shriving breaking audio effect
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.PLAYERS, 0.6f, 1.2f);

        // Consume exactly 1 item count from the player's held stone stack
        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        return true;
    }
}