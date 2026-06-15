package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.item.HarmoniousEssenceItem;
import eyeliss.particle.mod.sound.ModSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class HarmoniousItemSoundMixin {

    @Shadow public abstract ItemStack getStack();

    @Inject(method = "tick", at = @At("TAIL"))
    private void playFloorSound(CallbackInfo ci) {
        ItemEntity entity = (ItemEntity) (Object) this;
        World world = entity.getWorld();

        if (world.isClient() && entity.isOnGround()) {
            if (this.getStack().getItem() instanceof HarmoniousEssenceItem) {
                if (entity.age % 100 == 0) {
                    if (entity.getRandom().nextFloat() < 0.50f) {
                        world.playSound(
                                entity.getX(), entity.getY(), entity.getZ(),
                                ModSounds.HARMONIOUS_ESSENCE_EVENT,
                                SoundCategory.AMBIENT,
                                0.6f,
                                0.9f + entity.getRandom().nextFloat() * 0.2f,
                                false
                        );
                    }
                }
            }
        }
    }

    /**
     * Triggers the exact microsecond a player overlaps the item entity to pick it up.
     */
    @Inject(method = "onPlayerCollision", at = @At("HEAD"))
    private void stopFloorSoundOnPickup(PlayerEntity player, CallbackInfo ci) {
        ItemEntity entity = (ItemEntity) (Object) this;
        World world = entity.getWorld();

        // Check if the item hitting the player is the Harmonious Essence on the client side
        if (world.isClient() && this.getStack().getItem() instanceof HarmoniousEssenceItem) {

            // Access Minecraft's active client sound engine
            MinecraftClient client = MinecraftClient.getInstance();

            // Tell the engine to forcefully kill all sound emitters matching your sound event
            // that are currently playing right around this item's specific pickup location!
            client.getSoundManager().stopSounds(
                    ModSounds.HARMONIOUS_ESSENCE_EVENT.getId(),
                    SoundCategory.AMBIENT
            );
        }
    }
}
