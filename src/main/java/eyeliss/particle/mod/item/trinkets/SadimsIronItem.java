package eyeliss.particle.mod.item.trinkets;

import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class SadimsIronItem extends TrinketItem {

    public SadimsIronItem(Settings settings) {
        super(settings);
    }

    @Override
    public Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, Identifier slotIdentifier) {
        Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifiers = com.google.common.collect.HashMultimap.create(super.getModifiers(stack, slot, entity, slotIdentifier));

        if (slot.inventory().getSlotType().getId().endsWith("legs/pocket")) {
            Identifier speedId = Identifier.of(eyeliss.particle.mod.EyelisssParticleMod.MOD_ID, "trinket_sadim_speed");
            Identifier damageId = Identifier.of(eyeliss.particle.mod.EyelisssParticleMod.MOD_ID, "trinket_sadim_damage");

            modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(
                    speedId,
                    -0.10,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));

            modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(
                    damageId,
                    0.15,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }

        return modifiers;
    }
}
