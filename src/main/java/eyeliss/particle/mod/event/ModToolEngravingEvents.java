package eyeliss.particle.mod.event;

import eyeliss.particle.mod.component.BlockShrivingCharge;
import eyeliss.particle.mod.component.EngravingContents;
import eyeliss.particle.mod.component.ModComponents;
import eyeliss.particle.mod.component.ModEngravings;
import eyeliss.particle.mod.network.ServerVeinMineTracker;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ShearsItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.util.*;

public class ModToolEngravingEvents {

    public static void registerToolEvents() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || player.isCreative() || !(player instanceof ServerPlayerEntity serverPlayer)) return true;

            ItemStack tool = player.getMainHandStack();
            if (tool.isEmpty()) return true;

            List<EngravingContents> engravings = tool.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());
            boolean isHoldingKeybind = ServerVeinMineTracker.isPlayerHoldingKey(player.getUuid());

            if (isHoldingKeybind && engravings.stream().anyMatch(e -> e.engravingId().equals("pulverizing"))) {

                // --- 1. PICKAXE & SHOVEL: 3x3 AREA MINING ---
                if (tool.getItem() instanceof PickaxeItem || tool.getItem() instanceof ShovelItem) {
                    HitResult hit = player.raycast(4.5, 0.0f, false);
                    Direction minedSide = Direction.UP;
                    if (hit instanceof BlockHitResult blockHit) {
                        minedSide = blockHit.getSide();
                    }

                    List<BlockPos> offsets = new ArrayList<>();
                    if (minedSide == Direction.UP || minedSide == Direction.DOWN) {
                        for (int x = -1; x <= 1; x++) {
                            for (int z = -1; z <= 1; z++) {
                                if (x == 0 && z == 0) continue;
                                offsets.add(pos.add(x, 0, z));
                            }
                        }
                    } else if (minedSide == Direction.NORTH || minedSide == Direction.SOUTH) {
                        for (int x = -1; x <= 1; x++) {
                            for (int y = -1; y <= 1; y++) {
                                if (x == 0 && y == 0) continue;
                                offsets.add(pos.add(x, y, 0));
                            }
                        }
                    } else {
                        for (int z = -1; z <= 1; z++) {
                            for (int y = -1; y <= 1; y++) {
                                if (z == 0 && y == 0) continue;
                                offsets.add(pos.add(0, y, z));
                            }
                        }
                    }

                    for (BlockPos targetPos : offsets) {
                        BlockState targetState = world.getBlockState(targetPos);
                        if (tool.isSuitableFor(targetState)) {
                            Block.dropStacks(targetState, world, targetPos, world.getBlockEntity(targetPos), player, tool);
                            world.breakBlock(targetPos, false, player);

                            // Award Geologic progression for co-mined blocks (100% chance for solid blocks)
                            awardPulverizeBlockCharge(serverPlayer);

                            tool.damage(1, serverPlayer, EquipmentSlot.MAINHAND);
                            if (tool.isEmpty()) break;
                        }
                    }
                }

                // --- 2. HOE & SHEARS: LEAF-ONLY TRIMMING (128 CAP, ZERO COST) ---
                else if ((tool.getItem() instanceof HoeItem || tool.getItem() instanceof ShearsItem) && state.isIn(BlockTags.LEAVES)) {
                    Queue<BlockPos> queue = new LinkedList<>();
                    Set<BlockPos> visited = new HashSet<>();
                    queue.add(pos);
                    visited.add(pos);

                    int leafTrimCount = 0;
                    while (!queue.isEmpty() && leafTrimCount < 128) {
                        BlockPos currentPos = queue.poll();

                        for (int x = -1; x <= 1; x++) {
                            for (int y = -1; y <= 1; y++) {
                                for (int z = -1; z <= 1; z++) {
                                    if (x == 0 && y == 0 && z == 0) continue;

                                    BlockPos adjacent = currentPos.add(x, y, z);
                                    if (visited.contains(adjacent)) continue;

                                    BlockState adjState = world.getBlockState(adjacent);
                                    if (adjState.isIn(BlockTags.LEAVES)) {
                                        visited.add(adjacent);
                                        queue.add(adjacent);
                                        leafTrimCount++;

                                        Block.dropStacks(adjState, world, adjacent, world.getBlockEntity(adjacent), player, tool);
                                        world.setBlockState(adjacent, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);

                                        // --- FIX: BALANCED 33% PROBABILITY RATIO GATE ON LEAF TRIMMING ---
                                        if (serverPlayer.getRandom().nextFloat() < 0.33f) {
                                            awardPulverizeBlockCharge(serverPlayer);
                                        }

                                        if (leafTrimCount >= 128) break;
                                    }
                                }
                                if (leafTrimCount >= 128) break;
                            }
                            if (leafTrimCount >= 128) break;
                        }
                    }
                }

                // --- 3. HOE: OMNI CROP VEIN MINING (MATURED ONLY) ---
                else if (tool.getItem() instanceof HoeItem && (state.getBlock() instanceof CropBlock || state.isIn(BlockTags.CROPS))) {
                    if (state.getBlock() instanceof CropBlock cropBlock && state.get(CropBlock.AGE) < cropBlock.getMaxAge()) {
                        return true;
                    }

                    Queue<BlockPos> queue = new LinkedList<>();
                    Set<BlockPos> visited = new HashSet<>();
                    queue.add(pos);
                    visited.add(pos);

                    int cropCount = 0;
                    while (!queue.isEmpty() && cropCount < 64) {
                        BlockPos currentPos = queue.poll();

                        for (int x = -1; x <= 1; x++) {
                            for (int z = -1; z <= 1; z++) {
                                if (x == 0 && z == 0) continue;

                                BlockPos adjacent = currentPos.add(x, 0, z);
                                BlockState adjState = world.getBlockState(adjacent);

                                if (!visited.contains(adjacent) && (adjState.getBlock() instanceof CropBlock || adjState.isIn(BlockTags.CROPS))) {
                                    if (adjState.getBlock() instanceof CropBlock cb) {
                                        if (adjState.get(CropBlock.AGE) < cb.getMaxAge()) continue;
                                    }

                                    visited.add(adjacent);
                                    queue.add(adjacent);
                                    cropCount++;

                                    Block.dropStacks(adjState, world, adjacent, world.getBlockEntity(adjacent), player, tool);
                                    world.breakBlock(adjacent, false, player);

                                    // Award Geologic progress for co-harvested mature crops (100% chance)
                                    awardPulverizeBlockCharge(serverPlayer);

                                    tool.damage(1, serverPlayer, EquipmentSlot.MAINHAND);
                                    if (tool.isEmpty()) break;
                                }
                            }
                        }
                        if (tool.isEmpty()) break;
                    }
                }

                // --- 4. AXE: OMNI LOG + LOG-CONNECTED LEAVES (DUAL-CAP OVERLAY) ---
                else if (tool.getItem() instanceof AxeItem && state.isIn(BlockTags.LOGS)) {
                    Queue<BlockPos> queue = new LinkedList<>();
                    Set<BlockPos> visited = new HashSet<>();
                    queue.add(pos);
                    visited.add(pos);

                    int logCount = 0;
                    int leafCount = 0;

                    while (!queue.isEmpty() && (logCount < 64 || leafCount < 64)) {
                        BlockPos currentPos = queue.poll();

                        for (int x = -1; x <= 1; x++) {
                            for (int y = -1; y <= 1; y++) {
                                for (int z = -1; z <= 1; z++) {
                                    if (x == 0 && y == 0 && z == 0) continue;

                                    BlockPos adjacent = currentPos.add(x, y, z);
                                    if (visited.contains(adjacent)) continue;

                                    BlockState adjState = world.getBlockState(adjacent);
                                    boolean isLog = adjState.isIn(BlockTags.LOGS);
                                    boolean isLeaf = adjState.isIn(BlockTags.LEAVES);

                                    if (isLog && logCount < 64) {
                                        visited.add(adjacent);
                                        queue.add(adjacent);
                                        logCount++;

                                        Block.dropStacks(adjState, world, adjacent, world.getBlockEntity(adjacent), player, tool);
                                        world.breakBlock(adjacent, false, player);

                                        // Award Geologic progress for co-mined vein log blocks (100% chance)
                                        awardPulverizeBlockCharge(serverPlayer);

                                        tool.damage(1, serverPlayer, EquipmentSlot.MAINHAND);
                                    }
                                    else if (isLeaf && leafCount < 64) {
                                        visited.add(adjacent);
                                        queue.add(adjacent);
                                        leafCount++;

                                        Block.dropStacks(adjState, world, adjacent, world.getBlockEntity(adjacent), player, tool);
                                        world.setBlockState(adjacent, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);

                                        // --- FIX: BALANCED 33% PROBABILITY RATIO GATE ON LOG-CONNECTED LEAVES ---
                                        if (serverPlayer.getRandom().nextFloat() < 0.33f) {
                                            awardPulverizeBlockCharge(serverPlayer);
                                        }
                                    }

                                    if (tool.isEmpty()) break;
                                }
                                if (tool.isEmpty()) break;
                            }
                            if (tool.isEmpty()) break;
                        }
                        if (tool.isEmpty()) break;
                    }
                }
            } // Closes the 'if (isHoldingKeybind...)' check block

            if (engravings.stream().anyMatch(e -> e.engravingId().equals("shattering")) && !player.isSneaking()) {
                boolean isStandardToolBlock = state.isIn(BlockTags.SHOVEL_MINEABLE)
                        || state.isIn(BlockTags.PICKAXE_MINEABLE)
                        || state.isIn(BlockTags.AXE_MINEABLE)
                        || state.isIn(BlockTags.HOE_MINEABLE);

                if (!isStandardToolBlock && !state.isAir()) {
                    // Let the block break naturally to completely eliminate the double break sound!
                    return true;
                }
            }

            return true; // Fallback: Allows the block to break normally if no special filters match
        });
    }

    /**
     * Internal Core Engine: Awards a block-breaking charge point across your tool matrices.
     * Processes main hand tools directly, and grants a 50% off-hand siphon transfer chance.
     */
    private static void awardPulverizeBlockCharge(ServerPlayerEntity serverPlayer) {
        List<ItemStack> blockProcessingTargets = new ArrayList<>();

        ItemStack mainHandTool = serverPlayer.getMainHandStack();
        if (!mainHandTool.isEmpty() && mainHandTool.contains(ModComponents.BLOCK_CHARGE)) {
            blockProcessingTargets.add(mainHandTool);
        }

        ItemStack offHandTool = serverPlayer.getOffHandStack();
        if (!offHandTool.isEmpty() && offHandTool.contains(ModComponents.BLOCK_CHARGE) && mainHandTool.contains(ModComponents.BLOCK_CHARGE)) {
            if (serverPlayer.getRandom().nextFloat() < 0.50f) {
                blockProcessingTargets.add(offHandTool);
            }
        }

        for (ItemStack targetStack : blockProcessingTargets) {
            BlockShrivingCharge blockShrivingCharge = targetStack.get(ModComponents.BLOCK_CHARGE);
            if (blockShrivingCharge == null) continue;

            int nextBlocksCount = blockShrivingCharge.currentBlocks() + 1;

            if (nextBlocksCount >= blockShrivingCharge.requiredBlocks()) {
                List<EngravingContents> currentList = new ArrayList<>(targetStack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of()));
                String selectedId = null;
                boolean foundValidUpgrade = false;

                for (int loop = 0; loop < 20; loop++) {
                    Optional<String> rolledOpt = ModEngravings.rollEngravingFor(targetStack, serverPlayer.getRandom());
                    if (rolledOpt.isEmpty()) break;

                    String rolledId = rolledOpt.get();
                    int level = currentList.stream().filter(e -> e.engravingId().equals(rolledId)).map(EngravingContents::level).findFirst().orElse(0);
                    int ceiling = ModEngravings.isBlessingOrCurse(rolledId) ? 1 : 3;

                    if (level < ceiling) {
                        selectedId = rolledId;
                        foundValidUpgrade = true;
                        break;
                    }
                }

                if (!foundValidUpgrade) {
                    serverPlayer.sendMessage(Text.literal("This item has reached its absolute maximum engraving potential!").formatted(Formatting.RED), true);
                    serverPlayer.getWorld().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                            SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.5f, 1.5f);

                    targetStack.set(ModComponents.BLOCK_CHARGE, new BlockShrivingCharge(blockShrivingCharge.requiredBlocks() - 1, blockShrivingCharge.requiredBlocks()));
                    continue;
                }

                targetStack.remove(ModComponents.BLOCK_CHARGE);

                boolean updated = false;
                for (int idx = 0; idx < currentList.size(); idx++) {
                    if (currentList.get(idx).engravingId().equals(selectedId)) {
                        currentList.set(idx, new EngravingContents(selectedId, currentList.get(idx).level() + 1));
                        updated = true;
                        break;
                    }
                }
                if (!updated) currentList.add(new EngravingContents(selectedId, 1));
                targetStack.set(ModComponents.ENGRAVING_CONTENTS, currentList);

                serverPlayer.getWorld().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.7f, 1.2f);
            } else {
                targetStack.set(ModComponents.BLOCK_CHARGE, new BlockShrivingCharge(nextBlocksCount, blockShrivingCharge.requiredBlocks()));
            }
        }
    }
}
