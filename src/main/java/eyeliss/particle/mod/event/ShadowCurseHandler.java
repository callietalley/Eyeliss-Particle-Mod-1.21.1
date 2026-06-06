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
            // Optimization: Only scan inventories once every 10 ticks (0.5 seconds)
            tickCounter++;
            if (tickCounter % 10 != 0) return;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // Skip tracking if the player is dead or currently spectating
                if (player.isSpectator() || !player.isAlive()) continue;

                int cursedItemCount = 0;

                // 1. Loop through all 36 slots of the player's primary inventory grid
                for (int slot = 0; slot < player.getInventory().main.size(); slot++) {
                    ItemStack stack = player.getInventory().main.get(slot);

                    if (stack != null && !stack.isEmpty()) {
                        // Check if the item carries our custom registered data tag
                        if (stack.getOrDefault(ModComponents.IS_CURSED, false)) {
                            // Add the count of items in this stack (e.g., if stacked, they count separately)
                            cursedItemCount += stack.getCount();
                        }
                    }
                }

                // 2. Also check the offhand slot explicitly (since it's not part of the 'main' inventory array)
                ItemStack offHandStack = player.getOffHandStack();
                if (offHandStack != null && !offHandStack.isEmpty()) {
                    if (offHandStack.getOrDefault(ModComponents.IS_CURSED, false)) {
                        cursedItemCount += offHandStack.getCount();
                    }
                }

                // Enforce your maximum cap boundary of 4 items total (-8 hearts)
                if (cursedItemCount > 4) {
                    cursedItemCount = 4;
                }

                // Fetch player's dynamic maximum health modifier attribute registry
                EntityAttributeInstance maxHealthAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                if (maxHealthAttribute != null) {
                    // Strip older instances of the curse modifier before recalculating
                    maxHealthAttribute.removeModifier(DYNAMIC_HEALTH_CURSE_ID);

                    if (cursedItemCount > 0) {
                        // Calculate health deduction (-4.0 HP / 2 full hearts per curse count)
                        double healthReductionValue = cursedItemCount * -4.0;

                        EntityAttributeModifier activeCurseModifier = new EntityAttributeModifier(
                                DYNAMIC_HEALTH_CURSE_ID,
                                healthReductionValue,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        );

                        // Safely apply the dynamic health attribute modifier temporarily
                        maxHealthAttribute.addTemporaryModifier(activeCurseModifier);

                        // If player's current health lands above their new maximum cap, trim it down cleanly
                        if (player.getHealth() > player.getMaxHealth()) {
                            player.setHealth(player.getMaxHealth());
                        }
                    }
                }
            }
        });
    }
}