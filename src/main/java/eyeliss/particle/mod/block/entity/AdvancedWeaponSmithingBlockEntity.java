package eyeliss.particle.mod.block.entity;

import eyeliss.particle.mod.network.BlockPosPayload;
import eyeliss.particle.mod.recipe.ModRecipes;
import eyeliss.particle.mod.recipe.WeaponSmithingRecipe;
import eyeliss.particle.mod.recipe.HardLimitedSmithingRecipe;
import eyeliss.particle.mod.recipe.CraftCounterState;
import eyeliss.particle.mod.recipe.HardLimitedRecipe;
import eyeliss.particle.mod.screen.AdvancedWeaponSmithingScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdvancedWeaponSmithingBlockEntity extends BlockEntity implements ImplementedInventory, ExtendedScreenHandlerFactory<BlockPosPayload> {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(8, ItemStack.EMPTY);
    private int selectedRecipeIndex = 0;
    protected final PropertyDelegate propertyDelegate;
    private PlayerEntity activeUser = null;

    public AdvancedWeaponSmithingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_WEAPON_SMITHING_ENTITY, pos, state);
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return index == 0 ? selectedRecipeIndex : 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    selectedRecipeIndex = value;

                    AdvancedWeaponSmithingBlockEntity.this.updateRecipeOutput();

                    if (AdvancedWeaponSmithingBlockEntity.this.world != null && !AdvancedWeaponSmithingBlockEntity.this.world.isClient) {
                        AdvancedWeaponSmithingBlockEntity.this.markDirty();
                        AdvancedWeaponSmithingBlockEntity.this.world.updateListeners(
                                AdvancedWeaponSmithingBlockEntity.this.pos,
                                AdvancedWeaponSmithingBlockEntity.this.getCachedState(),
                                AdvancedWeaponSmithingBlockEntity.this.getCachedState(),
                                3
                        );
                    }
                }
            }

            @Override
            public int size() { return 1; }
        };
    }

    public void returnMaterialsToPlayer(PlayerEntity player) {
        if (this.world == null || this.world.isClient || player == null) return;

        for (int i = 0; i < 7; i++) {
            ItemStack stackInSlot = this.getStack(i);
            if (!stackInSlot.isEmpty()) {
                player.getInventory().offerOrDrop(stackInSlot);
                this.inventory.set(i, ItemStack.EMPTY);
            }
        }
        this.markDirty();
    }

    @Override
    public void onOpen(PlayerEntity player) {
        this.activeUser = player;
    }

    @Override
    public void onClose(PlayerEntity player) {
        returnMaterialsToPlayer(player);
        this.activeUser = null;
    }

    public PropertyDelegate getPropertyDelegate() { return this.propertyDelegate; }
    @Override
    public DefaultedList<ItemStack> getItems() { return this.inventory; }

    public void updateRecipeOutput() {
        if (this.world == null || this.world.isClient) return;

        List<RecipeEntry<?>> recipes = getAllAvailableSmithingRecipes();
        if (selectedRecipeIndex < 0 || selectedRecipeIndex >= recipes.size()) {
            this.setStack(7, ItemStack.EMPTY);
            return;
        }

        var activeRecipe = recipes.get(selectedRecipeIndex).value();
        boolean craftable;
        ItemStack outputStack;

        if (activeRecipe instanceof HardLimitedSmithingRecipe limited) {
            craftable = canCraft(limited.ingredients());
            outputStack = limited.output();
        } else if (activeRecipe instanceof WeaponSmithingRecipe standard) {
            craftable = canCraft(standard.ingredients());
            outputStack = standard.output();
        } else {
            craftable = false;
            outputStack = ItemStack.EMPTY;
        }

        if (craftable) {
            this.inventory.set(7, outputStack.copy());
        } else {
            this.inventory.set(7, ItemStack.EMPTY);
        }
        this.markDirty();
    }

    private boolean canCraft(List<ItemStack> requirements) {
        if (requirements == null || requirements.isEmpty()) return false;
        for (int i = 0; i < requirements.size() && i < 7; i++) {
            ItemStack required = requirements.get(i);
            ItemStack inSlot = this.getStack(i);

            if (inSlot.isEmpty() || !ItemStack.areItemsEqual(inSlot, required) || inSlot.getCount() < required.getCount()) {
                return false;
            }
        }
        return true;
    }

    public void consumeMaterials() {
        List<RecipeEntry<?>> recipes = getAllAvailableSmithingRecipes();
        if (selectedRecipeIndex < 0 || selectedRecipeIndex >= recipes.size()) return;

        var activeEntry = recipes.get(selectedRecipeIndex);
        var activeRecipe = activeEntry.value();

        List<ItemStack> requirements = (activeRecipe instanceof HardLimitedSmithingRecipe limited) ? limited.ingredients() : ((WeaponSmithingRecipe)activeRecipe).ingredients();
        for (int i = 0; i < requirements.size() && i < 7; i++) {
            this.removeStack(i, requirements.get(i).getCount());
        }

        updateRecipeOutput();
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.getItems().set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        this.markDirty();
        if (slot < 7) {
            this.updateRecipeOutput();
        }
        if (this.world != null && !this.world.isClient) {
            this.world.updateListeners(this.pos, getCachedState(), getCachedState(), 3);
        }
    }

    public List<RecipeEntry<?>> getAllAvailableSmithingRecipes() {
        if (this.world == null || this.world.getServer() == null) return List.of();

        CraftCounterState limitState = CraftCounterState.getServerState(this.world.getServer());
        PlayerEntity currentPlayer = this.activeUser;

        return this.world.getRecipeManager()
                .values()
                .stream()
                .filter(entry -> entry.value().getType() == ModRecipes.WEAPON_SMITHING_TYPE)
                .filter(entry -> {
                    var recipe = entry.value();
                    if (recipe instanceof HardLimitedSmithingRecipe limited) {
                        String recipeId = entry.id().toString();

                        int globalCrafted = limitState.getGlobalCount(recipeId);
                        int playerCrafted = currentPlayer != null ? limitState.getPlayerCount(recipeId, currentPlayer.getUuid()) : 0;

                        if (globalCrafted >= limited.globalLimit() || playerCrafted >= limited.playerLimit()) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted((entry1, entry2) -> {
                    int order1 = (entry1.value() instanceof HardLimitedSmithingRecipe l) ? l.order() : ((WeaponSmithingRecipe)entry1.value()).order();
                    int order2 = (entry2.value() instanceof HardLimitedSmithingRecipe l) ? l.order() : ((WeaponSmithingRecipe)entry2.value()).order();

                    int orderComparison = Integer.compare(order1, order2);
                    if (orderComparison != 0) return orderComparison;

                    String name1 = entry1.value().getResult(null).getName().getString();
                    String name2 = entry2.value().getResult(null).getName().getString();
                    return name1.compareToIgnoreCase(name2);
                })
                .toList();
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        var totalRecipes = this.world.getRecipeManager().values().stream()
                .filter(entry -> entry.value().getType() == ModRecipes.WEAPON_SMITHING_TYPE)
                .sorted((entry1, entry2) -> {
                    int order1 = (entry1.value() instanceof HardLimitedSmithingRecipe l) ? l.order() : ((WeaponSmithingRecipe)entry1.value()).order();
                    int order2 = (entry2.value() instanceof HardLimitedSmithingRecipe l) ? l.order() : ((WeaponSmithingRecipe)entry2.value()).order();
                    int comp = Integer.compare(order1, order2);
                    if (comp != 0) return comp;
                    return entry1.value().getResult(null).getName().getString().compareToIgnoreCase(entry2.value().getResult(null).getName().getString());
                })
                .toList();

        var allowedRecipes = getAllAvailableSmithingRecipes();

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < totalRecipes.size(); i++) {
            if (allowedRecipes.contains(totalRecipes.get(i))) {
                validIndices.add(i);
            }
        }

        return new BlockPosPayload(this.pos, validIndices);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.advanced_weapon_smithing");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new AdvancedWeaponSmithingScreenHandler(syncId, playerInventory, this, this.getPropertyDelegate());
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        Inventories.writeNbt(nbt, this.inventory, registries);
        nbt.putInt("SelectedRecipe", this.selectedRecipeIndex);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        Inventories.readNbt(nbt, this.inventory, registries);
        this.selectedRecipeIndex = nbt.getInt("SelectedRecipe");
    }
}
