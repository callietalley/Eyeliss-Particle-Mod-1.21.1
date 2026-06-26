package eyeliss.particle.mod.item.specialitems;

import eyeliss.particle.mod.component.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;

public class DenseShrivingStoneItem extends Item {
    public DenseShrivingStoneItem(Settings settings) { super(settings); }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.LEFT) return false;

        ItemStack targetStack = slot.getStack();
        // Checks and wipes out EITHER form of upgrade component tracker data, but ignores Blessed stones
        if (targetStack.isEmpty() || (!targetStack.contains(ModComponents.SHRIVING_CHARGE) && !targetStack.contains(ModComponents.BLOCK_CHARGE))) {
            return false;
        }

        targetStack.remove(ModComponents.SHRIVING_CHARGE);
        targetStack.remove(ModComponents.BLOCK_CHARGE);

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.PLAYERS, 0.6f, 0.8f);

        if (!player.getAbilities().creativeMode) stack.decrement(1);
        return true;
    }
}
