package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.component.ActiveEngravingEvaluator;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class EngravingEffectEntityMixin {

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float injectCustomWeaponEngravingDamageModifiers(float amount, DamageSource source) {
        if (source.getAttacker() instanceof PlayerEntity player) {
            LivingEntity target = (LivingEntity) (Object) this;

            return ActiveEngravingEvaluator.calculateCustomWeaponDamageModifiers(player, target, source, amount);
        }
        return amount;
    }

    @ModifyVariable(method = "applyArmorToDamage", at = @At("HEAD"), argsOnly = true)
    private float injectCrushingArmorIgnore(float damageAmount, DamageSource source) {
        LivingEntity target = (LivingEntity) (Object) this;

        float targetArmor = target.getArmor();

        float modifiedArmor = ActiveEngravingEvaluator.modifyIncomingArmorProtection(target, source, targetArmor);

        if (modifiedArmor < targetArmor) {
            float toughness = (float) target.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);

            return DamageUtil.getDamageLeft(target, damageAmount, source, modifiedArmor, toughness);
        }

        return damageAmount;
    }
}