package eyeliss.particle.mod.mixin.client;

import eyeliss.particle.mod.fluid.ModFluids.ModDamageTypes;
import eyeliss.particle.mod.fluid.SauceDamageTracker; // CHANGED IMPORT
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements SauceDamageTracker {
    @Unique
    private static long lastSauceTick = 0;

    @Inject(method = "damage", at = @At("HEAD"))
    private void captureIncomingDamageTypeClient(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getTypeRegistryEntry().matchesKey(ModDamageTypes.SOURCE_SAUCE_DAMAGE)) {
            lastSauceTick = System.currentTimeMillis();
        }
    }

    @Override
    public boolean eyelisssParticleMod$wasRecentlyDamagedBySauce() {
        return (System.currentTimeMillis() - lastSauceTick) < 500;
    }
}
