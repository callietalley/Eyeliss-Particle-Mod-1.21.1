package eyeliss.particle.mod.api;

import eyeliss.particle.mod.network.RiftGemPayloads;
import eyeliss.particle.mod.network.TrinketKeybindPayloads;
import eyeliss.particle.mod.network.VeinMineKeyPayload;
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
    public static KeyBinding secondary_trinket_key;
    public static KeyBinding vein_mine_active_key;
    private static boolean wasHeldLastTick = false;

    public static void register() {
        quaternary_active = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.eyelisspartmod.quaternary_active",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "key.category.eyelisspartmod.keybinds"
        ));

        trinket_key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.eyelisspartmod.trinket_key",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "key.category.eyelisspartmod.keybinds"
        ));

        secondary_trinket_key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.eyelisspartmod.secondary_trinket_key",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "key.category.eyelisspartmod.keybinds"
        ));

        vein_mine_active_key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.eyelisspartmod.vein_mine_key",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                "key.category.eyelisspartmod.keybinds"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            boolean isHeldThisTick = vein_mine_active_key.isPressed();
            if (isHeldThisTick != wasHeldLastTick) {
                ClientPlayNetworking.send(new VeinMineKeyPayload(isHeldThisTick));
                wasHeldLastTick = isHeldThisTick;
            }

            while (quaternary_active.wasPressed()) {}

            while (trinket_key.wasPressed()) {
                boolean hasActiveTrinket = TrinketsApi.getTrinketComponent(client.player)
                        .map(comp -> comp.getAllEquipped().stream()
                                .anyMatch(equip -> equip.getRight().getItem() instanceof IActiveTrinketItem))
                        .orElse(false);

                if (hasActiveTrinket) {
                    boolean isSneaking = client.player.isSneaking();
                    ClientPlayNetworking.send(new TrinketKeybindPayloads.OpenRiftScreenPayload(isSneaking));
                }
            }

            while (secondary_trinket_key.wasPressed()) {
                boolean hasAspectTrinket = TrinketsApi.getTrinketComponent(client.player)
                        .map(comp -> comp.getAllEquipped().stream()
                                .anyMatch(equip -> equip.getRight().getItem() instanceof IAspectTrinketItem))
                        .orElse(false);

                if (hasAspectTrinket) {
                    boolean isSneaking = client.player.isSneaking();
                    ClientPlayNetworking.send(new TrinketKeybindPayloads.OpenAspectScreenPayload(isSneaking));
                }
            }
        });
    }
}
