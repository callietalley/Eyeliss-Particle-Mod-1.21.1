package eyeliss.particle.mod.component;

import eyeliss.particle.mod.EyelisssParticleMod;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModComponents {

    // Matches the frozen fallback key seen in your crash logs: "eyeliss_particle_mod:is_cursed"
    public static final ComponentType<Boolean> IS_CURSED = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("eyeliss_particle_mod", "is_cursed"),
            ComponentType.<Boolean>builder().codec(Codec.BOOL).build()
    );

    public static void registerComponents() {
        // Triggering this method forces Java to load the static block definitions above early
        System.out.println("Initializing Shadow Curse Data Components for " + EyelisssParticleMod.MOD_ID);
    }
}