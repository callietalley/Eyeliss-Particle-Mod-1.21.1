package eyeliss.particle.mod.mixin;

import dev.emi.trinkets.api.TrinketsApi;
import eyeliss.particle.mod.item.trinkets.NamespaceWarperItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerDisplayNameMixin {

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void overrideChatAndDeathLogsName(CallbackInfoReturnable<Text> cir) {
        PlayerEntity basePlayer = (PlayerEntity) (Object) this;

        if (!(basePlayer instanceof ServerPlayerEntity player)) return;

        var trinketComp = TrinketsApi.getTrinketComponent(player);
        if (trinketComp.isPresent()) {
            for (var equip : trinketComp.get().getAllEquipped()) {
                ItemStack stack = equip.getRight();

                if (stack.getItem() instanceof NamespaceWarperItem) {
                    NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                    NbtCompound nbt = nbtComponent.copyNbt();

                    String customName = nbt.getString("IdentityCustomName").trim();

                    if (!customName.isEmpty()) {
                        Text baseCustomText = Text.literal(customName);

                        Text formattedAlias = player.getScoreboardTeam() != null
                                ? player.getScoreboardTeam().decorateName(baseCustomText)
                                : baseCustomText;

                        cir.setReturnValue(formattedAlias);
                        return;
                    }
                }
            }
        }
    }
}
