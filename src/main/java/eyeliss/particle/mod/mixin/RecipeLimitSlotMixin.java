package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.recipe.CraftCounterState;
import eyeliss.particle.mod.recipe.HardLimitedRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eyeliss.particle.mod.component.TrackingID.TRACKING_ID;

@Mixin(CraftingResultSlot.class)
public class RecipeLimitSlotMixin {
    @Shadow @Final private RecipeInputInventory input;

    @Inject(method = "onTakeItem", at = @At("HEAD"))
    private void eyelisspartmod$onTakeItem(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (!player.getWorld().isClient() && player.getWorld().getServer() != null) {
            MinecraftServer server = player.getWorld().getServer();

            server.getRecipeManager().getFirstMatch(net.minecraft.recipe.RecipeType.CRAFTING, input.createRecipeInput(), player.getWorld())
                    .ifPresent(recipeEntry -> {
                        if (recipeEntry.value() instanceof HardLimitedRecipe limitedRecipe) {
                            String recipeId = recipeEntry.id().toString();
                            CraftCounterState state = CraftCounterState.getServerState(server);

                            int currentGlobal = state.getGlobalCount(recipeId);
                            int currentPlayer = state.getPlayerCount(recipeId, player.getUuid());

                            if (stack.getItem() != limitedRecipe.getResult(player.getWorld().getRegistryManager()).getItem())
                                return;
                            if (currentGlobal >= limitedRecipe.getGlobalLimit() || currentPlayer >= limitedRecipe.getPlayerLimit())
                                return;

                            int craftAmount = stack.getCount();
                            state.increaseCounts(recipeId, player.getUuid(), craftAmount);

                            int newGlobal = currentGlobal + craftAmount;

                            if (!stack.isEmpty() && !stack.contains(TRACKING_ID)) {
                                String uniqueItemId = java.util.UUID.randomUUID().toString();
                                stack.set(TRACKING_ID, uniqueItemId);
                            }

                            server.getPlayerManager().broadcast(
                                    Text.literal("[Global] ").copy().formatted(Formatting.GOLD)
                                            .append(HardLimitedRecipe.getTranslatableName(recipeId).copy().formatted(Formatting.LIGHT_PURPLE))
                                            .append(" crafted! Server pool left: " + (limitedRecipe.getGlobalLimit() - newGlobal)),
                                    true
                            );
                        }
                    });
        }
    }
}
