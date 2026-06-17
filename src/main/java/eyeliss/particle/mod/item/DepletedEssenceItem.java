package eyeliss.particle.mod.item;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class DepletedEssenceItem extends Item {

    public static final TagKey<Item> COMMON_POOL = TagKey.of(RegistryKeys.ITEM, Identifier.of(EyelisssParticleMod.MOD_ID, "pools/depleted_pool_common"));
    public static final TagKey<Item> RARE_POOL = TagKey.of(RegistryKeys.ITEM, Identifier.of(EyelisssParticleMod.MOD_ID, "pools/depleted_pool_rare"));

    public DepletedEssenceItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack heldStack = user.getStackInHand(hand);

        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            TagKey<Item> chosenPoolTag = (world.getRandom().nextFloat() < 0.90f) ? COMMON_POOL : RARE_POOL;

            // Gather the list of elements sitting inside the selected tag collection
            var entriesStream = Registries.ITEM.iterateEntries(chosenPoolTag);
            var entryList = new java.util.ArrayList<net.minecraft.registry.entry.RegistryEntry<Item>>();
            entriesStream.forEach(entryList::add);

            // ================== FAILSAFE INTERCEPTOR ==================
            // If the pool has 0 items (missing JSON or empty list arrays),
            // HALT execution instantly without consuming or changing anything.
            if (entryList.isEmpty()) {
                EyelisssParticleMod.LOGGER.warn("Failsafe triggered: Depleted Essence tag pool was empty or missing. Item consumption cancelled.");
                return TypedActionResult.pass(heldStack);
            }
            // ==========================================================

            int randomIndex = world.getRandom().nextInt(entryList.size());
            var selectedItemEntry = entryList.get(randomIndex);
            Item droppedItem = selectedItemEntry.value();

            // Construct and hand out reward stack safely
            ItemStack rewardStack = new ItemStack(droppedItem, 1);
            if (!user.getInventory().insertStack(rewardStack)) {
                user.dropItem(rewardStack, false);
            }

            // Play transformation audio FX
            world.playSound(
                    null,
                    user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_PLAYER_LEVELUP,
                    SoundCategory.PLAYERS,
                    0.6F,
                    1.4F
            );

            // Directional dust particle cloud
            Random rand = serverWorld.getRandom();
            Vector3f targetDarkGray = new Vector3f(0.15F, 0.15F, 0.15F);
            Vec3d lookDir = user.getRotationVector();

            for (int i = 0; i < 30; i++) {
                float r = rand.nextFloat();
                float g = rand.nextFloat();
                float b = rand.nextFloat();
                Vector3f startRandomColor = new Vector3f(r, g, b);

                DustColorTransitionParticleEffect colorShiftEffect =
                        new DustColorTransitionParticleEffect(startRandomColor, targetDarkGray, 1.2F);

                double offsetX = user.getX() + (lookDir.x * 0.3) + (rand.nextDouble() - 0.5) * 0.4;
                double offsetY = user.getEyeY() - 0.4 + (lookDir.y * 0.3) + (rand.nextDouble() - 0.5) * 0.4;
                double offsetZ = user.getZ() + (lookDir.z * 0.3) + (rand.nextDouble() - 0.5) * 0.4;

                double velX = ((rand.nextDouble() - 0.5) * 0.1) + (lookDir.x * 0.25);
                double velY = ((rand.nextDouble() - 0.5) * 0.1) + (lookDir.y * 0.25) + 0.05;
                double velZ = ((rand.nextDouble() - 0.5) * 0.1) + (lookDir.z * 0.25);

                serverWorld.spawnParticles(
                        colorShiftEffect,
                        offsetX, offsetY, offsetZ,
                        1,
                        velX, velY, velZ,
                        0.0
                );
            }

            // Decrement item stack directly after verifying successful drop generation
            heldStack.decrement(1);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.consume(heldStack);
        }

        return TypedActionResult.success(heldStack, world.isClient());
    }
}
