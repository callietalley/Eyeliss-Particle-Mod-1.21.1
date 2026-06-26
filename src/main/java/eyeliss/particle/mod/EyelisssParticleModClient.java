package eyeliss.particle.mod;

import eyeliss.particle.mod.api.ModKeybinds;
import eyeliss.particle.mod.block.ModBlocks;
import eyeliss.particle.mod.client.ShadowParticleHandler;
import eyeliss.particle.mod.client.SyringeColor;
import eyeliss.particle.mod.client.render.ThrownSyringeEntityRenderer;
import eyeliss.particle.mod.client.render.UmberwitherRenderer;
import eyeliss.particle.mod.client.OverhealthBarRenderer;
import eyeliss.particle.mod.entity.ModEntities;
import eyeliss.particle.mod.fluid.ModFluids;
import eyeliss.particle.mod.item.ModItems;
import eyeliss.particle.mod.network.OverhealthSyncPayload;
import eyeliss.particle.mod.particle.*;
import eyeliss.particle.mod.screen.AdvancedWeaponSmithingScreen;
import eyeliss.particle.mod.screen.ModScreenHandlers;
import eyeliss.particle.mod.screen.RiftGemScreen;
import eyeliss.particle.mod.screen.RiftGemBindScreen;
import eyeliss.particle.mod.util.ClientOverhealthTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.util.Identifier;

public class EyelisssParticleModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FluidRenderHandlerRegistry.INSTANCE.register(
                ModFluids.STILL_SOURCE_SAUCE,
                ModFluids.FLOWING_SOURCE_SAUCE,
                new SimpleFluidRenderHandler(
                        Identifier.of("eyelisspartmod", "block/source_sauce_still"),
                        Identifier.of("eyelisspartmod", "block/source_sauce_flow")
                )
        );

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SOURCE_BLOCK, net.minecraft.client.render.RenderLayer.getSolid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DEEP_SOURCE_BLOCK, net.minecraft.client.render.RenderLayer.getSolid());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.ADVANCED_WEAPON_SMITHING_BLOCK, RenderLayer.getCutout());

        // --- Entity & Screen Handlers ---
        EntityRendererRegistry.register(ModEntities.THROWN_SYRINGE, ThrownSyringeEntityRenderer::new);
        HandledScreens.register(ModScreenHandlers.RIFT_GEM_SCREEN_HANDLER, RiftGemScreen::new);
        HandledScreens.register(ModScreenHandlers.RIFT_GEM_BIND_HANDLER, RiftGemBindScreen::new);
        net.minecraft.client.gui.screen.ingame.HandledScreens.register(
                ModScreenHandlers.ADVANCED_WEAPON_SMITHING_HANDLER,
                AdvancedWeaponSmithingScreen::new
        );
        net.minecraft.client.gui.screen.ingame.HandledScreens.register(
                eyeliss.particle.mod.screen.ModScreenHandlers.ENGRAVING_GUIDE_HANDLER,
                eyeliss.particle.mod.screen.EngravingGuideScreen::new
        );

        // --- Particle Handlers ---
        ShadowParticleHandler.register();
        ParticleFactoryRegistry.getInstance().register(ModParticles.RAGE_PARTICLE, RageParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.FLOCK_ORBIT_PARTICLE, FlockOrbitParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.FLOCK_AURA_PARTICLE, FlockAuraParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.OVERHEALTH_ORBIT, OverhealthOrbitParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(
                eyeliss.particle.mod.particle.ModParticles.BLOOD_SMOKE,
                BloodSmokeParticle.Factory::new
        );

        // --- Keybinds & Additional Entities ---
        ModKeybinds.register();
        EntityRendererRegistry.register(ModEntities.UMBERWITHER, UmberwitherRenderer::new);

        // --- Item Predicates ---
        ModelPredicateProviderRegistry.register(ModItems.SHADOW_BUNDLE, Identifier.of("filled"), (stack, world, entity, seed) -> {
            BundleContentsComponent contents = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
            if (contents == null || contents.isEmpty()) {
                return 0.0F;
            }
            return 1.0F;
        });

        // --- Colors, Networking, & HUD Overlays ---
        SyringeColor.registerColor();

        ClientPlayNetworking.registerGlobalReceiver(OverhealthSyncPayload.ID, (payload, context) -> context.client().execute(() -> {
            ClientOverhealthTracker.currentShield = payload.currentShield();
            ClientOverhealthTracker.maxShield = payload.maxShield();
        }));

        HudRenderCallback.EVENT.register(new OverhealthBarRenderer());
    }
}
