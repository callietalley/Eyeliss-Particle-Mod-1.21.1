package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.recipe.CraftCounterState;
import eyeliss.particle.mod.recipe.HardLimitedRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

                            int currentCount = state.getCraftCount(recipeId);
                            int maxLimit = limitedRecipe.getRecipeLimit();

                            if (stack.getItem() != limitedRecipe.getResult(player.getWorld().getRegistryManager()).getItem()) {
                                return;
                            }

                            if (currentCount >= maxLimit) {
                                return;
                            }

                            state.incrementCraftCount(recipeId);
                            int updatedCount = currentCount + 1;

                            server.getPlayerManager().broadcast(
                                    Text.literal("A ")
                                            .append(HardLimitedRecipe.getTranslatableName(recipeId).formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                                            .append(" has been crafted! Only " + (maxLimit - updatedCount) + " more can be crafted."),
                                    true
                            );

                            if (player instanceof ServerPlayerEntity serverPlayer) {
                                serverPlayer.getWorld().playSound(
                                        null,
                                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                                        SoundCategory.MASTER,
                                        1.0F, 1.0F
                                );
                            }

                            for (ServerPlayerEntity onlinePlayer : server.getPlayerManager().getPlayerList()) {
                                onlinePlayer.getWorld().playSound(
                                        null,
                                        onlinePlayer.getX(), onlinePlayer.getY(), onlinePlayer.getZ(),
                                        SoundEvents.ENTITY_ENDER_DRAGON_GROWL,
                                        SoundCategory.MASTER,
                                        1.0F, 1.0F
                                );
                            }

                            if (player.currentScreenHandler instanceof CraftingScreenHandler craftingHandler) {
                                try {
                                    java.lang.reflect.Field contextField = CraftingScreenHandler.class.getDeclaredField("context");
                                    contextField.setAccessible(true);
                                    ScreenHandlerContext context = (ScreenHandlerContext) contextField.get(craftingHandler);

                                    context.run((world, pos) -> {
                                        if (world instanceof ServerWorld serverWorld) {
                                            double pX = pos.getX() + 0.5;
                                            double pY = pos.getY() + 1.1;
                                            double pX_offset = pos.getZ() + 0.5;

                                            serverWorld.spawnParticles(
                                                    ParticleTypes.FIREWORK,
                                                    pX, pY, pX_offset,
                                                    35,
                                                    0.2, 0.1, 0.2,
                                                    0.15
                                            );

                                            serverWorld.playSound(null, pX, pY, pX_offset, SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.BLOCKS, 0.8F, 1.2F);
                                        }
                                    });
                                } catch (Exception e) {
                                    if (player.getWorld() instanceof ServerWorld serverWorld) {
                                        serverWorld.spawnParticles(ParticleTypes.FIREWORK, player.getX(), player.getY() + 1.5, player.getZ(), 25, 0.3, 0.3, 0.3, 0.1);
                                    }
                                }
                            }
                        }
                    });
        }
    }
}
