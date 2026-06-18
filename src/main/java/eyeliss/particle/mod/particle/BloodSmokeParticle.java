package eyeliss.particle.mod.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class BloodSmokeParticle extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;

    public BloodSmokeParticle(ClientWorld clientWorld, double x, double y, double z,
                              double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(clientWorld, x, y, z);
        this.spriteProvider = spriteProvider;

        this.velocityX = velocityX * 0.1 + (this.random.nextFloat() - 0.5f) * 0.05;
        this.velocityY = velocityY * 0.1 + this.random.nextFloat() * 0.08;
        this.velocityZ = velocityZ * 0.1 + (this.random.nextFloat() - 0.5f) * 0.05;

        // LOCK THE COLOR TO CRIMSON NATIVELY 🩸
        this.red = 0.85f + (this.random.nextFloat() * 0.15f);
        this.green = 0.85f;
        this.blue = 0.85f;

        // Particle layout properties
        this.maxAge = 25 + this.random.nextInt(15);
        this.scale = 0.4f + this.random.nextFloat() * 0.3f;
        this.alpha = 0.9f;

        this.collidesWithWorld = false;

        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        this.setSpriteForAge(this.spriteProvider);

        this.velocityX *= 0.96;
        this.velocityY *= 0.96;
        this.velocityZ *= 0.96;

        this.move(this.velocityX, this.velocityY, this.velocityZ);

        if (this.age > this.maxAge * 0.5) {
            this.alpha = 1.0f - ((float) this.age / (float) this.maxAge);
        }

        this.scale += 0.02f;
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
        public @Nullable Particle createParticle(SimpleParticleType parameters, ClientWorld world,
                                                 double x, double y, double z,
                                                 double velocityX, double velocityY, double velocityZ) {
            return new BloodSmokeParticle(world, x, y, z, velocityX, velocityY, velocityZ, this.spriteProvider);
        }
    }
}
