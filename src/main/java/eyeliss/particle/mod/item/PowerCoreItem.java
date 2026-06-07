package eyeliss.particle.mod.item;

import eyeliss.particle.mod.entity.ModEntities;
import eyeliss.particle.mod.entity.UmberwitherEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class PowerCoreItem extends Item {
    public PowerCoreItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof WitherEntity && !(entity.getType() == ModEntities.UMBERWITHER)) {
            if (!user.getWorld().isClient()) {
                ServerWorld world = (ServerWorld) user.getWorld();

                UmberwitherEntity umberwither = ModEntities.UMBERWITHER.create(world);
                if (umberwither != null) {
                    umberwither.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
                    umberwither.setVelocity(entity.getVelocity());

                    world.spawnEntity(umberwither);

                    world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.HOSTILE, 1.5F, 0.5F);

                    entity.discard();

                    if (!user.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }
                }
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}