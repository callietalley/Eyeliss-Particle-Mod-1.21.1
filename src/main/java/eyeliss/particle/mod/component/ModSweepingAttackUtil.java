// FIX: Relocated completely out of the mixin package directory to bypass the IllegalClassLoadError crash
package eyeliss.particle.mod.component;
import net.minecraft.item.ItemStack;
import java.util.List;

public class ModSweepingAttackUtil {

    public static double calculateSweepingAreaRangeModifier(ItemStack tool, boolean isNativelySword) {
        List<EngravingContents> engravings = tool.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());

        for (EngravingContents e : engravings) {
            if (e.engravingId().equals("sweeping")) {
                int level = e.level();

                if (!isNativelySword) {
                    return 0.70 + ((level - 1) * 0.10);
                } else {
                    return 1.25 + ((level - 1) * 0.075);
                }
            }
        }
        return isNativelySword ? 1.0 : 0.0;
    }
}
