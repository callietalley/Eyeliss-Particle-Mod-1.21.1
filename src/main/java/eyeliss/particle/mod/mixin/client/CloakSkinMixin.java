package eyeliss.particle.mod.mixin.client;

import dev.emi.trinkets.api.TrinketsApi;
import eyeliss.particle.mod.item.trinkets.HiddenCloakItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.authlib.GameProfile;

@Environment(EnvType.CLIENT)
public class CloakSkinMixin {

    @Mixin(PlayerListEntry.class)
    public static abstract class TabListSkinMixin {
        @Shadow @Final private GameProfile profile;

        @Unique
        private static final Identifier CLOAK_SKIN = Identifier.of("eyelisspartmod", "textures/models/cloak_skin.png");

        @Inject(method = "getSkinTextures", at = @At("HEAD"), cancellable = true)
        private void overrideTabListHeadIcon(CallbackInfoReturnable<SkinTextures> cir) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || this.profile == null) return;

            PlayerEntity player = client.world.getPlayerByUuid(this.profile.getId());
            if (player != null) {
                ItemStack cloakStack = TrinketsApi.getTrinketComponent(player)
                        .flatMap(comp -> comp.getAllEquipped().stream()
                                .map(net.minecraft.util.Pair::getRight)
                                .filter(stack -> stack.getItem() instanceof HiddenCloakItem)
                                .findFirst())
                        .orElse(ItemStack.EMPTY);

                if (!cloakStack.isEmpty()) {
                    NbtComponent nbtComponent = cloakStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);

                    if (nbtComponent.copyNbt().getBoolean("CloakToggle")) {
                        SkinTextures original = cir.getReturnValue();
                        cir.setReturnValue(new SkinTextures(
                                CLOAK_SKIN,
                                null,
                                original != null ? original.capeTexture() : null,
                                original != null ? original.elytraTexture() : null,
                                SkinTextures.Model.WIDE,
                                true
                        ));
                    }
                }
            }
        }
    }

    @Mixin(net.minecraft.client.network.AbstractClientPlayerEntity.class)
    public static abstract class InWorldPlayerSkinMixin {
        @Unique
        private static final Identifier CLOAK_SKIN = Identifier.of("eyelisspartmod", "textures/models/cloak_skin.png");

        @Inject(method = "getSkinTextures", at = @At("HEAD"), cancellable = true)
        private void overrideInWorldSkinMesh(CallbackInfoReturnable<SkinTextures> cir) {
            PlayerEntity player = (PlayerEntity) (Object) this;

            ItemStack cloakStack = TrinketsApi.getTrinketComponent(player)
                    .flatMap(comp -> comp.getAllEquipped().stream()
                            .map(net.minecraft.util.Pair::getRight)
                            .filter(stack -> stack.getItem() instanceof HiddenCloakItem)
                            .findFirst())
                    .orElse(ItemStack.EMPTY);

            if (!cloakStack.isEmpty()) {
                NbtComponent nbtComponent = cloakStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);

                if (nbtComponent.copyNbt().getBoolean("CloakToggle")) {
                    SkinTextures original = cir.getReturnValue();
                    cir.setReturnValue(new SkinTextures(
                            CLOAK_SKIN,
                            null,
                            original != null ? original.capeTexture() : null,
                            original != null ? original.elytraTexture() : null,
                            SkinTextures.Model.WIDE,
                            true
                    ));
                }
            }
        }
    }
}
