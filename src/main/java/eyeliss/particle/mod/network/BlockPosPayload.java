package eyeliss.particle.mod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.packet.CustomPayload;

import java.util.List;

public record BlockPosPayload(BlockPos pos, List<Integer> activeRecipeIndices) implements CustomPayload {

    public static final CustomPayload.Id<BlockPosPayload> ID = new CustomPayload.Id<>(Identifier.of("eyelisspartmod", "block_pos_payload"));

    public static final PacketCodec<RegistryByteBuf, BlockPosPayload> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, BlockPosPayload::pos,
            PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), BlockPosPayload::activeRecipeIndices,
            BlockPosPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
