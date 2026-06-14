package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.item.trinkets.MidasGoldItem;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(LivingEntity.class)
public class MidasMobLootMixin {

    @Inject(method = "dropLoot", at = @At("TAIL"))
    private void injectMidasGoldBounty(DamageSource damageSource, boolean causedByPlayer, CallbackInfo ci) {
        LivingEntity deadMob = (LivingEntity) (Object) this;

        if (deadMob.getWorld().isClient() || !causedByPlayer || !(damageSource.getAttacker() instanceof PlayerEntity player)) {
            return;
        }

        AtomicBoolean isEquippedInPocketSlot = new AtomicBoolean(false);

        TrinketsApi.getTrinketComponent(player).ifPresent(comp ->
                comp.forEach((slotRef, stack) -> {
                    boolean isMidasGold = stack.getItem() instanceof MidasGoldItem;
                    boolean isInPocket = slotRef.inventory().getSlotType().getId().endsWith("legs/pocket");
                    if (isMidasGold && isInPocket) {
                        isEquippedInPocketSlot.set(true);
                    }
                })
        );

        if (isEquippedInPocketSlot.get()) {
            if (player.getRandom().nextFloat() <= 0.25f) {

                int nuggetCount = player.getRandom().nextBetween(3, 6);

                ItemStack goldNuggets = new ItemStack(Items.GOLD_NUGGET, nuggetCount);

                ItemEntity itemEntity = new ItemEntity(
                        deadMob.getWorld(),
                        deadMob.getX(), deadMob.getY(), deadMob.getZ(),
                        goldNuggets
                );

                deadMob.getWorld().spawnEntity(itemEntity);
            }
        }
    }
}
