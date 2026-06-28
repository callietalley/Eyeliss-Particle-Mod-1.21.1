package eyeliss.particle.mod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static eyeliss.particle.mod.EyelisssParticleMod.MOD_ID;

public class TrinketKeybindPayloads {

    public record OpenRiftScreenPayload(boolean isSneaking) implements CustomPayload {
        public static final CustomPayload.Id<OpenRiftScreenPayload> ID =
                new CustomPayload.Id<>(Identifier.of(MOD_ID, "open_rift_screen"));

        public static final PacketCodec<RegistryByteBuf, OpenRiftScreenPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.BOOL, OpenRiftScreenPayload::isSneaking,
                OpenRiftScreenPayload::new
        );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record OpenAspectScreenPayload(boolean isSneaking) implements CustomPayload {
        public static final CustomPayload.Id<OpenAspectScreenPayload> ID =
                new CustomPayload.Id<>(Identifier.of(MOD_ID, "open_aspect_screen"));

        public static final PacketCodec<RegistryByteBuf, OpenAspectScreenPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.BOOL, OpenAspectScreenPayload::isSneaking,
                OpenAspectScreenPayload::new
        );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
