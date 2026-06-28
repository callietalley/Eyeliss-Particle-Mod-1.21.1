package eyeliss.particle.mod.mixin.client;

import dev.emi.trinkets.api.TrinketsApi;
import eyeliss.particle.mod.item.trinkets.NamespaceWarperItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void hideOverheadNametag(Entity entity, Text text, MatrixStack matrices,
                                     VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity player)) return;

        var trinketComp = TrinketsApi.getTrinketComponent(player);
        if (trinketComp.isPresent()) {
            for (var equip : trinketComp.get().getAllEquipped()) {
                var stack = equip.getRight();

                if (stack.getItem() instanceof NamespaceWarperItem) {
                    NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                    NbtCompound nbt = nbtComponent.copyNbt();

                    boolean hideNameplate = nbt.getBoolean("IdentityHideNameplate");

                    if (hideNameplate) {
                        ci.cancel();
                        return;
                    }
                }
            }
        }
    }

    @ModifyVariable(method = "renderLabelIfPresent", at = @At("HEAD"), argsOnly = true)
    private Text swapOverheadNametagText(Text originalText, Entity entity, Text text, MatrixStack matrices,
                                         VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        if (!(entity instanceof PlayerEntity player)) return originalText;

        var trinketComp = TrinketsApi.getTrinketComponent(player);
        if (trinketComp.isPresent()) {
            for (var equip : trinketComp.get().getAllEquipped()) {
                var stack = equip.getRight();

                if (stack.getItem() instanceof NamespaceWarperItem) {
                    NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                    NbtCompound nbt = nbtComponent.copyNbt();

                    String customName = nbt.getString("IdentityCustomName").trim();

                    if (!customName.isEmpty()) {
                        return Text.literal(customName);
                    }
                }
            }
        }
        return originalText;
    }
}
