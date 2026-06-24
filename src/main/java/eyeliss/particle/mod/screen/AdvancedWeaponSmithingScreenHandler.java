package eyeliss.particle.mod.screen;

import eyeliss.particle.mod.block.entity.AdvancedWeaponSmithingBlockEntity;
import eyeliss.particle.mod.network.BlockPosPayload;
import eyeliss.particle.mod.recipe.WeaponSmithingRecipe;
import eyeliss.particle.mod.recipe.HardLimitedSmithingRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.recipe.RecipeEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdvancedWeaponSmithingScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    private final PlayerInventory playerInventory;

    private final List<Integer> clientAllowedIndices = new ArrayList<>();

    public AdvancedWeaponSmithingScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        this(syncId, playerInventory, new SimpleInventory(8), new ArrayPropertyDelegate(1));
        this.clientAllowedIndices.addAll(payload.activeRecipeIndices());
    }

    public AdvancedWeaponSmithingScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate delegate) {
        super(ModScreenHandlers.ADVANCED_WEAPON_SMITHING_HANDLER, syncId);
        checkSize(inventory, 8);
        this.inventory = inventory;
        this.propertyDelegate = delegate;
        this.playerInventory = playerInventory;
        this.addProperties(delegate);

        for (int i = 0; i < 7; i++) {
            final int slotIndex = i;
            this.addSlot(new Slot(inventory, slotIndex, 0, 135) {
                @Override
                public boolean isEnabled() {
                    return slotIndex < getRequiredIngredientCount();
                }

                @Override
                public boolean canInsert(ItemStack stack) {
                    if (!isEnabled()) return false;
                    if (playerInventory.player.getWorld() == null) return false;

                    List<RecipeEntry<?>> recipes = getFilteredRecipesList();
                    int selection = getSelectedRecipeIndex();
                    if (selection >= 0 && selection < recipes.size()) {
                        var activeRecipe = recipes.get(selection).value();
                        List<ItemStack> ingredients = (activeRecipe instanceof HardLimitedSmithingRecipe l) ? l.ingredients() : ((WeaponSmithingRecipe)activeRecipe).ingredients();
                        if (slotIndex < ingredients.size()) {
                            return ItemStack.areItemsEqual(stack, ingredients.get(slotIndex));
                        }
                    }
                    return false;
                }
            });
        }

        this.addSlot(new Slot(inventory, 7, 215, 135) {
            @Override
            public boolean canInsert(ItemStack stack) { return false; }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                super.onTakeItem(player, stack);

                if (player.getWorld() != null) {
                    player.getWorld().playSound(
                            null, player.getBlockPos(),
                            net.minecraft.sound.SoundEvents.BLOCK_ANVIL_USE,
                            net.minecraft.sound.SoundCategory.BLOCKS,
                            1.0F, 1.3F
                    );
                }

                if (inventory instanceof AdvancedWeaponSmithingBlockEntity smithingEntity) {
                    smithingEntity.consumeMaterials();
                }
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 40 + col * 18, 163 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 40 + col * 18, 221));
        }
    }

    public List<RecipeEntry<?>> getFilteredRecipesList() {
        if (playerInventory.player.getWorld() == null) return List.of();

        var totalList = playerInventory.player.getWorld().getRecipeManager()
                .values().stream()
                .filter(e -> e.value().getType() == eyeliss.particle.mod.recipe.ModRecipes.WEAPON_SMITHING_TYPE)
                .sorted((entry1, entry2) -> {
                    int order1 = (entry1.value() instanceof HardLimitedSmithingRecipe l) ? l.order() : ((WeaponSmithingRecipe)entry1.value()).order();
                    int order2 = (entry2.value() instanceof HardLimitedSmithingRecipe l) ? l.order() : ((WeaponSmithingRecipe)entry2.value()).order();
                    int comp = Integer.compare(order1, order2);
                    if (comp != 0) return comp;
                    return entry1.value().getResult(null).getName().getString().compareToIgnoreCase(entry2.value().getResult(null).getName().getString());
                })
                .toList();

        if (playerInventory.player.getWorld().isClient() == false) return totalList;

        List<RecipeEntry<?>> allowedList = new ArrayList<>();
        for (int i = 0; i < totalList.size(); i++) {
            if (this.clientAllowedIndices.isEmpty() || this.clientAllowedIndices.contains(i)) {
                allowedList.add(totalList.get(i));
            }
        }
        return allowedList;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (this.inventory instanceof AdvancedWeaponSmithingBlockEntity smithingEntity) {
            smithingEntity.returnMaterialsToPlayer(player);
        }
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (this.inventory instanceof AdvancedWeaponSmithingBlockEntity smithingEntity) {
            smithingEntity.updateRecipeOutput();
            if (!playerInventory.player.getWorld().isClient()) {
                this.sendContentUpdates();
            }
        }
    }

    @Override
    public void onSlotClick(int slotId, int button, net.minecraft.screen.slot.SlotActionType actionType, PlayerEntity player) {
        if (slotId == 7 && !player.getWorld().isClient() && player.getWorld().getServer() != null) {
            net.minecraft.server.MinecraftServer server = player.getWorld().getServer();
            Slot resultSlot = this.getSlot(7);

            if (resultSlot != null && resultSlot.hasStack()) {
                ItemStack stack = resultSlot.getStack();

                List<RecipeEntry<?>> masterRecipes = player.getWorld().getRecipeManager()
                        .values().stream()
                        .filter(e -> e.value().getType() == eyeliss.particle.mod.recipe.ModRecipes.WEAPON_SMITHING_TYPE)
                        .sorted((entry1, entry2) -> {
                            int order1 = (entry1.value() instanceof HardLimitedSmithingRecipe l) ? l.order() : ((WeaponSmithingRecipe)entry1.value()).order();
                            int order2 = (entry2.value() instanceof HardLimitedSmithingRecipe l) ? l.order() : ((WeaponSmithingRecipe)entry2.value()).order();
                            int orderComparison = Integer.compare(order1, order2);
                            if (orderComparison != 0) return orderComparison;
                            return entry1.value().getResult(null).getName().getString().compareToIgnoreCase(entry2.value().getResult(null).getName().getString());
                        })
                        .toList();

                int selection = this.getSelectedRecipeIndex();

                if (selection >= 0 && selection < masterRecipes.size()) {
                    RecipeEntry<?> activeEntry = masterRecipes.get(selection);
                    var activeRecipe = activeEntry.value();

                    net.minecraft.util.Identifier serializerId = net.minecraft.registry.Registries.RECIPE_SERIALIZER.getId(activeRecipe.getSerializer());
                    String serializerPath = serializerId != null ? serializerId.toString() : "";
                    boolean isHardLimited = serializerPath.contains("hard_limited_smithing") || activeRecipe instanceof HardLimitedSmithingRecipe;

                    if (isHardLimited) {
                        String recipeId = activeEntry.id().toString();
                        eyeliss.particle.mod.recipe.CraftCounterState state = eyeliss.particle.mod.recipe.CraftCounterState.getServerState(server);

                        int currentGlobalCount = state.getGlobalCount(recipeId);
                        int maxGlobalThreshold = 999;
                        if (activeRecipe instanceof HardLimitedSmithingRecipe limited) {
                            maxGlobalThreshold = limited.globalLimit();
                        } else {
                            try {
                                java.lang.reflect.Method globalMethod = activeRecipe.getClass().getMethod("globalLimit");
                                maxGlobalThreshold = (int) globalMethod.invoke(activeRecipe);
                            } catch (Exception ignored) {}
                        }

                        if (currentGlobalCount >= maxGlobalThreshold) {
                            this.sendContentUpdates();
                            return;
                        }

                        int countToCraft = (actionType == net.minecraft.screen.slot.SlotActionType.QUICK_MOVE) ? 1 : stack.getCount();

                        state.increaseCounts(recipeId, player.getUuid(), countToCraft);
                        int updatedGlobalCount = state.getGlobalCount(recipeId);

                        if (!stack.isEmpty() && !stack.contains(eyeliss.particle.mod.component.TrackingID.TRACKING_ID)) {
                            String uniqueId = UUID.randomUUID().toString();
                            stack.set(eyeliss.particle.mod.component.TrackingID.TRACKING_ID, uniqueId);
                        }

                        int remainingPool = maxGlobalThreshold - updatedGlobalCount;
                        server.getPlayerManager().broadcast(
                                net.minecraft.text.Text.literal("[Global Forge] ").copy().formatted(net.minecraft.util.Formatting.GOLD)
                                        .append(eyeliss.particle.mod.recipe.HardLimitedRecipe.getTranslatableName(recipeId).copy().formatted(net.minecraft.util.Formatting.LIGHT_PURPLE))
                                        .append(" forged! Server pool left: " + remainingPool),
                                true
                        );

                    }
                }
            }
        }

        super.onSlotClick(slotId, button, actionType, player);

        if (!player.getWorld().isClient()) {
            this.sendContentUpdates();
        }
    }

    public int getSelectedRecipeIndex() { return this.propertyDelegate.get(0); }
    public PropertyDelegate getPropertyDelegate() { return this.propertyDelegate; }

    public int getRequiredIngredientCount() {
        List<RecipeEntry<?>> recipes = getFilteredRecipesList();
        int selection = this.getSelectedRecipeIndex();
        if (selection >= 0 && selection < recipes.size()) {
            var activeRecipe = recipes.get(selection).value();
            if (activeRecipe instanceof HardLimitedSmithingRecipe limited) return limited.ingredients().size();
            if (activeRecipe instanceof WeaponSmithingRecipe standard) return standard.ingredients().size();
        }
        return 0;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (invSlot == 7) {
                ItemStack singleCraftStack = originalStack.copy();
                singleCraftStack.setCount(1);

                if (!this.insertItem(singleCraftStack, 8, 44, true)) {
                }

                slot.onTakeItem(player, singleCraftStack);

                slot.setStack(ItemStack.EMPTY);

                if (!player.getWorld().isClient() && this.inventory instanceof AdvancedWeaponSmithingBlockEntity smithingEntity) {
                    var allowedRecipesAfterCraft = smithingEntity.getAllAvailableSmithingRecipes();

                    List<RecipeEntry<?>> masterRecipes = player.getWorld().getRecipeManager()
                            .values().stream()
                            .filter(e -> e.value().getType() == eyeliss.particle.mod.recipe.ModRecipes.WEAPON_SMITHING_TYPE)
                            .sorted((entry1, entry2) -> {
                                int order1 = (entry1.value() instanceof HardLimitedSmithingRecipe l) ? l.order() : ((WeaponSmithingRecipe)entry1.value()).order();
                                int order2 = (entry2.value() instanceof HardLimitedSmithingRecipe l) ? l.order() : ((WeaponSmithingRecipe)entry2.value()).order();
                                int orderComparison = Integer.compare(order1, order2);
                                if (orderComparison != 0) return orderComparison;
                                return entry1.value().getResult(null).getName().getString().compareToIgnoreCase(entry2.value().getResult(null).getName().getString());
                            })
                            .toList();

                    int nextValidIndex = 0;

                    if (!allowedRecipesAfterCraft.isEmpty()) {
                        var topEntry = allowedRecipesAfterCraft.get(0);
                        for (int i = 0; i < masterRecipes.size(); i++) {
                            if (masterRecipes.get(i).id().equals(topEntry.id())) {
                                nextValidIndex = i;
                                break;
                            }
                        }
                    }
                    this.getPropertyDelegate().set(0, nextValidIndex);
                    smithingEntity.updateRecipeOutput();
                }
                if (this.inventory instanceof AdvancedWeaponSmithingBlockEntity smithingEntity) {
                    smithingEntity.updateRecipeOutput();
                }
                this.sendContentUpdates();

                ItemStack resultToken = newStack.copy();
                resultToken.setCount(1);
                return resultToken;
            }
            if (invSlot < 7) {
                if (!this.insertItem(originalStack, 8, 44, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(originalStack, newStack);
            }
            else {
                int activeInputs = this.getRequiredIngredientCount();
                boolean inserted = false;

                for (int i = 0; i < activeInputs; i++) {
                    Slot targetSlot = this.slots.get(i);
                    if (targetSlot.canInsert(originalStack)) {
                        if (this.insertItem(originalStack, i, i + 1, false)) {
                            inserted = true;
                            break;
                        }
                    }
                }
                if (!inserted) return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, originalStack);
        }
        return newStack;
    }
}


