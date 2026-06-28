package eyeliss.particle.mod.mixin.client;

import dev.emi.trinkets.api.TrinketsApi;
import eyeliss.particle.mod.item.trinkets.NamespaceWarperItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntity.class)
public class ClientPlayerNameMixin {

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void overrideJadeTooltipName(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        var trinketComp = TrinketsApi.getTrinketComponent(player);
        if (trinketComp.isPresent()) {
            for (var equip : trinketComp.get().getAllEquipped()) {
                ItemStack stack = equip.getRight();

                if (stack.getItem() instanceof NamespaceWarperItem) {
                    NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                    NbtCompound nbt = nbtComponent.copyNbt();

                    String customName = nbt.getString("IdentityCustomName").trim();
                    boolean hideNameplate = nbt.getBoolean("IdentityHideNameplate");

                    if (hideNameplate) {
                        cir.setReturnValue(Text.empty());
                        return;
                    }

                    if (!customName.isEmpty()) {
                        cir.setReturnValue(Text.literal(customName));
                        return;
                    }
                }
            }
        }
    }
}
