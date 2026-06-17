package eyeliss.particle.mod.util;

import net.minecraft.entity.LivingEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OverhealthTracker {
    private static final Map<UUID, Float> CURRENT_SHIELDS = new HashMap<>();
    private static final Map<UUID, Float> MAX_SHIELDS = new HashMap<>();
    private static final Map<UUID, Integer> HISTORICAL_DURATIONS = new HashMap<>();

    public static void grantOverhealth(LivingEntity entity, int amplifier) {
        if (entity == null) return;

        float baseMaxHealth = entity.getMaxHealth();
        float scaledShield = baseMaxHealth * (amplifier + 1);

        UUID uuid = entity.getUuid();
        CURRENT_SHIELDS.put(uuid, scaledShield);
        MAX_SHIELDS.put(uuid, scaledShield);
    }

    public static float getOverhealth(LivingEntity entity) {
        return CURRENT_SHIELDS.getOrDefault(entity.getUuid(), 0.0f);
    }

    public static float getMaxOverhealth(LivingEntity entity) {
        return MAX_SHIELDS.getOrDefault(entity.getUuid(), 0.0f);
    }

    public static void setOverhealth(LivingEntity entity, float amount) {
        UUID uuid = entity.getUuid();
        if (amount <= 0.0f) {
            CURRENT_SHIELDS.remove(uuid);
            MAX_SHIELDS.remove(uuid);
            HISTORICAL_DURATIONS.remove(uuid);
        } else {
            CURRENT_SHIELDS.put(uuid, amount);
        }
    }

    public static boolean hasOverhealth(LivingEntity entity) {
        return CURRENT_SHIELDS.containsKey(entity.getUuid());
    }

    public static int getHistoricalDuration(LivingEntity entity) {
        return HISTORICAL_DURATIONS.getOrDefault(entity.getUuid(), 0);
    }

    public static void setHistoricalDuration(LivingEntity entity, int duration) {
        HISTORICAL_DURATIONS.put(entity.getUuid(), duration);
    }
}