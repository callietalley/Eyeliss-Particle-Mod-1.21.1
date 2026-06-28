package eyeliss.particle.mod.mixin;

import dev.emi.trinkets.api.TrinketsApi;
import eyeliss.particle.mod.item.trinkets.NamespaceWarperItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class NamespaceItemDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"))
    private void damageTrinketOnPlayerHit(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!(entity instanceof ServerPlayerEntity player) || player.isCreative()) return;

        boolean isAttackedByPlayer = source.getAttacker() instanceof PlayerEntity;

        if (isAttackedByPlayer) {
            TrinketsApi.getTrinketComponent(player).ifPresent(comp -> {
                for (var equip : comp.getAllEquipped()) {
                    ItemStack stack = equip.getRight();

                    if (stack.getItem() instanceof NamespaceWarperItem) {
                        stack.damage(1, player, ServerPlayerEntity.getSlotForHand(player.getActiveHand()));
                        break;
                    }
                }
            });
        }
    }
}
