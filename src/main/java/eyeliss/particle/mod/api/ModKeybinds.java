package eyeliss.particle.mod.api;

import eyeliss.particle.mod.EyelisssParticleMod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {

    public static KeyBinding quaternary_active;

    public static void register() {
        quaternary_active = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.eyeliss_particle_mod.quaternary_active",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "key.category.eyeliss_particle_mod.quaternary_active"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (quaternary_active.wasPressed()) {
                // Literally does nothing on it's own.
            }
        });
    }
}