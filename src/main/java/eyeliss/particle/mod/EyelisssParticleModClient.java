package eyeliss.particle.mod;

import eyeliss.particle.mod.api.ModKeybinds;
import eyeliss.particle.mod.client.render.UmberwitherRenderer;
import eyeliss.particle.mod.entity.ModEntities; // Import your entities class
import eyeliss.particle.mod.particle.FlockAuraParticle;
import eyeliss.particle.mod.particle.FlockOrbitParticle;
import eyeliss.particle.mod.particle.ModParticles;
import eyeliss.particle.mod.particle.RageParticle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry; // Import Fabric's renderer registry


public class EyelisssParticleModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register your particles
        ParticleFactoryRegistry.getInstance().register(ModParticles.RAGE_PARTICLE, RageParticle.Factory::new);

        ParticleFactoryRegistry.getInstance().register(ModParticles.FLOCK_ORBIT_PARTICLE, FlockOrbitParticle.Factory::new);

        ParticleFactoryRegistry.getInstance().register(ModParticles.FLOCK_AURA_PARTICLE, FlockAuraParticle.Factory::new);

        // Call your dedicated keybind registration class
        ModKeybinds.register();

        // Directs the client to render the Umberwither using the vanilla Wither's model, animations, and textures
        EntityRendererRegistry.register(ModEntities.UMBERWITHER, UmberwitherRenderer::new);
    }
}