package eyeliss.particle.mod.client.render;

import eyeliss.particle.mod.entity.ThrownSyringeEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class ThrownSyringeEntityRenderer extends EntityRenderer<ThrownSyringeEntity> {
    private final ItemRenderer itemRenderer;
    private final float scale;
    private final boolean lit;

    public ThrownSyringeEntityRenderer(EntityRendererFactory.Context context) {
        this(context, 1.0F, false);
    }

    public ThrownSyringeEntityRenderer(EntityRendererFactory.Context context, float scale, boolean lit) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.scale = scale;
        this.lit = lit;
    }

    @Override
    public void render(ThrownSyringeEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (entity.age >= 2 || !(this.dispatcher.camera.getFocusedEntity().squaredDistanceTo(entity) < 12.25)) {
            matrices.push();
            matrices.scale(this.scale, this.scale, this.scale);

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(net.minecraft.util.math.MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(net.minecraft.util.math.MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch())));

            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));

            this.itemRenderer.renderItem(
                    entity.getSyringeStack(),
                    ModelTransformationMode.FIXED,
                    this.lit ? 15728880 : light,
                    net.minecraft.client.render.OverlayTexture.DEFAULT_UV,
                    matrices,
                    vertexConsumers,
                    entity.getWorld(),
                    entity.getId()
            );

            matrices.pop();
            super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        }
    }

    @Override
    public Identifier getTexture(ThrownSyringeEntity entity) {
        return net.minecraft.screen.PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }
}