package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.item.ShadowCurseHelper;
import eyeliss.particle.mod.item.ModItems;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
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

        // Verify that the right item slot contains the SHADOW_BOOK
        if (!leftStack.isEmpty() && rightStack.isOf(ModItems.SHADOW_BOOK)) {

            // Fix Duplication: Force the output item copy stack count to exactly 1
            ItemStack resultStack = leftStack.copy();
            resultStack.setCount(1);

            ItemEnchantmentsComponent leftEnchants = EnchantmentHelper.getEnchantments(leftStack);
            ItemEnchantmentsComponent rightEnchants = EnchantmentHelper.getEnchantments(rightStack);

            boolean logicChanged = false;
            boolean hasMutuallyExclusiveViolation = false;
            int cost = 0;

            // 1. Loop through shadow book enchants to verify safety and compute costs
            for (var entry : rightEnchants.getEnchantmentEntries()) {
                RegistryEntry<Enchantment> bookEnchantment = entry.getKey();
                int bookLevel = entry.getIntValue();
                int currentLevel = leftEnchants.getLevel(bookEnchantment);

                for (var leftEntry : leftEnchants.getEnchantmentEntries()) {
                    RegistryEntry<Enchantment> leftEnchantment = leftEntry.getKey();

                    if (!bookEnchantment.equals(leftEnchantment)) {
                        if (bookEnchantment.value().exclusiveSet().contains(leftEnchantment)) {
                            hasMutuallyExclusiveViolation = true;
                        }
                    }
                }

                int finalLevel = (currentLevel == bookLevel) ? bookLevel + 1 : Math.max(currentLevel, bookLevel);
                int maxLevel = bookEnchantment.value().getMaxLevel();
                if (finalLevel > maxLevel) {
                    finalLevel = maxLevel;
                }

                cost += finalLevel * 2;
                logicChanged = true;
            }

            if (logicChanged) {
                // 2. Apply changes to our working item copy
                EnchantmentHelper.apply(resultStack, innerBuilder -> {
                    for (var entry : rightEnchants.getEnchantmentEntries()) {
                        RegistryEntry<Enchantment> enchantment = entry.getKey();
                        int bookLevel = entry.getIntValue();
                        int currentLevel = leftEnchants.getLevel(enchantment);

                        int finalLevel = (currentLevel == bookLevel) ? bookLevel + 1 : Math.max(currentLevel, bookLevel);
                        int maxLevel = enchantment.value().getMaxLevel();
                        if (finalLevel > maxLevel) {
                            finalLevel = maxLevel;
                        }

                        innerBuilder.set(enchantment, finalLevel);
                    }
                });

                if (hasMutuallyExclusiveViolation) {
                    ShadowCurseHelper.applyShadowCurse(resultStack);
                    cost += 15;
                }

                // Yarn/Mojang mappings dictate field_29577/field_29578 or similar for item costs.
                // Anvil handles this natively if we pass structural completion, but setting the count
                // on output ensures taking the output only subtracts 1 from your input stacks!

                this.output.setStack(0, resultStack);
                this.levelCost.set(Math.max(1, cost));

                ci.cancel();
            }
        }
    }
}