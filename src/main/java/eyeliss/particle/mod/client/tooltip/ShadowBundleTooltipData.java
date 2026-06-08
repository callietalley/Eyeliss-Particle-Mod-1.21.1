package eyeliss.particle.mod.client.tooltip;

import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.tooltip.TooltipData;

public record ShadowBundleTooltipData(BundleContentsComponent contents) implements TooltipData {
}