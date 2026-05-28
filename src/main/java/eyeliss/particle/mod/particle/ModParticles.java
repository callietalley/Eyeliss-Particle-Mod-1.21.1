package eyeliss.particle.mod.particle;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModParticles {
    public static final SimpleParticleType RAGE_PARTICLE =
            registerParticle("rage_particle", FabricParticleTypes.simple(true));


    private static SimpleParticleType registerParticle(String name, SimpleParticleType particleType) {
        return Registry.register(Registries.PARTICLE_TYPE, Identifier.of(EyelisssParticleMod.MOD_ID, name), particleType);
    }

    public static void registerParticles() {
        EyelisssParticleMod.LOGGER.info("Registering Particles for " + EyelisssParticleMod.MOD_ID);
    }
}
