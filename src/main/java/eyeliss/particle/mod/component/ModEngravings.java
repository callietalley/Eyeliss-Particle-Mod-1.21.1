package eyeliss.particle.mod.component;

import eyeliss.particle.mod.registry.ModItemTags;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.random.Random;
import java.util.*;

public class ModEngravings {
    private static final Map<TagKey<Item>, List<String>> NORMAL_POOLS = new HashMap<>();
    private static final Map<TagKey<Item>, List<String>> BLESSING_POOLS = new HashMap<>();
    private static final Map<TagKey<Item>, List<String>> CURSE_POOLS = new HashMap<>();
    private static final List<String> SINGLE_LEVEL_POOLS = List.of("blessed");

    public static void registerPools() {
        NORMAL_POOLS.put(ModItemTags.WEAPON_ENGRAVING_POOL, List.of("sweeping", "truth", "exorcism", "blessed", "magic_touch"));
        BLESSING_POOLS.put(ModItemTags.WEAPON_ENGRAVING_POOL, List.of());

        NORMAL_POOLS.put(ModItemTags.SWORD_ENGRAVING_POOL, List.of("absorption", "crushing"));

        // --- ARMOR POOL MATRIX ---
        NORMAL_POOLS.put(ModItemTags.ARMOR_ENGRAVING_POOL, List.of("fortitude", "lightweight"));
        BLESSING_POOLS.put(ModItemTags.ARMOR_ENGRAVING_POOL, List.of("hasted"));

        // --- TOOL POOL MATRIX ---
        NORMAL_POOLS.put(ModItemTags.TOOL_ENGRAVING_POOL, List.of("pulverizing"));
        BLESSING_POOLS.put(ModItemTags.TOOL_ENGRAVING_POOL, List.of("ethereal", "dwarven", "shattering"));

        // --- GENERAL STRATIFIED INTERFACE POOLS ---
        NORMAL_POOLS.put(ModItemTags.GENERAL_ENGRAVING_POOL, List.of("sturdy"));
        BLESSING_POOLS.put(ModItemTags.GENERAL_ENGRAVING_POOL, List.of("restoration", "transcendence", "wisdom"));
        CURSE_POOLS.put(ModItemTags.GENERAL_ENGRAVING_POOL, List.of("stagnation", "ruin"));
    }

    public static Map<TagKey<Item>, List<String>> getBlessingPools() {
        return BLESSING_POOLS;
    }

    public static boolean isBlessingOrCurse(String id) {
        if (SINGLE_LEVEL_POOLS.contains(id)) return true;
        return List.of(
                "restoration", "transcendence", "wisdom", "stagnation", "ruin", "baptism",
                "ethereal", "dwarven", "shattering", "hasted"
        ).contains(id);
    }


    public static Optional<String> rollEngravingFor(ItemStack stack, Random random) {
        List<String> specializedNormals = new ArrayList<>();
        List<String> genericNormals = new ArrayList<>();

        List<String> validBlessings = new ArrayList<>();
        List<String> validCurses = new ArrayList<>();

        for (TagKey<Item> tag : NORMAL_POOLS.keySet()) {
            if (stack.isIn(tag)) {
                if (tag.equals(ModItemTags.GENERAL_ENGRAVING_POOL)) {
                    genericNormals.addAll(NORMAL_POOLS.get(tag));
                    if (BLESSING_POOLS.containsKey(tag)) validBlessings.addAll(BLESSING_POOLS.get(tag));
                    if (CURSE_POOLS.containsKey(tag)) validCurses.addAll(CURSE_POOLS.get(tag));
                }
                else if (tag.equals(ModItemTags.WEAPON_ENGRAVING_POOL) || tag.equals(ModItemTags.SWORD_ENGRAVING_POOL)) {
                    specializedNormals.addAll(NORMAL_POOLS.get(tag));
                    if (BLESSING_POOLS.containsKey(tag)) validBlessings.addAll(BLESSING_POOLS.get(tag));
                    if (CURSE_POOLS.containsKey(tag)) validCurses.addAll(CURSE_POOLS.get(tag));
                }
                else {
                    specializedNormals.addAll(NORMAL_POOLS.get(tag));
                }
            }
        }

        float magicRoll = random.nextFloat();
        if (magicRoll < 0.066f && !validBlessings.isEmpty()) {
            return Optional.of(validBlessings.get(random.nextInt(validBlessings.size())));
        }
        if (magicRoll >= 0.066f && magicRoll < 0.10f && !validCurses.isEmpty()) {
            return Optional.of(validCurses.get(random.nextInt(validCurses.size())));
        }

        if (!specializedNormals.isEmpty() && !genericNormals.isEmpty()) {
            if (random.nextFloat() < 0.75f) {
                return Optional.of(specializedNormals.get(random.nextInt(specializedNormals.size())));
            } else {
                return Optional.of(genericNormals.get(random.nextInt(genericNormals.size())));
            }
        }

        List<String> combinedNormals = new ArrayList<>(specializedNormals);
        combinedNormals.addAll(genericNormals);

        if (!combinedNormals.isEmpty()) {
            return Optional.of(combinedNormals.get(random.nextInt(combinedNormals.size())));
        }
        return Optional.empty();
    }
}