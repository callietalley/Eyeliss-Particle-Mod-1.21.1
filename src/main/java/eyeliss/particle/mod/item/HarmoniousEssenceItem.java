package eyeliss.particle.mod.item;

import eyeliss.particle.mod.util.ClientSoundTracker; // 👈 We will create this in Part 2
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class HarmoniousEssenceItem extends Item {

    public HarmoniousEssenceItem(Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient() && entity instanceof PlayerEntity player) {

            boolean isHeld = player.getMainHandStack() == stack || player.getOffHandStack() == stack;

            if (isHeld) {
                if (world.getTime() % 60 == 0) {

                    if (!ClientSoundTracker.isSoundPlaying()) {

                        if (player.getRandom().nextFloat() < 0.50f) {
                            ClientSoundTracker.playHarmoniousSound(player);
                        }
                    }
                }
            } else {
                ClientSoundTracker.stopHarmoniousSound();
            }
        }
        super.inventoryTick(stack, world, entity, slot, selected);


    }
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List< Text > tooltip, net.minecraft.item.tooltip.TooltipType type) {
        tooltip.add(Text.literal("Obtained by playing a music disc near").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("four parrots and/or allays.").formatted(Formatting.GRAY));

        super.appendTooltip(stack, context, tooltip, type);
    }
}
