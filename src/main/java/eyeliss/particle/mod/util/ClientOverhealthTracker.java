package eyeliss.particle.mod.util;

public class ClientOverhealthTracker {
    public static float currentShield = 0.0f;
    public static float maxShield = 0.0f;

    public static float getPercentage() {
        if (maxShield <= 0.0f) return 0.0f;
        return Math.clamp(currentShield / maxShield, 0.0f, 1.0f);
    }
}