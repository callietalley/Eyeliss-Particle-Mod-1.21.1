package eyeliss.particle.mod.event;

import eyeliss.particle.mod.component.ModComponents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ShadowCurseHandler {

    private static final Identifier DYNAMIC_HEALTH_CURSE_ID = Identifier.of("eyeliss_particle_mod", "dynamic_shadow_curse");
    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter % 10 != 0) return;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // Skip tracking if the player is dead or currently spectating
                if (player.isSpectator() || !player.isAlive()) continue;

                int cursedItemCount = 0;

                for (int slot = 0; slot < player.getInventory().main.size(); slot++) {
                    ItemStack stack = player.getInventory().main.get(slot);

                    if (stack != null && !stack.isEmpty()) {
                        if (stack.getOrDefault(ModComponents.IS_CURSED, false)) {
                            cursedItemCount += stack.getCount();
                        }
                    }
                }

                ItemStack offHandStack = player.getOffHandStack();
                if (offHandStack != null && !offHandStack.isEmpty()) {
                    if (offHandStack.getOrDefault(ModComponents.IS_CURSED, false)) {
                        cursedItemCount += offHandStack.getCount();
                    }
                }

                if (cursedItemCount > 4) {
                    cursedItemCount = 4;
                }

                EntityAttributeInstance maxHealthAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                if (maxHealthAttribute != null) {
                    maxHealthAttribute.removeModifier(DYNAMIC_HEALTH_CURSE_ID);

                    if (cursedItemCount > 0) {
                        double healthReductionValue = cursedItemCount * -4.0;

                        EntityAttributeModifier activeCurseModifier = new EntityAttributeModifier(
                                DYNAMIC_HEALTH_CURSE_ID,
                                healthReductionValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        );

                        maxHealthAttribute.addTemporaryModifier(activeCurseModifier);

                        if (player.getHealth() > player.getMaxHealth()) {
                            player.setHealth(player.getMaxHealth());
                        }
                    }
                }
            }
        });
    }
}