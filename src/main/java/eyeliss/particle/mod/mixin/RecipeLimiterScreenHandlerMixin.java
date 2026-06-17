package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.recipe.CraftCounterState;
import eyeliss.particle.mod.recipe.HardLimitedRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public abstract class RecipeLimiterScreenHandlerMixin extends ScreenHandler {

    @Shadow @Final private RecipeInputInventory input;
    @Shadow @Final private CraftingResultInventory result;
    @Shadow @Final private PlayerEntity player; // Added to capture world references safely

    protected RecipeLimiterScreenHandlerMixin(net.minecraft.screen.ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "onContentChanged", at = @At("TAIL"))
    private void eyelisspartmod$onContentChanged(Inventory inventory, CallbackInfo ci) {
        World world = this.player.getWorld();

        if (!world.isClient() && world.getServer() != null) {
            MinecraftServer server = world.getServer();

            var match = server.getRecipeManager()
                    .getFirstMatch(RecipeType.CRAFTING, this.input.createRecipeInput(), world);

            if (match.isPresent() && match.get().value() instanceof HardLimitedRecipe limitedRecipe) {
                CraftCounterState state = CraftCounterState.getServerState(server);
                String recipeId = match.get().id().toString();

                if (state.getCraftCount(recipeId) >= limitedRecipe.getRecipeLimit()) {
                    this.result.setStack(0, HardLimitedRecipe.getDepletedPlaceholder());

                    this.sendContentUpdates();
                }
            }
        }
    }
}
