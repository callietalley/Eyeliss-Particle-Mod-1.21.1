package eyeliss.particle.mod.item.trinkets;

import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import eyeliss.particle.mod.api.IActiveTrinketItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class HiddenCloakItem extends TrinketItem implements IActiveTrinketItem {

    public HiddenCloakItem(Settings settings) {
        super(settings);
    }

    @Override
    public void onTrinketKeybindPressed(ServerPlayerEntity player, ItemStack stack, boolean isSneaking) {
        NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = nbtComponent.copyNbt();

        boolean isActive = !nbt.getBoolean("CloakToggle");
        nbt.putBoolean("CloakToggle", isActive);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        if (player.getServer() != null) {
            PlayerListS2CPacket refreshPacket = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
            player.getServer().getPlayerManager().sendToAll(refreshPacket);
        }

        if (isActive) {
            player.sendMessage(Text.literal("§d[Cloak] Camouflage active."), true);
            player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS, 0.8f, 0.6f);
        } else {
            player.sendMessage(Text.literal("§7[Cloak] Camouflage deactivated."), true);
            player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS, 0.8f, 1.4f);
        }
    }

    @Override
    public Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, Identifier slotIdentifier) {
        Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifiers =
                com.google.common.collect.HashMultimap.create(super.getModifiers(stack, slot, entity, slotIdentifier));

        if (slot.inventory().getSlotType().getId().endsWith("legs/pocket")) {
            Identifier cloakSpeedId = Identifier.of("eyelisspartmod", "trinket_cloak_speed");

            modifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(
                    cloakSpeedId,
                    0.05,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }

        return modifiers;
    }
}
