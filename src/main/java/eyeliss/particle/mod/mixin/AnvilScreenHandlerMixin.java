package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.item.ShadowCurseHelper;
import eyeliss.particle.mod.item.ModItems;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    @Shadow @Final private Property levelCost;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void applyShadowBookEnchantments(CallbackInfo ci) {
        ItemStack leftStack = this.input.getStack(0);
        ItemStack rightStack = this.input.getStack(1);

        if (leftStack.isEmpty() || rightStack.isEmpty()) {
            return;
        }

        boolean isItemWithShadowBook = !leftStack.isOf(ModItems.SHADOW_BOOK) && rightStack.isOf(ModItems.SHADOW_BOOK);
        boolean isShadowBookWithEnchantedBook = leftStack.isOf(ModItems.SHADOW_BOOK) && rightStack.isOf(Items.ENCHANTED_BOOK);

        if (isItemWithShadowBook || isShadowBookWithEnchantedBook) {

            ItemStack resultStack = leftStack.copy();
            resultStack.setCount(1);

            ItemEnchantmentsComponent leftEnchants = EnchantmentHelper.getEnchantments(leftStack);
            ItemEnchantmentsComponent rightEnchants = EnchantmentHelper.getEnchantments(rightStack);

            boolean logicChanged = false;
            boolean hasMutuallyExclusiveViolation = false;
            int cost = 0;

            for (var entry : rightEnchants.getEnchantmentEntries()) {
                RegistryEntry<Enchantment> rightEnchantment = entry.getKey();
                int rightLevel = entry.getIntValue();
                int leftLevel = leftEnchants.getLevel(rightEnchantment);
                int maxLevel = rightEnchantment.value().getMaxLevel();

                // 1. RULE CHANGE: Verify if the incoming enchantment is legally acceptable on this specific item type
                // If it is completely invalid (like Sharpness on a Pickaxe), skip it entirely so it is not added
                if (!rightEnchantment.value().isAcceptableItem(leftStack) && !leftStack.isOf(ModItems.SHADOW_BOOK)) {
                    continue;
                }

                // 2. Mutual Exclusivity Check (e.g., Silk Touch vs Fortune)
                // We still flag this as a violation, but we no longer block the merger from happening
                for (var leftEntry : leftEnchants.getEnchantmentEntries()) {
                    RegistryEntry<Enchantment> leftEnchantment = leftEntry.getKey();

                    if (!rightEnchantment.equals(leftEnchantment)) {
                        if (rightEnchantment.value().exclusiveSet().contains(leftEnchantment)) {
                            hasMutuallyExclusiveViolation = true;
                        }
                    }
                }

                int finalLevel;
                if (leftLevel > maxLevel || rightLevel > maxLevel) {
                    finalLevel = Math.max(leftLevel, rightLevel);
                } else {
                    finalLevel = (leftLevel == rightLevel) ? rightLevel + 1 : Math.max(leftLevel, rightLevel);
                    if (finalLevel > maxLevel) {
                        finalLevel = maxLevel;
                    }
                }

                cost += finalLevel * 2;
                logicChanged = true;
            }

            if (logicChanged) {
                EnchantmentHelper.apply(resultStack, innerBuilder -> {
                    for (var entry : leftEnchants.getEnchantmentEntries()) {
                        innerBuilder.set(entry.getKey(), entry.getIntValue());
                    }

                    for (var entry : rightEnchants.getEnchantmentEntries()) {
                        RegistryEntry<Enchantment> enchantment = entry.getKey();
                        int rightLevel = entry.getIntValue();
                        int leftLevel = leftEnchants.getLevel(enchantment);
                        int maxLevel = enchantment.value().getMaxLevel();

                        // Enforce the same item acceptability checks during the application pass
                        if (!enchantment.value().isAcceptableItem(leftStack) && !leftStack.isOf(ModItems.SHADOW_BOOK)) {
                            continue;
                        }

                        int finalLevel;
                        if (leftLevel > maxLevel || rightLevel > maxLevel) {
                            finalLevel = Math.max(leftLevel, rightLevel);
                        } else {
                            finalLevel = (leftLevel == rightLevel) ? rightLevel + 1 : Math.max(leftLevel, rightLevel);
                            if (finalLevel > maxLevel) {
                                finalLevel = maxLevel;
                            }
                        }

                        innerBuilder.set(enchantment, finalLevel);
                    }
                });

                if (hasMutuallyExclusiveViolation) {
                    ShadowCurseHelper.applyShadowCurse(resultStack);
                    cost += 15;
                }

                int finalAllowedCost = Math.clamp(cost, 1, 30);

                this.output.setStack(0, resultStack);
                this.levelCost.set(finalAllowedCost);

                ci.cancel();
            }
        }
    }
}