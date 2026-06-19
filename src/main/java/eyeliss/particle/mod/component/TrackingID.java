package eyeliss.particle.mod.component;

import com.mojang.serialization.Codec;
import eyeliss.particle.mod.EyelisssParticleMod;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TrackingID {

    // Registered directly under your standard mod ID namespace
    public static final ComponentType<String> TRACKING_ID = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(EyelisssParticleMod.MOD_ID, "tracking_id"),
            ComponentType.<String>builder().codec(Codec.STRING).build()
    );

    // This method can remain empty—referencing it forces Java to load the class and register the field above
    public static void load() {}
}