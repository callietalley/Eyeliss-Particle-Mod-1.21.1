package eyeliss.particle.mod.network;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.network.PacketByteBuf; // Changed import
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ShadowBundleScrollPayload(int slotId, boolean scrollUp) implements CustomPayload {
    public static final Id<ShadowBundleScrollPayload> ID = new Id<>(Identifier.of(EyelisssParticleMod.MOD_ID, "shadow_bundle_scroll"));

    // Changing the buffer parameter to PacketByteBuf automatically makes it match PayloadTypeRegistry
    public static final PacketCodec<PacketByteBuf, ShadowBundleScrollPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ShadowBundleScrollPayload::slotId,
            PacketCodecs.BOOL, ShadowBundleScrollPayload::scrollUp,
            ShadowBundleScrollPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}