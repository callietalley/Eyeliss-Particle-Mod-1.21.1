package eyeliss.particle.mod.api;

import eyeliss.particle.mod.network.RiftGemPayloads;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {

    public static KeyBinding quaternary_active;
    public static KeyBinding trinket_key;

    public static void register() {
        quaternary_active = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.eyeliss_particle_mod.quaternary_active",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "key.category.eyeliss_particle_mod.keybinds"
        ));

        trinket_key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.eyeliss_particle_mod.trinket_key",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "key.category.eyeliss_particle_mod.keybinds"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (quaternary_active.wasPressed()) {
            }

            while (trinket_key.wasPressed()) {
                boolean isEquipped = TrinketsApi.getTrinketComponent(client.player)
                        .map(comp -> comp.isEquipped(stack -> stack.isOf(eyeliss.particle.mod.item.trinkets.ModTrinkets.RIFT_GEM)))
                        .orElse(false);

                if (isEquipped) {
                    boolean isSneaking = client.player.isSneaking();
                    ClientPlayNetworking.send(new RiftGemPayloads.OpenRiftScreenPayload(isSneaking));
                }
            }
        });

    }
}
