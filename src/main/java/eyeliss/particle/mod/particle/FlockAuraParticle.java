package eyeliss.particle.mod.particle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class FlockAuraParticle extends SpriteBillboardParticle {

    private Entity targetEntity;
    private final double spawnX;
    private final double spawnY;
    private final double spawnZ;

    public FlockAuraParticle(ClientWorld clientWorld, double x, double y, double z,
                             SpriteProvider spriteProvider, Entity targetEntity, double velocityX) {
        super(clientWorld, x, y, z, velocityX, 0.0, 0.0);

        this.targetEntity = targetEntity;
        this.spawnX = x;
        this.spawnY = y;
        this.spawnZ = z;

        this.maxAge = 80;
        this.setSprite(spriteProvider.getSprite(this.random));
        this.updateAuraPosition();
    }

    @Override
    public void tick() {
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        this.updateAuraPosition();

        int fadeStartTick = this.maxAge - 10; // Trigger final fade at tick 70

        if (this.age < fadeStartTick) {
            // Normal fluctuation phase: smoothly wave back and forth between 0.2f and 0.5f opacity
            double alphaOscillation = Math.sin((double) this.age * 0.15);
            this.alpha = (float) (0.35 + (alphaOscillation * 0.15));
        } else {
            // Fade out phase: calculate the exact alpha value it held at the moment tick 70 struck
            double alphaAtFadeStart = 0.35 + (Math.sin((double) fadeStartTick * 0.15) * 0.15);

            // Calculate progress through the final half-second window (0.0 at tick 70, 1.0 at tick 80)
            float fadeProgress = (float) (this.age - fadeStartTick) / 10.0f;

            // Linearly drop to zero from its active starting opacity position
            this.alpha = (float) (alphaAtFadeStart * (1.0f - fadeProgress));
        }
    }

    /**
     * Forces Minecraft to scale this particle up.
     * Reduced by exactly one-third from its previous 2.2f layout down to 1.466f.
     */
    @Override
    public float getSize(float tickDelta) {
        return 1.8f;
    }

    private void updateAuraPosition() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.targetEntity == null) {
            this.targetEntity = MinecraftClient.getInstance().player;
        }

        // Anchors cleanly to the dead-center point of the tracking player
        double targetX = (this.targetEntity != null) ? this.targetEntity.getX() : this.spawnX;
        double targetY = (this.targetEntity != null) ? this.targetEntity.getY() + 1.0 : this.spawnY;
        double targetZ = (this.targetEntity != null) ? this.targetEntity.getZ() : this.spawnZ;

        this.setPos(targetX, targetY, targetZ);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            int targetId = (int) Math.round(velocityX);
            Entity foundEntity = world.getEntityById(targetId);

            if (foundEntity == null) {
                for (AbstractClientPlayerEntity player : world.getPlayers()) {
                    if (player.squaredDistanceTo(x, y, z) < 16.0) {
                        foundEntity = player;
                        break;
                    }
                }
            }

            return new FlockAuraParticle(world, x, y, z, this.spriteProvider, foundEntity, velocityX);
        }
    }
}