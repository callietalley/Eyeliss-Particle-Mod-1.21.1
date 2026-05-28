package eyeliss.particle.mod.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class RageParticle extends SpriteBillboardParticle {

    protected final SpriteProvider spriteProvider;
    private final float rotationSpeed;

    private final float baseScale;
    private final float totalShrinkAmount;

    public RageParticle(ClientWorld clientWorld, double x, double y, double z,
                        SpriteProvider spriteProvider, double xSpeed, double ySpeed, double zSpeed) {
        super(clientWorld, x, y, z, xSpeed, ySpeed, zSpeed);

        this.spriteProvider = spriteProvider;
        this.velocityMultiplier = 0.8f;

        this.maxAge = 40;

        // 1. Pick a random initial scale between 1.3 and 1.5
        float minScale = 1.3f;
        float maxScale = 1.5f;
        float randomInitialScale = minScale + (this.random.nextFloat() * (maxScale - minScale));

        // 2. Apply and save the randomized initial scale
        this.scale(randomInitialScale);
        this.baseScale = this.scale;

        this.setSprite(spriteProvider.getSprite(this.random));

        // Randomize redness variation
        float redIntensity = this.random.nextFloat() * 0.3f;
        this.red = 1f;
        this.green = 1f - redIntensity;
        this.blue = 1f - redIntensity;

        // Force the particle to always spawn perfectly upright.
        this.angle = 0.0f;
        this.prevAngle = 0.0f;

        // Convert degrees to radians (15° = 0.262, 33° = 0.576)
        float minRotation = 0.262f;
        float maxRotation = 0.576f;

        // Pick a random total rotation angle between 15° and 33°
        float totalRotation = minRotation + (this.random.nextFloat() * (maxRotation - minRotation));

        // Randomly choose left (negative) or right (positive) rotation
        if (this.random.nextBoolean()) {
            totalRotation *= -1.0f;
        }

        // Divide by maxAge so it reaches that exact total rotation right as it dies
        this.rotationSpeed = totalRotation / (float)this.maxAge;

        // Pick a random total shrink percentage between 10% (0.10f) and 33% (0.33f)
        float minShrink = 0.10f;
        float maxShrink = 0.33f;
        this.totalShrinkAmount = minShrink + (this.random.nextFloat() * (maxShrink - minShrink));
    }

    @Override
    public void tick() {
        this.prevAngle = this.angle;

        super.tick();

        // Apply the rotation step on every tick
        this.angle += this.rotationSpeed;

        // Calculate how much of the particle's lifetime has passed (0.0 to 1.0)
        float lifePercentage = (float) this.age / (float) this.maxAge;

        // Calculate and apply the shrinking factor smoothly over time
        this.scale = this.baseScale * (1.0f - (this.totalShrinkAmount * lifePercentage));

        // Smoothly fade away over time
        this.alpha = 1.0f - lifePercentage;
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
            return new RageParticle(world, x, y, z, this.spriteProvider, velocityX, velocityY, velocityZ);
        }
    }
}