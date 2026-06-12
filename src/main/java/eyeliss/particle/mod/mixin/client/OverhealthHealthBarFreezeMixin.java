package eyeliss.particle.mod.mixin.client;

import eyeliss.particle.mod.effect.ModEffects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class OverhealthHealthBarFreezeMixin {

    @Unique private int originalHurtTime;
    @Unique private float originalHealth;

    @ModifyVariable(method = "renderHealthBar", at = @At("HEAD"), ordinal = 3, argsOnly = true)
    private int freezeRegeneratingHeartIndex(int value, DrawContext context, PlayerEntity player) {
        if (player != null) {
            RegistryEntry<StatusEffect> overhealthEntry = Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH);
            if (player.hasStatusEffect(overhealthEntry)) {
                return -1; // -1 ensures no heart matches the regeneration bobbing index loop
            }
        }
        return value;
    }

    @Inject(method = "renderHealthBar", at = @At("HEAD"))
    private void lockHealthStates(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (player != null) {
            RegistryEntry<StatusEffect> overhealthEntry = Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH);

            if (player.hasStatusEffect(overhealthEntry)) {
                this.originalHurtTime = player.hurtTime;
                this.originalHealth = player.getHealth();

                player.hurtTime = 0;                     // Stops red heart shake and damage flash
                player.setHealth(player.getMaxHealth()); // Stops low health wobbling
            }
        }
    }

    @Inject(method = "renderHealthBar", at = @At("TAIL"))
    private void restoreHealthStates(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (player != null) {
            RegistryEntry<StatusEffect> overhealthEntry = Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH);

            if (player.hasStatusEffect(overhealthEntry)) {
                player.hurtTime = this.originalHurtTime;
                player.setHealth(this.originalHealth);
            }
        }
    }
}