package eyeliss.particle.mod;

import eyeliss.particle.mod.particle.ModParticles;
import eyeliss.particle.mod.particle.RageParticle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class EyelisssParticleModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        ParticleFactoryRegistry.getInstance().register(ModParticles.RAGE_PARTICLE, RageParticle.Factory::new);
    }
}
