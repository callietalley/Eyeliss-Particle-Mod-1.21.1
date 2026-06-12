package eyeliss.particle.mod.network;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OverhealthSyncPayload(float currentShield, float maxShield) implements CustomPayload {
    public static final Id<OverhealthSyncPayload> ID = new Id<>(Identifier.of(EyelisssParticleMod.MOD_ID, "overhealth_sync"));

    public static final PacketCodec<RegistryByteBuf, OverhealthSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodec.ofStatic((buf, val) -> buf.writeFloat(val), buf -> buf.readFloat()), OverhealthSyncPayload::currentShield,
            PacketCodec.ofStatic((buf, val) -> buf.writeFloat(val), buf -> buf.readFloat()), OverhealthSyncPayload::maxShield,
            OverhealthSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}