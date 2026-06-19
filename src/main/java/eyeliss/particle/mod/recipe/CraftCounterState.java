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
import java.util.UUID;

public class CraftCounterState extends PersistentState {
    private final Map<String, Integer> globalCounts = new HashMap<>();
    private final Map<String, Map<String, Integer>> playerCounts = new HashMap<>();

    public CraftCounterState() {}

    public int getGlobalCount(String recipeId) {
        return globalCounts.getOrDefault(recipeId, 0);
    }

    public int getPlayerCount(String recipeId, UUID playerUuid) {
        Map<String, Integer> playerMap = playerCounts.get(recipeId);
        if (playerMap == null) return 0;
        return playerMap.getOrDefault(playerUuid.toString(), 0);
    }

    public void increaseCounts(String recipeId, UUID playerUuid, int amount) {
        globalCounts.put(recipeId, getGlobalCount(recipeId) + amount);

        playerCounts.computeIfAbsent(recipeId, k -> new HashMap<>());
        Map<String, Integer> playerMap = playerCounts.get(recipeId);
        String uuidStr = playerUuid.toString();
        playerMap.put(uuidStr, playerMap.getOrDefault(uuidStr, 0) + amount);

        this.markDirty();
    }

    public void clearAllCounts() {
        this.globalCounts.clear();
        this.playerCounts.clear();
        this.markDirty();
    }

    public void clearSpecificCount(String recipeId) {
        this.globalCounts.remove(recipeId);
        this.playerCounts.remove(recipeId);
        this.markDirty();
    }

    public void setPlayerCount(String recipeId, UUID playerUuid, int amount) {
        this.playerCounts.computeIfAbsent(recipeId, k -> new HashMap<>());
        this.playerCounts.get(recipeId).put(playerUuid.toString(), amount);
        this.markDirty();
    }

    public static CraftCounterState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        CraftCounterState state = new CraftCounterState();

        NbtCompound globals = nbt.getCompound("globalCounts");
        for (String key : globals.getKeys()) {
            state.globalCounts.put(key, globals.getInt(key));
        }

        NbtCompound playersRoot = nbt.getCompound("playerCounts");
        for (String recipeKey : playersRoot.getKeys()) {

            NbtCompound playerGroup = playersRoot.getCompound(recipeKey);

            Map<String, Integer> playerMap = new HashMap<>();
            for (String playerUuidStr : playerGroup.getKeys()) {
                playerMap.put(playerUuidStr, playerGroup.getInt(playerUuidStr));
            }
            state.playerCounts.put(recipeKey, playerMap);
        }
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound globals = new NbtCompound();
        globalCounts.forEach(globals::putInt);
        nbt.put("globalCounts", globals);

        NbtCompound playersRoot = new NbtCompound();
        playerCounts.forEach((recipeId, playerMap) -> {
            NbtCompound playerGroup = new NbtCompound();
            playerMap.forEach(playerGroup::putInt);
            playersRoot.put(recipeId, playerGroup);
        });
        nbt.put("playerCounts", playersRoot);

        return nbt;
    }

    public static CraftCounterState getServerState(MinecraftServer server) {
        var overworld = server.getWorld(World.OVERWORLD);
        if (overworld == null) return new CraftCounterState();
        PersistentStateManager manager = overworld.getPersistentStateManager();
        Type<CraftCounterState> type = new Type<>(CraftCounterState::new, CraftCounterState::fromNbt, DataFixTypes.LEVEL);
        return manager.getOrCreate(type, "dual_craft_counters");
    }
}
