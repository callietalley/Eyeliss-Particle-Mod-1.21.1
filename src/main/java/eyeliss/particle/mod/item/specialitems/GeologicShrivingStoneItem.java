package eyeliss.particle.mod.item.specialitems;

import eyeliss.particle.mod.component.EngravingContents;
import eyeliss.particle.mod.component.ModComponents;
import eyeliss.particle.mod.component.BlockShrivingCharge;
import eyeliss.particle.mod.registry.ModItemTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import java.util.List;

public class GeologicShrivingStoneItem extends Item {
    public GeologicShrivingStoneItem(Settings settings) { super(settings); }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.LEFT) return false;

        ItemStack targetStack = slot.getStack();
        if (targetStack.isEmpty()) return false;

        // CRITICAL RULE ENFORCEMENT: Only ONE upgrading shriving stone can be active at any given time!
        if (targetStack.contains(ModComponents.SHRIVING_CHARGE) || targetStack.contains(ModComponents.BLOCK_CHARGE)) {
            if (!player.getWorld().isClient()) {
                player.sendMessage(Text.literal("This item is already carrying an active Shriving Stone project! Strip it first using a Dense Shriving Stone.").formatted(net.minecraft.util.Formatting.RED), true);
            }
            return false;
        }

        // Validate tags mapping constraints
        List<TagKey<Item>> activePoolTags = List.of(
                ModItemTags.SWORD_ENGRAVING_POOL, ModItemTags.TOOL_ENGRAVING_POOL,
                ModItemTags.ARMOR_ENGRAVING_POOL, ModItemTags.GENERAL_ENGRAVING_POOL
        );

        boolean isValid = false;
        for (TagKey<Item> tag : activePoolTags) {
            if (targetStack.isIn(tag)) { isValid = true; break; }
        }
        if (!isValid) return false;

        List<EngravingContents> current = targetStack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());

        // Block application if Curse of Stagnation is present
        if (current.stream().anyMatch(e -> e.engravingId().equals("stagnation"))) return false;

        boolean hasTranscendence = current.stream().anyMatch(e -> e.engravingId().equals("transcendence"));
        int requiredBlocks = 100 * (int) Math.pow(10, current.size());

        if (hasTranscendence) {
            requiredBlocks = Math.max(1, requiredBlocks / 100);
        }

        targetStack.set(ModComponents.BLOCK_CHARGE, new BlockShrivingCharge(0, requiredBlocks));

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.PLAYERS, 0.6f, 1.0f);

        if (!player.getAbilities().creativeMode) stack.decrement(1);
        return true;
    }
}
