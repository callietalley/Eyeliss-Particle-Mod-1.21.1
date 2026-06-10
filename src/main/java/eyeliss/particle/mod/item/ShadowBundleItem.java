package eyeliss.particle.mod.item;

import eyeliss.particle.mod.client.tooltip.ShadowBundleTooltipData;
import eyeliss.particle.mod.sound.ModSounds;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ClickType;
import org.apache.commons.lang3.math.Fraction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShadowBundleItem extends Item {
    private static final Fraction MAX_SHADOW_CAPACITY = Fraction.getFraction(27, 1);

    public ShadowBundleItem(Settings settings) {
        super(settings);
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        BundleContentsComponent contents = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (contents == null) {
            return Optional.empty();
        }
        return Optional.of(new ShadowBundleTooltipData(contents));
    }

    private boolean isBannedItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();

        if (item instanceof BundleItem || item instanceof ShadowBundleItem) {
            return true;
        }

        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof ShulkerBoxBlock) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.RIGHT) {
            return false;
        } else {
            ItemStack slotStack = slot.getStack();
            BundleContentsComponent contents = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);

            if (slotStack.isEmpty()) {
                boolean popped = popFirstItemToCursor(stack, contents, new StackReference() {
                    @Override
                    public ItemStack get() { return slot.getStack(); }
                    @Override
                    public boolean set(ItemStack stack) { slot.setStack(stack); return true; }
                });

                if (popped) {
                    player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), ModSounds.SHADOW_BUNDLE_WITHDRAW_EVENT, SoundCategory.PLAYERS, 0.8F, 1.0F);
                }
            } else {
                if (isBannedItem(slotStack)) {
                    player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), ModSounds.SHADOW_BUNDLE_INSERT_FAIL_EVENT, SoundCategory.PLAYERS, 0.8F, 1.0F);
                    return true;
                }

                if (slotStack.getItem().canBeNested()) {
                    Fraction currentOccupancy = contents.getOccupancy();
                    Fraction itemWeight = Fraction.getFraction(slotStack.getCount(), slotStack.getMaxCount());

                    if (currentOccupancy.add(itemWeight).compareTo(MAX_SHADOW_CAPACITY) <= 0) {
                        List<ItemStack> newStacks = new ArrayList<>(contents.stream().toList());
                        newStacks.add(0, slotStack.copy());

                        stack.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(newStacks));
                        slot.setStack(ItemStack.EMPTY);

                        player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), ModSounds.SHADOW_BUNDLE_INSERT_EVENT, SoundCategory.PLAYERS, 0.8F, 1.0F);
                    } else {
                        player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), ModSounds.SHADOW_BUNDLE_INSERT_FAIL_EVENT, SoundCategory.PLAYERS, 0.8F, 1.0F);
                    }
                }
            }
            return true;
        }
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT && slot.canTakePartial(player)) {
            if (otherStack.isEmpty()) {
                boolean popped = popFirstItemToCursor(stack, stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT), cursorStackReference);
                if (popped) {
                    player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), ModSounds.SHADOW_BUNDLE_WITHDRAW_EVENT, SoundCategory.PLAYERS, 0.8F, 1.0F);
                }
            } else {
                if (isBannedItem(otherStack)) {
                    player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), ModSounds.SHADOW_BUNDLE_INSERT_FAIL_EVENT, SoundCategory.PLAYERS, 0.8F, 1.0F);
                    return true;
                }

                if (otherStack.getItem().canBeNested()) {
                    BundleContentsComponent contents = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
                    Fraction currentOccupancy = contents.getOccupancy();
                    Fraction itemWeight = Fraction.getFraction(otherStack.getCount(), otherStack.getMaxCount());

                    if (currentOccupancy.add(itemWeight).compareTo(MAX_SHADOW_CAPACITY) <= 0) {
                        List<ItemStack> newStacks = new ArrayList<>(contents.stream().toList());
                        newStacks.add(0, otherStack.copy());

                        stack.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(newStacks));
                        otherStack.setCount(0);

                        player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), ModSounds.SHADOW_BUNDLE_INSERT_EVENT, SoundCategory.PLAYERS, 0.8F, 1.0F);
                    } else {
                        player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), ModSounds.SHADOW_BUNDLE_INSERT_FAIL_EVENT, SoundCategory.PLAYERS, 0.8F, 1.0F);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean popFirstItemToCursor(ItemStack bundle, BundleContentsComponent contents, StackReference cursorStackReference) {
        if (!contents.isEmpty()) {
            List<ItemStack> newStacks = new ArrayList<>(contents.stream().toList());
            ItemStack currentCursorStack = cursorStackReference.get();
            ItemStack targetToPop = newStacks.get(0);

            if (currentCursorStack.isEmpty()) {
                ItemStack poppedItem = newStacks.remove(0);
                cursorStackReference.set(poppedItem);
                bundle.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(newStacks));
                return true;
            }
            else if (ItemStack.areItemsAndComponentsEqual(currentCursorStack, targetToPop)) {
                int spaceLeftOnCursor = currentCursorStack.getMaxCount() - currentCursorStack.getCount();
                if (spaceLeftOnCursor > 0) {
                    int transferAmount = Math.min(spaceLeftOnCursor, targetToPop.getCount());

                    currentCursorStack.increment(transferAmount);
                    targetToPop.decrement(transferAmount);

                    if (targetToPop.isEmpty()) {
                        newStacks.remove(0);
                    }
                    bundle.set(DataComponentTypes.BUNDLE_CONTENTS, new BundleContentsComponent(newStacks));
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        BundleContentsComponent contents = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        return contents != null && !contents.isEmpty();
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        BundleContentsComponent contents = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (contents == null || contents.isEmpty()) {
            return 0;
        }

        float currentOccupancy = contents.getOccupancy().floatValue();

        float fullnessPercentage = Math.min(1.0F, currentOccupancy / 27.0F);
        return Math.round(fullnessPercentage * 13.0F);}
        @Override
        public int getItemBarColor(ItemStack stack) {return 0xD665FF;}
    }