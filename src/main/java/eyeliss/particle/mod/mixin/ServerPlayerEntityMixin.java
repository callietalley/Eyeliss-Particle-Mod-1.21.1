package eyeliss.particle.mod.mixin;

import dev.emi.trinkets.api.TrinketsApi;
import eyeliss.particle.mod.item.trinkets.NamespaceWarperItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Unique
    private static final Map<UUID, Boolean> IDENTITY_TRINKET_CACHE = new HashMap<>();

    @Inject(method = "playerTick", at = @At("HEAD"))
    private void monitorIdentityTrinketSlotsRealtime(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.getWorld().isClient()) return;

        UUID uuid = player.getUuid();

        var trinketComp = TrinketsApi.getTrinketComponent(player);
        boolean isTrinketEquipped = false;

        if (trinketComp.isPresent()) {
            for (var equip : trinketComp.get().getAllEquipped()) {
                ItemStack stack = equip.getRight();
                if (stack.getItem() instanceof NamespaceWarperItem) {
                    isTrinketEquipped = true;
                    break;
                }
            }
        }

        boolean wasEquippedLastTick = IDENTITY_TRINKET_CACHE.getOrDefault(uuid, false);

        if (!isTrinketEquipped && wasEquippedLastTick) {

            player.setCustomName(null);
            player.setCustomNameVisible(false);

            if (player.getServer() != null) {
                PlayerListS2CPacket clearPacket = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
                player.getServer().getPlayerManager().sendToAll(clearPacket);
            }

            player.getServerWorld().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_BEACON_POWER_SELECT,
                    SoundCategory.PLAYERS,
                    0.8f,
                    1.2f
            );

            for (int i = 0; i < player.getInventory().size(); i++) {
                cleanseProfileStack(player.getInventory().getStack(i));
            }

            if (player.currentScreenHandler != null) {
                cleanseProfileStack(player.currentScreenHandler.getCursorStack());
            }
        }

        IDENTITY_TRINKET_CACHE.put(uuid, isTrinketEquipped);
    }

    @Unique
    private void cleanseProfileStack(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof NamespaceWarperItem) {
            NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
            NbtCompound nbt = nbtComponent.copyNbt();

            if (!nbt.getString("IdentityCustomName").isEmpty() || nbt.getBoolean("IdentityHideNameplate")) {
                nbt.putString("IdentityCustomName", "");
                nbt.putBoolean("IdentityHideNameplate", false);
                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
            }
        }
    }
}
