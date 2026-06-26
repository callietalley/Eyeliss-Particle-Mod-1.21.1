package eyeliss.particle.mod.event;

import eyeliss.particle.mod.component.EngravingContents;
import eyeliss.particle.mod.component.ModComponents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import java.util.List;

public class PulverizingLootModifier {

    public static void registerLootModifiers() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) return;

            tableBuilder.modifyPools(poolBuilder -> LootTableEvents.MODIFY.register((k, tb, src, regs) -> {}));
        });

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

                if (!isStandardToolBlock && !state.isAir()) {
                    net.minecraft.block.Block.dropStack(world, pos, new ItemStack(state.getBlock().asItem()));
                }
            }
        });
    }
}
