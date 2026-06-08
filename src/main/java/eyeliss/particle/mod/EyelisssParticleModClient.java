package eyeliss.particle.mod;

import eyeliss.particle.mod.api.ModKeybinds;
import eyeliss.particle.mod.client.ShadowParticleHandler;
import eyeliss.particle.mod.client.render.UmberwitherRenderer;
import eyeliss.particle.mod.entity.ModEntities; // Import your entities class
import eyeliss.particle.mod.item.ModItems;
import eyeliss.particle.mod.particle.FlockAuraParticle;
import eyeliss.particle.mod.particle.FlockOrbitParticle;
import eyeliss.particle.mod.particle.ModParticles;
import eyeliss.particle.mod.particle.RageParticle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry; // Import Fabric's renderer registry
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.util.Identifier;


public class EyelisssParticleModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        ShadowParticleHandler.register();
        ParticleFactoryRegistry.getInstance().register(ModParticles.RAGE_PARTICLE, RageParticle.Factory::new);

        ParticleFactoryRegistry.getInstance().register(ModParticles.FLOCK_ORBIT_PARTICLE, FlockOrbitParticle.Factory::new);

        ParticleFactoryRegistry.getInstance().register(ModParticles.FLOCK_AURA_PARTICLE, FlockAuraParticle.Factory::new);

        ModKeybinds.register();

        EntityRendererRegistry.register(ModEntities.UMBERWITHER, UmberwitherRenderer::new);
        ModelPredicateProviderRegistry.register(ModItems.SHADOW_BUNDLE, Identifier.of("filled"), (stack, world, entity, seed) -> {
            BundleContentsComponent contents = stack.get(DataComponentTypes.BUNDLE_CONTENTS);

            // Returns 0.0 (empty texture) if no component exists or it's empty; returns 1.0 (filled texture) if it holds items
            if (contents == null || contents.isEmpty()) {
                return 0.0F;
            }
            return 1.0F;
        });
    }
}