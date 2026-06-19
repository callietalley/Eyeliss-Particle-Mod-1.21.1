package eyeliss.particle.mod.screen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

import static eyeliss.particle.mod.EyelisssParticleMod.MOD_ID;

public class RiftGemScreens {
    public static final ScreenHandlerType<RiftGemScreenHandler> RIFT_GEM_SCREEN_HANDLER =
            new ScreenHandlerType<>(RiftGemScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES);

    public static final ScreenHandlerType<RiftGemBindScreenHandler> RIFT_GEM_BIND_HANDLER =
            new ScreenHandlerType<>(RiftGemBindScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES);

    public static void registerScreenHandlers() {
        Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "rift_gem_handler"), RIFT_GEM_SCREEN_HANDLER);
        Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "rift_gem_bind_handler"), RIFT_GEM_BIND_HANDLER);
    }
}
