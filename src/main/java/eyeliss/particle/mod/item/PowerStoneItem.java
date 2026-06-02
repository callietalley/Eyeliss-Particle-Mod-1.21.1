package eyeliss.particle.mod.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class PowerStoneItem extends Item {
    public PowerStoneItem(Settings settings) {
        super(settings);
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.BLOCK_AMETHYST_BLOCK_FALL;
    }
}