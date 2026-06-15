package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxRitualMixin {

    @Shadow public abstract ItemStack getStack();
    @Shadow public abstract void setStack(ItemStack stack);

    @Unique
    private int harmoniusRitualTimer = 0;

    /**
     * 💿 FIXED RESET HOOK: Triggers instantly whenever an inventory modification occurs.
     * This forces an immediate wipeout of the timer progress when a disc is either
     * taken out or pushed in, completely avoiding stale cached integer values!
     */
    @Inject(method = "setStack", at = @At("HEAD"))
    private void onInventoryChanged(ItemStack stack, CallbackInfo ci) {
        // Reset the ritual progress back to absolute zero on the spot
        this.harmoniusRitualTimer = 0;
    }

    /**
     * Core server ritual processing engine
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private static void tickRitualLogic(World world, BlockPos pos, BlockState state, JukeboxBlockEntity blockEntity, CallbackInfo ci) {
        // Strict safety guard: check both world and the blockEntity itself
        if (world == null || world.isClient() || blockEntity == null) {
            return;
        }

        JukeboxRitualMixin instance = (JukeboxRitualMixin) (Object) blockEntity;

        // 💿 FIXED: Call the static check, passing the blockEntity directly
        if (isMusicDiscPresent(blockEntity)) {
            Box searchArea = new Box(pos).expand(5.0);
            List<ParrotEntity> parrots = world.getEntitiesByClass(ParrotEntity.class, searchArea, parrot -> true);
            List<AllayEntity> allays = world.getEntitiesByClass(AllayEntity.class, searchArea, allay -> true);

            if ((parrots.size() + allays.size()) >= 4) {
                instance.harmoniusRitualTimer++;

                if (instance.harmoniusRitualTimer >= 600) {
                    instance.harmoniusRitualTimer = 0;
                    instance.spawnHarmoniusEssence(world, pos);
                    instance.setStack(ItemStack.EMPTY);

                    blockEntity.markDirty();
                    world.updateListeners(pos, state, state, 3);
                }
            } else {
                if (instance.harmoniusRitualTimer > 0) {
                    instance.harmoniusRitualTimer--;
                }
            }
        } else {
            instance.harmoniusRitualTimer = 0;
        }
    }

    // 💿 FIXED: Changed to static and added an explicit check against the blockEntity object
    @Unique
    private static boolean isMusicDiscPresent(JukeboxBlockEntity blockEntity) {
        if (blockEntity == null) return false;

        ItemStack disc = blockEntity.getStack();
        return disc != null && !disc.isEmpty() && disc.getComponents() != null && disc.getComponents().contains(DataComponentTypes.JUKEBOX_PLAYABLE);
    }

    @Unique
    private void spawnHarmoniusEssence(World world, BlockPos pos) {
        if (ModItems.HARMONIOUS_ESSENCE != null) {
            ItemStack reward = new ItemStack(ModItems.HARMONIOUS_ESSENCE, 1);

            double spawnX = pos.getX() + 0.5;
            double spawnY = pos.getY() + 1.2;
            double spawnZ = pos.getZ() + 0.5;

            ItemEntity rewardEntity = new ItemEntity(world, spawnX, spawnY, spawnZ, reward);
            rewardEntity.setVelocity(0.0, 0.2, 0.0);
            world.spawnEntity(rewardEntity);
        }
    }
}
