package eyeliss.particle.mod.item.trinkets;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;

import java.util.List;

public class BloodStoneItem extends TrinketItem {

    public BloodStoneItem(Settings settings) {
        super(settings);
    }

    @Override
    public Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(
            ItemStack stack, SlotReference slot, LivingEntity entity, Identifier id) {
        var modifiers = super.getModifiers(stack, slot, entity, id);

        modifiers.put(EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier(
                Identifier.of("eyelisspartmod", "bloodstone_health"),
                10.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        ));
        return modifiers;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Grants +5 hearts")
                .formatted(Formatting.GRAY));
        tooltip.add(Text.literal("If you go below 20% max hp, you steal health")
                .formatted(Formatting.GRAY));
        tooltip.add(Text.literal("from nearby enemies, with a 200 second cooldown")
                .formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Killing most mobs gives regeneration 2 for 4 seconds")
                .formatted(Formatting.GRAY));
        tooltip.add(Text.literal(" ")
                .formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
