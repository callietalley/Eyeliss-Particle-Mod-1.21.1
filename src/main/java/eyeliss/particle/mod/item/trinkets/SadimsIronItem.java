package eyeliss.particle.mod.item.trinkets;

import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem; // FIXED: Extends TrinketItem natively
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
import java.util.List;

public class SadimsIronItem extends TrinketItem {

    public SadimsIronItem(Settings settings) {
        super(settings);
    }

    @Override
    public Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, Identifier slotIdentifier) {
        var modifiers = super.getModifiers(stack, slot, entity, slotIdentifier);

        if (slot.inventory().getSlotType().getId().endsWith("legs/pocket")) {

            modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(
                    slotIdentifier.withPrefixedPath("sadim_speed"),
                    -0.10,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));

            modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(
                    slotIdentifier.withPrefixedPath("sadim_damage"),
                    0.15,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }

        return modifiers;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Heavy, slows you to equip").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Increased melee damage").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Increased pickup range").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(" ").formatted(Formatting.GRAY));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
