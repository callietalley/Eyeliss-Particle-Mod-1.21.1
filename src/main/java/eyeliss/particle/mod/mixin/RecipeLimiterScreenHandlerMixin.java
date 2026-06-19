package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.recipe.CraftCounterState;
import eyeliss.particle.mod.recipe.HardLimitedRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public abstract class RecipeLimiterScreenHandlerMixin extends ScreenHandler {

    @Shadow @Final private RecipeInputInventory input;
    @Shadow @Final private CraftingResultInventory result;
    @Shadow @Final private PlayerEntity player;

    protected RecipeLimiterScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "onContentChanged", at = @At("TAIL"))
    private void eyelisspartmod$onContentChanged(net.minecraft.inventory.Inventory inventory, CallbackInfo ci) {
        this.eyelisspartmod$enforceLimits();
    }

    @Inject(method = "updateResult", at = @At("TAIL"))
    private static void eyelisspartmod$onRecipeBookUpdate(
            ScreenHandler handler, World world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, RecipeEntry<CraftingRecipe> recipe, CallbackInfo ci
    ) {

        if (handler instanceof RecipeLimiterScreenHandlerMixin mixin) {
            mixin.eyelisspartmod$enforceLimits();
        }
    }

    @Unique
    private void eyelisspartmod$enforceLimits() {
        World world = this.player.getWorld();

        if (!world.isClient() && world.getServer() != null) {
            MinecraftServer server = world.getServer();

            var match = server.getRecipeManager()
                    .getFirstMatch(net.minecraft.recipe.RecipeType.CRAFTING, this.input.createRecipeInput(), world);

            if (match.isPresent() && match.get().value() instanceof HardLimitedRecipe limitedRecipe) {
                CraftCounterState state = CraftCounterState.getServerState(server);
                String recipeId = match.get().id().toString();

                int globalCount = state.getGlobalCount(recipeId);
                int playerCount = state.getPlayerCount(recipeId, this.player.getUuid());

                if (globalCount >= limitedRecipe.getGlobalLimit() || playerCount >= limitedRecipe.getPlayerLimit()) {
                    this.result.setStack(0, HardLimitedRecipe.getDepletedPlaceholder());
                    this.sendContentUpdates();
                }
            }
        }
    }
}
