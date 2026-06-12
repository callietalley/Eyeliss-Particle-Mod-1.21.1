package eyeliss.particle.mod.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import eyeliss.particle.mod.effect.ModEffects; // Added to access your status effect registry context
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity; // Added for effect verification casting
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries; // Added for modern 1.21.1 entry checks
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class OverhealthOrbitParticle extends Particle {

    protected final SpriteProvider spriteProvider;
    private final float baseScale;
    private float currentRenderScale;

    // --- Orbit & Tracking Variables ---
    private Entity targetEntity;
    private final double spawnX;
    private final double spawnY;
    private final double spawnZ;
    private final double baseOrbitRadius;
    double currentOrbitRadius;
    private final double particleOrbitSpeed;
    private double orbitAngle;

    public OverhealthOrbitParticle(ClientWorld clientWorld, double x, double y, double z,
                                   SpriteProvider spriteProvider, Entity targetEntity, int shieldIndex) {
        super(clientWorld, x, y, z);

        this.spriteProvider = spriteProvider;
        this.targetEntity = targetEntity;

        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.velocityZ = 0.0;

        this.spawnX = x;
        this.spawnY = y;
        this.spawnZ = z;

        this.baseOrbitRadius = 0.85;
        this.currentOrbitRadius = this.baseOrbitRadius;
        this.particleOrbitSpeed = 0.15;

        double angleOffset = shieldIndex * ((Math.PI * 2.0) / 3.0);
        this.orbitAngle = angleOffset;

        this.maxAge = 70; // 3.5-second max lifecycle window

        this.baseScale = 1.68f;
        this.currentRenderScale = 0.0f;
        this.scale(0.0f);

        this.updateOrbitPosition();
    }

    @Override
    public void tick() {
        if (this.targetEntity instanceof LivingEntity livingTarget) {
            var overhealthEntry = Registries.STATUS_EFFECT.getEntry(ModEffects.OVERHEALTH);
            if (!livingTarget.hasStatusEffect(overhealthEntry)) {
                this.markDead(); // Clear vertices instantly this frame
                return;
            }
        }

        // Standard validation safety fallback check
        if (this.age++ >= this.maxAge || this.targetEntity == null || !this.targetEntity.isAlive()) {
            this.markDead();
            return;
        }

        this.orbitAngle += this.particleOrbitSpeed;

        int spawnInTicks = 2;
        int shrinkStartTick = (int) (this.maxAge * 0.85f); // Ticks 60 to 70

        if (this.age <= spawnInTicks) {
            float growthProgress = (float) this.age / (float) spawnInTicks;
            growthProgress = MathHelper.clamp(growthProgress, 0.0f, 1.0f);
            this.currentRenderScale = this.baseScale * growthProgress;
            this.scale(this.currentRenderScale);
            this.currentOrbitRadius = this.baseOrbitRadius;

        } else if (this.age >= shrinkStartTick) {
            float shrinkProgress = (float) (this.maxAge - this.age) / (float) (this.maxAge - shrinkStartTick);
            shrinkProgress = MathHelper.clamp(shrinkProgress, 0.0f, 1.0f);
            this.currentRenderScale = this.baseScale * shrinkProgress;
            this.scale(this.currentRenderScale);
            this.currentOrbitRadius = this.baseOrbitRadius * shrinkProgress;

        } else {
            this.currentRenderScale = this.baseScale;
            this.scale(this.baseScale);
            this.currentOrbitRadius = this.baseOrbitRadius;
        }

        this.updateOrbitPosition();
    }

    private void updateOrbitPosition() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.targetEntity == null) {
            this.targetEntity = MinecraftClient.getInstance().player;
        }

        double baseCenterX = (this.targetEntity != null) ? this.targetEntity.getX() : this.spawnX;
        double baseCenterY = (this.targetEntity != null) ? (this.targetEntity.getBodyY(0.4) + 0.25) : this.spawnY + 0.25;
        double baseCenterZ = (this.targetEntity != null) ? this.targetEntity.getZ() : this.spawnZ;

        double targetX = baseCenterX + (this.currentOrbitRadius * Math.cos(this.orbitAngle));
        double targetY = baseCenterY;
        double targetZ = baseCenterZ + (this.currentOrbitRadius * Math.sin(this.orbitAngle));

        this.setPos(targetX, targetY, targetZ);
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3d cameraPos = camera.getPos();
        float renderX = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - cameraPos.getX());
        float renderY = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - cameraPos.getY());
        float renderZ = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - cameraPos.getZ());

        net.minecraft.client.util.math.MatrixStack matrices = new net.minecraft.client.util.math.MatrixStack();
        matrices.push();

        matrices.translate(renderX, renderY, renderZ);

        Quaternionf finalRotation = new Quaternionf();
        finalRotation.rotationY((float) (-this.orbitAngle + (Math.PI / 2.0)));

        matrices.multiply(finalRotation);
        matrices.scale(this.currentRenderScale * 2.0f, this.currentRenderScale * 0.85f, this.currentRenderScale);

        ItemStack itemStack = new ItemStack(Items.SHIELD);
        int lightmapValue = this.getBrightness(tickDelta);

        var client = MinecraftClient.getInstance();
        VertexConsumerProvider.Immediate bufferBuilders = client.getBufferBuilders().getEntityVertexConsumers();

        client.getItemRenderer().renderItem(
                itemStack,
                net.minecraft.client.render.model.json.ModelTransformationMode.GROUND,
                lightmapValue,
                net.minecraft.client.render.OverlayTexture.DEFAULT_UV,
                matrices,
                bufferBuilders,
                this.world,
                0
        );

        bufferBuilders.draw();
        matrices.pop();
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;
        private int currentSpawningIndex = 0;

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

            int assignedIndex = this.currentSpawningIndex;
            this.currentSpawningIndex = (this.currentSpawningIndex + 1) % 3;

            return new OverhealthOrbitParticle(world, x, y, z, this.spriteProvider, foundEntity, assignedIndex);
        }
    }
}
