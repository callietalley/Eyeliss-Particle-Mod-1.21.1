package eyeliss.particle.mod.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import eyeliss.particle.mod.network.BlockPosPayload;

import static eyeliss.particle.mod.EyelisssParticleMod.MOD_ID;

public class ModScreenHandlers {
    public static final ScreenHandlerType<RiftGemScreenHandler> RIFT_GEM_SCREEN_HANDLER =
            new ScreenHandlerType<>(RiftGemScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES);

    public static final ScreenHandlerType<RiftGemBindScreenHandler> RIFT_GEM_BIND_HANDLER =
            new ScreenHandlerType<>(RiftGemBindScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES);

    public static final ScreenHandlerType<AdvancedWeaponSmithingScreenHandler> ADVANCED_WEAPON_SMITHING_HANDLER =
            new ExtendedScreenHandlerType<>(AdvancedWeaponSmithingScreenHandler::new, BlockPosPayload.PACKET_CODEC);

    public static void registerScreenHandlers() {
        Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "rift_gem_handler"), RIFT_GEM_SCREEN_HANDLER);
        Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "rift_gem_bind_handler"), RIFT_GEM_BIND_HANDLER);

        Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "advanced_weapon_smithing_handler"), ADVANCED_WEAPON_SMITHING_HANDLER);
    }
}
