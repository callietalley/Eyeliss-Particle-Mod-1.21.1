package eyeliss.particle.mod.recipe;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import java.util.HashMap;
import java.util.Map;

public class CraftCounterState extends PersistentState {
    // Stores recipe ID string -> craft count integers
    private final Map<String, Integer> recipeCounts = new HashMap<>();

    public CraftCounterState() {}

    public int getCraftCount(String recipeId) {
        return recipeCounts.getOrDefault(recipeId, 0);
    }

    public void incrementCraftCount(String recipeId) {
        recipeCounts.put(recipeId, getCraftCount(recipeId) + 1);
        this.markDirty();
    }

    public void clearAllCounts() {
        this.recipeCounts.clear();
        this.markDirty();
    }

    public void clearSpecificCount(String recipeId) {
        this.recipeCounts.remove(recipeId);
        this.markDirty(); // Saves changes directly to the server folder state files
    }

    public void setSpecificCount(String recipeId, int amount) {
        this.recipeCounts.put(recipeId, amount);
        this.markDirty(); // Save state update immediately to your world file
    }

    public static CraftCounterState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        CraftCounterState state = new CraftCounterState();
        NbtCompound countsGroup = nbt.getCompound("recipeCounts");
        for (String key : countsGroup.getKeys()) {
            state.recipeCounts.put(key, countsGroup.getInt(key));
        }
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound countsGroup = new NbtCompound();
        recipeCounts.forEach(countsGroup::putInt);
        nbt.put("recipeCounts", countsGroup);
        return nbt;
    }

    public static CraftCounterState getServerState(MinecraftServer server) {
        var overworld = server.getWorld(World.OVERWORLD);
        if (overworld == null) {
            // Safe fallback to prevent IDE NullPointerException warnings
            return new CraftCounterState();
        }

        PersistentStateManager manager = overworld.getPersistentStateManager();
        Type<CraftCounterState> type = new Type<>(
                CraftCounterState::new,
                CraftCounterState::fromNbt,
                DataFixTypes.LEVEL
        );
        return manager.getOrCreate(type, "dynamic_craft_counters");
    }
}
