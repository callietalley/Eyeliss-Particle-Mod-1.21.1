package eyeliss.particle.mod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SelectWeaponC2SPayload(String recipeId) implements CustomPayload {
    public static final CustomPayload.Id<SelectWeaponC2SPayload> ID = new CustomPayload.Id<>(Identifier.of("eyelisspartmod", "select_weapon_payload"));

    public static final PacketCodec<RegistryByteBuf, SelectWeaponC2SPayload> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SelectWeaponC2SPayload::recipeId,
            SelectWeaponC2SPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}