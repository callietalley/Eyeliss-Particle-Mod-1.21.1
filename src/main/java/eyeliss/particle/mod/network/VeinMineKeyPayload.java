package eyeliss.particle.mod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record VeinMineKeyPayload(boolean isHeldDown) implements CustomPayload {
    public static final Id<VeinMineKeyPayload> ID = new Id<>(Identifier.of("eyelisspartmod", "vein_mine_key"));
    public static final PacketCodec<RegistryByteBuf, VeinMineKeyPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, VeinMineKeyPayload::isHeldDown,
            VeinMineKeyPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
