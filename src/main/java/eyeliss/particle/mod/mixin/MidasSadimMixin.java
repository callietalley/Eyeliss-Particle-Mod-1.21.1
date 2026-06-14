package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.item.trinkets.MidasGoldItem;
import eyeliss.particle.mod.item.trinkets.SadimsIronItem;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ItemEntity.class)
public abstract class MidasSadimMixin {
    @Shadow private int pickupDelay;

    @Inject(method = "tick", at = @At("HEAD"))
    private void injectTrinketPriorityTick(CallbackInfo ci) {
        // CORRECTION 1: Clean, optimized target casting
        ItemEntity itemEntity = (ItemEntity) (Object) this;

        if (itemEntity.getWorld().isClient()) {
            return;
        }

        Box searchBox = itemEntity.getBoundingBox().expand(6.0, 6.0, 6.0);
        List<PlayerEntity> nearbyPlayers = itemEntity.getWorld().getEntitiesByClass(PlayerEntity.class, searchBox, player -> !player.isSpectator());

        for (PlayerEntity player : nearbyPlayers) {
            AtomicBoolean hasMidasGold = new AtomicBoolean(false);
            AtomicBoolean hasSadimsIron = new AtomicBoolean(false);

            TrinketsApi.getTrinketComponent(player).ifPresent(comp ->
                    comp.forEach((slotRef, stack) -> {
                        boolean isInPocket = slotRef.inventory().getSlotType().getId().endsWith("legs/pocket");
                        if (isInPocket) {
                            if (stack.getItem() instanceof MidasGoldItem) hasMidasGold.set(true);
                            if (stack.getItem() instanceof SadimsIronItem) hasSadimsIron.set(true);
                        }
                    })
            );

            boolean eligibleToPickUp = false;

            if (hasMidasGold.get()) {
                int reductionThreshold = (this.pickupDelay > 0 && this.pickupDelay < 32767) ? (int) (40 * 0.33f) : 0;
                if (this.pickupDelay <= reductionThreshold && itemEntity.getBoundingBox().expand(0.5, 0.5, 0.5).intersects(player.getBoundingBox())) {
                    eligibleToPickUp = true;
                }
            }
            else if (hasSadimsIron.get()) {
                if (this.pickupDelay == 0 && itemEntity.getBoundingBox().expand(2.0, 2.0, 2.0).intersects(player.getBoundingBox())) {
                    eligibleToPickUp = true;
                }
            }

            if (eligibleToPickUp) {
                ItemStack stack = itemEntity.getStack();
                int originalCount = stack.getCount();

                if (player.getInventory().insertStack(stack)) {
                    int pickedUpCount = originalCount - stack.getCount();
                    player.sendPickup(itemEntity, pickedUpCount);

                    if (pickedUpCount > 0 && player.getWorld() instanceof ServerWorld serverWorld) {
                        if (hasMidasGold.get()) {
                            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_ARMOR_EQUIP_GOLD, SoundCategory.NEUTRAL, 1.0F, 1.8F);
                            Vector3f goldColor = new Vector3f(1.0f, 0.84f, 0.0f);
                            serverWorld.spawnParticles(new DustParticleEffect(goldColor, 1.0f), player.getX(), player.getY(), player.getZ(), 15, 0.25, 0.1, 0.25, 0.05);
                        } else {
                            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.NEUTRAL, 0.25F, 2F);
                            Vector3f darkRedColor = new Vector3f(0.54f, 0.0f, 0.0f);
                            serverWorld.spawnParticles(new DustParticleEffect(darkRedColor, 1.0f), player.getX(), player.getY(), player.getZ(), 15, 0.25, 0.1, 0.25, 0.05);
                        }
                    }

                    if (stack.isEmpty()) {
                        itemEntity.discard();
                        break;
                    }
                }
            }
        }
    }
}
