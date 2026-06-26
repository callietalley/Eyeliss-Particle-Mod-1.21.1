package eyeliss.particle.mod.item.specialitems;

import eyeliss.particle.mod.component.EngravingContents;
import eyeliss.particle.mod.component.ModComponents;
import eyeliss.particle.mod.component.BloodShrivingCharge;
import eyeliss.particle.mod.registry.ModItemTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import java.util.List;

public class MagicalShrivingStoneItem extends Item {
    public MagicalShrivingStoneItem(Settings settings) { super(settings); }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.LEFT) return false;

        ItemStack targetStack = slot.getStack();
        if (targetStack.isEmpty() || targetStack.contains(ModComponents.SHRIVING_CHARGE)) return false;

        List<TagKey<Item>> activePoolTags = List.of(
                ModItemTags.SWORD_ENGRAVING_POOL,
                ModItemTags.TOOL_ENGRAVING_POOL,
                ModItemTags.ARMOR_ENGRAVING_POOL,
                ModItemTags.GENERAL_ENGRAVING_POOL
        );

        boolean isValidEngravableItem = false;
        for (TagKey<Item> tag : activePoolTags) {
            if (targetStack.isIn(tag)) {
                isValidEngravableItem = true;
                break;
            }
        }

        if (!isValidEngravableItem) return false;

        List<EngravingContents> current = targetStack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());

        boolean hasStagnation = false;
        boolean hasTranscendence = false;

        for (EngravingContents entry : current) {
            if (entry.engravingId().equals("stagnation")) {
                hasStagnation = true;
            } else if (entry.engravingId().equals("transcendence")) {
                hasTranscendence = true;
            }
        }

        if (hasStagnation) {
            return false;
        }

        int engravingCount = current.size();
        int requiredKills = 100 * (int) Math.pow(10, engravingCount);

        if (hasTranscendence) {
            requiredKills = requiredKills / 100;
            requiredKills = Math.max(1, requiredKills);
        }

        targetStack.set(ModComponents.SHRIVING_CHARGE, new BloodShrivingCharge(0, requiredKills));

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.PLAYERS, 0.6f, 1.2f);

        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }
        return true;
    }
}
