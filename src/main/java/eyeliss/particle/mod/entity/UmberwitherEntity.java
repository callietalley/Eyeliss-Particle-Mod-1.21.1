package eyeliss.particle.mod.entity;

import eyeliss.particle.mod.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;
import org.joml.Vector3f;
import java.util.Random;

public class UmberwitherEntity extends WitherEntity {
    private static final Random RANDOM = new Random();

    private int spawnTimerTicks = 160;
    private boolean hasExploded = false;

    private static final DustParticleEffect RED_DUST = new DustParticleEffect(new Vector3f(1.0F, 0.0F, 0.0F), 2.0F);

    private static final Item[] POWER_STONES = {
            ModItems.RED_POWER_STONE,
            ModItems.BLUE_POWER_STONE,
            ModItems.GREEN_POWER_STONE,
            ModItems.YELLOW_POWER_STONE,
            ModItems.PURPLE_POWER_STONE
    };

    public UmberwitherEntity(EntityType<? extends WitherEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings("unused")
    public static DefaultAttributeContainer.Builder createUmberwitherAttributes() {
        return WitherEntity.createWitherAttributes()
                .add(EntityAttributes.GENERIC_SCALE, 1.5D)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 600.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 12.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public boolean isAiDisabled() {
        if (this.spawnTimerTicks > 0) {
            return true;
        }
        return super.isAiDisabled();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.spawnTimerTicks > 0) {
            this.spawnTimerTicks--;

            this.setVelocity(0.0D, 0.0D, 0.0D);
            this.setNoGravity(true);

            if (this.spawnTimerTicks == 0 && !this.getWorld().isClient() && !this.hasExploded) {
                this.hasExploded = true;
                this.setNoGravity(false);

                this.getWorld().createExplosion(this, this.getX(), this.getEyeY(), this.getZ(), 7.0F, true, ExplosionSourceType.MOB);
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (this.getWorld().isClient()) {
            if (this.random.nextFloat() < 0.15F) {
                for (int i = 0; i < 3; i++) {
                    double xOffset = this.getX() + (this.random.nextDouble() - 0.5D) * this.getWidth() * 2.0D;
                    double zOffset = this.getZ() + (this.random.nextDouble() - 0.5D) * this.getWidth() * 2.0D;
                    double yOffset = this.getY() + this.random.nextDouble() * this.getHeight();

                    this.getWorld().addParticle(ParticleTypes.WITCH, xOffset, yOffset, zOffset, 0.0D, 0.0D, 0.0D);
                }
            }

            if (this.spawnTimerTicks > 0) {
                if (this.random.nextFloat() < 0.4f) {
                    this.getWorld().addParticle(ParticleTypes.LARGE_SMOKE, this.getX() + (this.random.nextDouble() - 0.5D) * this.getWidth(), this.getY() + this.random.nextDouble() * this.getHeight(), this.getZ() + (this.random.nextDouble() - 0.5D) * this.getWidth(), 0.0D, 0.0D, 0.0D);
                }

                if (this.spawnTimerTicks % 5 == 0) {
                    for (int i = 0; i < 10; i++) { // Spawns 10 dust dots simultaneously per wave
                        double xOffset = this.getX() + (this.random.nextDouble() - 0.5D) * this.getWidth() * 2.0D;
                        double zOffset = this.getZ() + (this.random.nextDouble() - 0.5D) * this.getWidth() * 2.0D;
                        double yOffset = this.getY() + this.random.nextDouble() * this.getHeight();

                        this.getWorld().addParticle(RED_DUST, xOffset, yOffset, zOffset, 0.0D, 0.0D, 0.0D);
                    }
                }
            }
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (this.spawnTimerTicks > 0) {
            return true;
        }
        return super.isInvulnerableTo(damageSource);
    }

    @Override
    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
        Item chosenStone = POWER_STONES[RANDOM.nextInt(POWER_STONES.length)];
        ItemEntity itemEntity = this.dropItem(chosenStone);

        if (itemEntity != null) {
            itemEntity.setNeverDespawn();
        }
    }
}