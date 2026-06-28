package eyeliss.particle.mod.network;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static eyeliss.particle.mod.EyelisssParticleMod.MOD_ID;

public class NamespaceWarperPayloads {

    public record ApplyIdentityPayload(String customName, boolean hideNameplate) implements CustomPayload {

        public static final CustomPayload.Id ID =
                new CustomPayload.Id<>(Identifier.of(MOD_ID, "apply_identity"));

        public static final PacketCodec CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, ApplyIdentityPayload::customName,
                PacketCodecs.BOOL, ApplyIdentityPayload::hideNameplate,
                ApplyIdentityPayload::new
        );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return (CustomPayload.Id<? extends CustomPayload>) ID;
        }
    }
}
