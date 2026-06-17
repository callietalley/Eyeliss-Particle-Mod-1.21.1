package eyeliss.particle.mod.item.specialweapons;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.SpellDataComponents;
import net.spell_engine.api.spell.container.SpellChoice;
import net.spell_engine.api.spell.container.SpellContainers;

import java.util.List;

public class KhopeshItem extends Item {
    private final ToolMaterial material;

    public KhopeshItem(ToolMaterial toolMaterial, Settings settings) {
        super(settings.maxDamage(toolMaterial.getDurability())
                .component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createKhopeshAttributes(toolMaterial))
                .component(
                        SpellDataComponents.SPELL_CONTAINER,
                        SpellContainers.forMagicWeapon().withSpell("eyelisspartmod:melee_weakness_effect")
                )
                .component(
                        SpellDataComponents.SPELL_CHOICE,
                        new SpellChoice("eyelisspartmod:weapon/khopesh_choice")
                )
                .component(
                        DataComponentTypes.LORE,
                        new LoreComponent(List.of(
                                Text.literal("The further you are from an enemy")
                                        .setStyle(Text.literal("").getStyle().withItalic(false).withExclusiveFormatting(Formatting.GRAY)),

                                Text.literal("the more damage you deal with this")
                                        .setStyle(Text.literal("").getStyle().withItalic(false).withExclusiveFormatting(Formatting.GRAY))
                        ))
                )
        );
        this.material = toolMaterial;
    }

    @Override
    public int getEnchantability() {
        return this.material.getEnchantability();
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, LivingEntity.getSlotForHand(attacker.getActiveHand()));
        return true;
    }

    private static AttributeModifiersComponent createKhopeshAttributes(ToolMaterial material) {
        double finalDamage = 6.0 + material.getAttackDamage();
        double attackSpeedValue = -2.5;

        return AttributeModifiersComponent.builder()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID, finalDamage, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .add(EntityAttributes.GENERIC_ATTACK_SPEED,
                        new EntityAttributeModifier(Item.BASE_ATTACK_SPEED_MODIFIER_ID, attackSpeedValue, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .add(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE,
                        new EntityAttributeModifier(Identifier.of("item.attack_range"), 0.75, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }
}