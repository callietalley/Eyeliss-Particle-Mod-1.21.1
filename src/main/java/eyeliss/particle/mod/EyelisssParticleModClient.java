package eyeliss.particle.mod;

import eyeliss.particle.mod.api.ModKeybinds;
import eyeliss.particle.mod.client.ShadowParticleHandler;
import eyeliss.particle.mod.client.render.UmberwitherRenderer;
import eyeliss.particle.mod.client.OverhealthBarRenderer; // Added
import eyeliss.particle.mod.entity.ModEntities;
import eyeliss.particle.mod.item.ModItems;
import eyeliss.particle.mod.network.OverhealthSyncPayload; // Added
import eyeliss.particle.mod.particle.*;
import eyeliss.particle.mod.util.ClientOverhealthTracker; // Added
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking; // Added
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback; // Added
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
        ParticleFactoryRegistry.getInstance().register(ModParticles.OVERHEALTH_ORBIT, OverhealthOrbitParticle.Factory::new);


        ModKeybinds.register();

        EntityRendererRegistry.register(ModEntities.UMBERWITHER, UmberwitherRenderer::new);

        ModelPredicateProviderRegistry.register(ModItems.SHADOW_BUNDLE, Identifier.of("filled"), (stack, world, entity, seed) -> {
            BundleContentsComponent contents = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
            if (contents == null || contents.isEmpty()) {
                return 0.0F;
            }
            return 1.0F;
        });

        // ==========================================
        //         CUSTOM OVERHEALTH SYSTEM
        // ==========================================

        // 1. Listen for the server packet and update client storage variables
        ClientPlayNetworking.registerGlobalReceiver(OverhealthSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientOverhealthTracker.currentShield = payload.currentShield();
                ClientOverhealthTracker.maxShield = payload.maxShield();
            });
        });

        // 2. Register the dynamic pixel-by-pixel HUD overlay renderer
        HudRenderCallback.EVENT.register(new OverhealthBarRenderer());
    }
}