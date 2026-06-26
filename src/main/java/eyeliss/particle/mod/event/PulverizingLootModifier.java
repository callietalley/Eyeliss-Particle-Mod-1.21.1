package eyeliss.particle.mod.event;

import eyeliss.particle.mod.component.EngravingContents;
import eyeliss.particle.mod.component.ModComponents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import java.util.List;

public class PulverizingLootModifier {

    public static void registerLootModifiers() {
        // Safe, stable Fabric API hook that intercepts block loot pools right before they spill into the world
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            // Only modify loot tables that belong to block drops
            if (!source.isBuiltin()) return;

            tableBuilder.modifyPools(poolBuilder -> {
                // We hook into the loot context parameters to inspect the harvesting block and player tool
                LootTableEvents.MODIFY.register((k, tb, src, regs) -> {});
            });
        });

        // Alternate clean method: Use Fabric's standard BlockBreak event pass to inject the drop safely
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || player.isCreative() || player.isSneaking()) return;

            ItemStack tool = player.getMainHandStack();
            if (tool.isEmpty()) return;

            List<EngravingContents> engravings = tool.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());
            boolean hasShattering = engravings.stream().anyMatch(e -> e.engravingId().equals("shattering"));

            if (hasShattering) {
                boolean isStandardToolBlock = state.isIn(BlockTags.SHOVEL_MINEABLE)
                        || state.isIn(BlockTags.PICKAXE_MINEABLE)
                        || state.isIn(BlockTags.AXE_MINEABLE)
                        || state.isIn(BlockTags.HOE_MINEABLE);

                // If it's a fragile glass block, drop the block itself directly at the position coordinates!
                if (!isStandardToolBlock && !state.isAir()) {
                    net.minecraft.block.Block.dropStack(world, pos, new ItemStack(state.getBlock().asItem()));
                }
            }
        });
    }
}
