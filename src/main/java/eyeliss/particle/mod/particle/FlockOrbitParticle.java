package eyeliss.particle.mod.particle;

import eyeliss.particle.mod.item.ModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class FlockOrbitParticle extends Particle {

    protected final SpriteProvider spriteProvider;
    private final float rotationSpeed;

    private float scale;
    private final float baseScale;
    private final float totalShrinkAmount;

    // --- Orbit & Tracking Variables ---
    private Entity targetEntity;
    private final double spawnX;
    private final double spawnY;
    private final double spawnZ;
    private final double anchorOffsetX;
    private final double anchorOffsetY;
    private final double anchorOffsetZ;
    private final double particleOrbitRadius;
    private final double particleOrbitSpeed;
    private double orbitAngle;

    // --- Outward Flyaway & Expansion Variables ---
    private double currentOrbitExpansion = 0.0;
    private double flyawayDistance = 0.0;
    // -----------------------------------

    public FlockOrbitParticle(ClientWorld clientWorld, double x, double y, double z,
                              SpriteProvider spriteProvider, Entity targetEntity, double velocityX) {
        super(clientWorld, x, y, z);

        this.spriteProvider = spriteProvider;
        this.targetEntity = targetEntity;

        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.velocityZ = 0.0;

        this.spawnX = x;
        this.spawnY = y;
        this.spawnZ = z;

        double horizontalSpread = 1.0 / 3.0;
        this.anchorOffsetX = (this.random.nextDouble() * 2.0 - 1.0) * horizontalSpread;
        this.anchorOffsetZ = (this.random.nextDouble() * 2.0 - 1.0) * horizontalSpread;

        double maxVerticalOffset = 1.1;
        this.anchorOffsetY = (this.random.nextDouble() * 2.0 - 1.0) * maxVerticalOffset;

        double minBaseRadius = 0.8;
        double maxBaseRadius = 1.2;
        double baseOrbitRadius = minBaseRadius + (this.random.nextDouble() * (maxBaseRadius - minBaseRadius));

        double radiusModifier = Math.sqrt(1.0 - Math.pow(this.anchorOffsetY / maxVerticalOffset, 2));
        this.particleOrbitRadius = baseOrbitRadius * Math.max(0.5, radiusModifier);

        double baseOrbitSpeed = 0.18;
        double maxSpeedIncrease = 0.33;
        this.particleOrbitSpeed = baseOrbitSpeed * (1.0 + (this.random.nextDouble() * maxSpeedIncrease));

        this.orbitAngle = this.random.nextDouble() * Math.PI * 2.0;

        this.maxAge = 80;

        // --- Scale Configurations ---
        float minScale = 0.25f;
        float maxScale = 0.5f;
        this.baseScale = minScale + (this.random.nextFloat() * (maxScale - minScale));
        this.scale = 0.0f;

        float redIntensity = this.random.nextFloat() * 0.3f;
        this.red = 1f;
        this.green = 1f - redIntensity;
        this.blue = 1f - redIntensity;

        float minRotation = 0.262f;
        float maxRotation = 0.576f;
        float totalRotation = minRotation + (this.random.nextFloat() * (maxRotation - minRotation));

        if (this.random.nextBoolean()) {
            totalRotation *= -1.0f;
        }

        float speedMultiplier = 0.8f + (this.random.nextFloat() * 0.4f);
        this.rotationSpeed = (totalRotation / (float)this.maxAge) * speedMultiplier;

        float minShrink = 0.10f;
        float maxShrink = 0.33f;
        this.totalShrinkAmount = minShrink + (this.random.nextFloat() * (maxShrink - minShrink));

        this.updateOrbitPosition();
    }

    @Override
    public void tick() {
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        int scaleInEndTick = 10;
        int flyawayStartTick = this.maxAge - 10;

        if (this.age < scaleInEndTick) {
            this.orbitAngle += this.particleOrbitSpeed;
            double expansionRate = 0.005;
            this.currentOrbitExpansion += expansionRate;

            float scaleInProgress = (float) this.age / (float) scaleInEndTick;
            float lifePercentage = (float) this.age / (float) this.maxAge;
            float intendedScale = this.baseScale * (1.0f - (this.totalShrinkAmount * lifePercentage));
            this.scale = intendedScale * scaleInProgress;

        } else if (this.age < flyawayStartTick) {
            this.orbitAngle += this.particleOrbitSpeed;
            double expansionRate = 0.005;
            this.currentOrbitExpansion += expansionRate;

            float lifePercentage = (float) this.age / (float) this.maxAge;
            this.scale = this.baseScale * (1.0f - (this.totalShrinkAmount * lifePercentage));

        } else {
            double flyawayVelocity = 0.25;
            this.flyawayDistance += flyawayVelocity;

            float flyawayProgress = (float) (this.age - flyawayStartTick) / 10.0f;
            float scaleAtFlyawayStart = this.baseScale * (1.0f - (this.totalShrinkAmount * ((float) flyawayStartTick / (float) this.maxAge)));
            this.scale = scaleAtFlyawayStart * (1.0f - flyawayProgress);
        }

        this.updateOrbitPosition();

        float lifePercentage = (float) this.age / (float) this.maxAge;
        this.alpha = Math.max(0.5f, 1.0f - (lifePercentage * lifePercentage));
    }

    private void updateOrbitPosition() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.targetEntity == null) {
            this.targetEntity = MinecraftClient.getInstance().player;
        }

        double baseCenterX = (this.targetEntity != null) ? this.targetEntity.getX() : this.spawnX;
        double baseCenterY = (this.targetEntity != null) ? this.targetEntity.getY() + 1.3 : this.spawnY;
        double baseCenterZ = (this.targetEntity != null) ? this.targetEntity.getZ() : this.spawnZ;

        double currentRadius = this.particleOrbitRadius + this.currentOrbitExpansion + this.flyawayDistance;

        double targetX = baseCenterX + this.anchorOffsetX + (currentRadius * Math.cos(this.orbitAngle));
        double targetY = baseCenterY + this.anchorOffsetY;
        double targetZ = baseCenterZ + this.anchorOffsetZ + (currentRadius * Math.sin(this.orbitAngle));

        this.setPos(targetX, targetY, targetZ);
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        // 1. Calculate camera relative coordinates
        Vec3d cameraPos = camera.getPos();
        float renderX = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - cameraPos.getX());
        float renderY = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - cameraPos.getY());
        float renderZ = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - cameraPos.getZ());

        // 2. Prepare Minecraft's 3D matrix stack for rendering
        net.minecraft.client.util.math.MatrixStack matrices = new net.minecraft.client.util.math.MatrixStack();
        matrices.push();

        // Translate model to its current space position
        matrices.translate(renderX, renderY, renderZ);

        // 3. Handle Orbit & Spin Transformations via Quaternions
        Quaternionf finalRotation = new Quaternionf();

        // Base orbit alignment facing forward along the path
        finalRotation.rotationY((float) (-this.orbitAngle - (Math.PI / 2.0)));

        // FIX: Turn the model 90 degrees (Math.PI / 2.0 radians) to its right
        finalRotation.rotateY((float) (-Math.PI / 2.0));

        // Apply your original rolling/spinning animation on top of the turn
        finalRotation.rotateZ(this.age * this.rotationSpeed);

        matrices.multiply(finalRotation);

        // Apply scale modifications smoothly
        matrices.scale(this.scale, this.scale, this.scale);

        // 4. Retrieve dummy item stack containing your custom Blockbench json model
        net.minecraft.item.ItemStack itemStack = new net.minecraft.item.ItemStack(ModItems.CUSTOM_BIRD);

        int lightmapValue = this.getBrightness(tickDelta);
        var client = MinecraftClient.getInstance();

        // 5. Hand the matrix off to the ItemRenderer using a dedicated, forced-flush buffer
        var bufferBuilders = client.getBufferBuilders().getEntityVertexConsumers();

        // Render the item model into the buffer
        client.getItemRenderer().renderItem(
                itemStack,
                net.minecraft.client.render.model.json.ModelTransformationMode.GROUND, // Changed from NONE to GROUND to prevent model flattening
                lightmapValue,
                net.minecraft.client.render.OverlayTexture.DEFAULT_UV,
                matrices,
                bufferBuilders,
                this.world,
                0
        );

        // CRUCIAL FIX: Force the global buffer to immediately flush and draw your custom particle pass
        bufferBuilders.draw();

        matrices.pop();
    }

    @Override
    public ParticleTextureSheet getType() {
        // MUST return CUSTOM when using explicit render buffers, otherwise Minecraft crashes trying to batch it with quads
        return ParticleTextureSheet.CUSTOM;
    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        // Added annotation wrapper block to explicitly clear IntelliJ's velocityX parameter warning
        @SuppressWarnings("unused")
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

            return new FlockOrbitParticle(world, x, y, z, this.spriteProvider, foundEntity, velocityX);
        }
    }
}