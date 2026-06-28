package eyeliss.particle.mod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static eyeliss.particle.mod.EyelisssParticleMod.MOD_ID;

public class RiftGemPayloads {

    public record BindEnvironmentPayload(String envName) implements CustomPayload {
        public static final CustomPayload.Id<BindEnvironmentPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "bind_environment"));
        public static final PacketCodec<RegistryByteBuf, BindEnvironmentPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, BindEnvironmentPayload::envName,
                BindEnvironmentPayload::new
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record TypeWarpPayload(String typedName) implements CustomPayload {
        public static final CustomPayload.Id<TypeWarpPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "type_warp_request"));
        public static final PacketCodec<RegistryByteBuf, TypeWarpPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, TypeWarpPayload::typedName,
                TypeWarpPayload::new
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }
}
