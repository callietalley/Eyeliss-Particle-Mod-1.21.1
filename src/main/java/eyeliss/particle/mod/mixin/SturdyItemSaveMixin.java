package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.component.EngravingContents;
import eyeliss.particle.mod.component.ModComponents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(ItemEntity.class)
public abstract class SturdyItemSaveMixin {
    @Shadow public abstract ItemStack getStack();

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void injectSturdyLevelThreeImmunity(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = this.getStack();
        if (stack == null || stack.isEmpty()) return;

        List<EngravingContents> engravings = stack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());
        for (EngravingContents entry : engravings) {
            if (entry.engravingId().equals("sturdy") && entry.level() >= 3) {
                cir.setReturnValue(false);
                return;
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void injectVoidRescueTeleportation(CallbackInfo ci) {
        ItemEntity entity = (ItemEntity)(Object)this;

        if (entity.getWorld().isClient() || entity.getY() > entity.getWorld().getBottomY() - 5) return;

        ItemStack stack = this.getStack();
        if (stack == null || stack.isEmpty()) return;

        List<EngravingContents> engravings = stack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());
        for (EngravingContents entry : engravings) {
            if (entry.engravingId().equals("sturdy") && entry.level() >= 3) {

                if (entity.getWorld() instanceof ServerWorld serverWorld) {
                    BlockPos fallbackPos = new BlockPos((int)entity.getX(), entity.getWorld().getBottomY() + 5, (int)entity.getZ());

                    entity.refreshPositionAndAngles(
                            fallbackPos.getX() + 0.5,
                            fallbackPos.getY() + 1.0,
                            fallbackPos.getZ() + 0.5,
                            entity.getYaw(),
                            entity.getPitch()
                    );

                    entity.setVelocity(0, 0, 0);
                    entity.setGlowing(true);

                    serverWorld.playSound(
                            null,
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM,
                            SoundCategory.NEUTRAL,
                            4.0f,
                            1.0f
                    );

                    serverWorld.spawnParticles(
                            ParticleTypes.CLOUD,
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            25,
                            0.2,
                            0.2,
                            0.2,
                            0.1
                    );
                }
                break;
            }
        }
    }
}
