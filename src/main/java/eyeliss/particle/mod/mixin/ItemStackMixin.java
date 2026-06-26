package eyeliss.particle.mod.mixin;

import eyeliss.particle.mod.component.BlockShrivingCharge;
import eyeliss.particle.mod.component.BloodShrivingCharge;
import eyeliss.particle.mod.component.BlessedCharge;
import eyeliss.particle.mod.component.EngravingContents;
import eyeliss.particle.mod.component.ModComponents;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Unique private boolean eyelisspartmod$isProcessingDamage = false;
    @Unique private static final ThreadLocal<Boolean> eyelisspartmod$isSortingTooltip = ThreadLocal.withInitial(() -> false);

    // --- TOOL ENGRAVINGS: INFRASTRUCTURE RECURSION-GUARDED DURABILITY MODIFIER ---
    @Inject(method = "setDamage(I)V", at = @At("HEAD"), cancellable = true)
    private void injectEngravingDurabilityModifiers(int damage, CallbackInfo ci) {
        // Stop execution instantly if this call was triggered by our own modifier bonus injection pass
        if (this.eyelisspartmod$isProcessingDamage) return;

        ItemStack stack = (ItemStack)(Object)this;
        List<EngravingContents> engravings = stack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());
        if (engravings.isEmpty()) return;

        try {
            this.eyelisspartmod$isProcessingDamage = true;

            int currentDamage = stack.getDamage();

            // --- UNIVERSAL MENDING II ENHANCEMENT MATRIX ---
            // If the incoming damage value is LESS than the current damage, the item is being REPAIRED.
            if (damage < currentDamage) {
                boolean hasRestoration = engravings.stream().anyMatch(e -> e.engravingId().equals("restoration"));

                if (hasRestoration) {
                    // Check if the item also carries vanilla Mending natively
                    ItemEnchantmentsComponent enchants = stack.getOrDefault(
                            DataComponentTypes.ENCHANTMENTS,
                            ItemEnchantmentsComponent.DEFAULT
                    );

                    boolean hasVanillaMending = enchants.getEnchantments().stream()
                            .anyMatch(e -> e.matchesKey(Enchantments.MENDING));

                    // CLUMPS & VANILLA COMPATIBLE COMBO LOGIC:
                    // If the item has Restoration AND vanilla Mending, we intercept the repair event.
                    // Vanilla/Clumps already calculated a +2 durability fix. We catch that difference,
                    // double it on the spot, and rewrite the final damage value to achieve a true 4-points-per-XP fix!
                    if (hasVanillaMending) {
                        int vanillaRepairAmount = currentDamage - damage;

                        // Apply an extra matching repair bonus chunk directly to the incoming calculation value
                        int calculatedComboDamage = Math.max(0, damage - vanillaRepairAmount);

                        stack.setDamage(calculatedComboDamage);
                        ci.cancel(); // Cancel the un-boosted parent repair transaction pass immediately
                        return;
                    }
                }
            }

        } finally {
            this.eyelisspartmod$isProcessingDamage = false;
        }
    }

    // =========================================================================
    // PART 4: ENFORCED GLOBAL STRUCTURAL TOOLTIP SEQUENCER ENGINE
    // =========================================================================
    @Inject(method = "getTooltip", at = @At("HEAD"), cancellable = true)
    private void enforceGlobalStructuralOrder(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        if (eyelisspartmod$isSortingTooltip.get()) return;

        ItemStack stack = (ItemStack) (Object) this;
        List<EngravingContents> engravings = stack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());

        boolean hasKillCharge = stack.contains(ModComponents.SHRIVING_CHARGE);
        boolean hasBlockCharge = stack.contains(ModComponents.BLOCK_CHARGE);
        boolean hasBlessedCharge = stack.contains(ModComponents.BLESSED_CHARGE);

        if (engravings.isEmpty() && !hasKillCharge && !hasBlockCharge && !hasBlessedCharge) return;

        try {
            eyelisspartmod$isSortingTooltip.set(true);

            ItemEnchantmentsComponent enchantmentsComponent = stack.get(DataComponentTypes.ENCHANTMENTS);
            ItemStack baseStackCopy = stack.copy();
            baseStackCopy.set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

            List<Text> baseLines = new ArrayList<>(baseStackCopy.getTooltip(context, player, type));
            if (baseLines.isEmpty()) return;

            List<Text> topBlock = new ArrayList<>();
            List<Text> bottomBlock = new ArrayList<>();
            topBlock.add(baseLines.get(0));

            int technicalStartIndex = baseLines.size();
            for (int i = 1; i < baseLines.size(); i++) {
                Text line = baseLines.get(i);
                String literalText = line.getString();
                net.minecraft.text.Style style = line.getStyle();

                String colorName = style.getColor() != null ? style.getColor().getName() : "";
                boolean isGray = colorName.equals("gray") || colorName.equals("dark_gray");

                boolean isAdvancedTechnicalText = isGray && (
                        literalText.startsWith("Durability:") ||
                                literalText.contains("component(s)") ||
                                literalText.matches("^\\d+.*")
                );

                boolean isModName = style.isItalic() && style.getColor() != null;
                boolean isRegistryId = isGray && literalText.contains(":") && !literalText.contains(" ");

                if (literalText.startsWith("When in ") || literalText.startsWith("When on ") ||
                        literalText.contains("Attribute") || literalText.startsWith("+") ||
                        literalText.startsWith("-") || literalText.startsWith("[") ||
                        isAdvancedTechnicalText || isRegistryId || isModName) {

                    technicalStartIndex = i;
                    break;
                }
            }

            for (int i = 1; i < baseLines.size(); i++) {
                Text line = baseLines.get(i);

                if (i < technicalStartIndex) {
                    if (!line.getString().isEmpty()) {
                        topBlock.add(line);
                    }
                } else {
                    bottomBlock.add(line);
                }
            }

            List<Text> finalOrderedTooltip = new ArrayList<>(topBlock);

            // --- RESOLVE ADVANCED TRANSLATABLE ENCHANTMENT OVERLAYS ---
            boolean hasDwarven = engravings.stream().anyMatch(e -> e.engravingId().equals("dwarven"));
            boolean hasRestoration = engravings.stream().anyMatch(e -> e.engravingId().equals("restoration"));
            String fortuneNameKey = Enchantments.FORTUNE.getValue().toTranslationKey("enchantment");
            String mendingNameKey = Enchantments.MENDING.getValue().toTranslationKey("enchantment");

            if (enchantmentsComponent != null && !enchantmentsComponent.isEmpty()) {
                for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantmentsComponent.getEnchantmentEntries()) {
                    RegistryEntry<Enchantment> enchRef = entry.getKey();
                    int originalLevel = entry.getIntValue();

                    // A. DWARVEN + FORTUNE FORMULA OVERLAY
                    if (hasDwarven && enchRef.matchesKey(Enchantments.FORTUNE)) {
                        int combinedTotal = originalLevel + 1;
                        Text baseNumeral = Text.translatable("enchantment.level." + originalLevel);
                        Text totalNumeral = Text.translatable("enchantment.level." + combinedTotal);

                        MutableText customizedFormulaLine = Text.translatable(fortuneNameKey)
                                .append(" ")
                                .append(totalNumeral)
                                .append(" (")
                                .append(baseNumeral)
                                .append(" + I)")
                                .formatted(Formatting.GRAY);

                        finalOrderedTooltip.add(customizedFormulaLine);
                    }
                    // B. RESTORATION + MENDING COMBO OVERLAY
                    else if (hasRestoration && enchRef.matchesKey(Enchantments.MENDING)) {
                        MutableText mendingTwoLine = Text.translatable(mendingNameKey)
                                .append(" ")
                                .append(Text.translatable("enchantment.level.2")) // Displays "Mending II"
                                .append(" (")
                                .append(Text.translatable("enchantment.level.1"))
                                .append(" + Restoration)")
                                .formatted(Formatting.GRAY);

                        finalOrderedTooltip.add(mendingTwoLine);
                    }
                    else {
                        finalOrderedTooltip.add(Enchantment.getName(enchRef, originalLevel));
                    }
                }
            }

            // Fallback A: If the tool has Dwarven but lacks a Fortune book entirely
            if (hasDwarven && (enchantmentsComponent == null || enchantmentsComponent.getEnchantmentEntries().stream().noneMatch(e -> e.getKey().matchesKey(Enchantments.FORTUNE)))) {
                MutableText placeholderLine = Text.translatable(fortuneNameKey).append(" ").append(Text.translatable("enchantment.level.1")).formatted(Formatting.GRAY, Formatting.ITALIC);
                finalOrderedTooltip.add(placeholderLine);
            }

            // Fallback B: If the tool has Restoration but lacks a vanilla Mending book entirely
            if (hasRestoration && (enchantmentsComponent == null || enchantmentsComponent.getEnchantmentEntries().stream().noneMatch(e -> e.getKey().matchesKey(Enchantments.MENDING)))) {
                MutableText mendingPlaceholder = Text.translatable(mendingNameKey).append(" ").append(Text.translatable("enchantment.level.1")).formatted(Formatting.GRAY);
                finalOrderedTooltip.add(mendingPlaceholder);
            }

            // --- RENDER DYNAMIC CUSTOM ENGRAVINGS (UN-INDENTED, FLUSH LEFT) ---
            for (EngravingContents engraving : engravings) {
                MutableText engravingLine = Text.empty().append(Text.translatable("engraving.eyelisspartmod." + engraving.engravingId()));

                boolean isBlessingOrCurse = engraving.engravingId().equals("ethereal") || engraving.engravingId().equals("dwarven") || engraving.engravingId().equals("shattering") || engraving.engravingId().equals("stagnation") || engraving.engravingId().equals("ruin") || engraving.engravingId().equals("blessed");
                if (!isBlessingOrCurse && engraving.level() > 1) {
                    engravingLine.append(Text.literal(" "))
                            .append(Text.translatable("enchantment.level." + engraving.level()));
                }

                engravingLine.setStyle(eyeliss.particle.mod.client.EngravingColorAnimator.getAnimatedMagicStyle(engraving.engravingId(), stack));
                finalOrderedTooltip.add(engravingLine);
            }

            // --- APPEND TECHNICAL ATTRIBUTE MODIFIERS & ADVANCED TOOLTIPS ---
            if (!bottomBlock.isEmpty()) {
                finalOrderedTooltip.add(Text.empty());
                finalOrderedTooltip.addAll(bottomBlock);
            }

            // =========================================================================
            // PROGRESS BARS SEGMENT: INJECT METERS AT THE TAIL CAP ENDS
            // =========================================================================

            // 1. BLOOD SHRIVING RE-ROLL PROGRESS TRACKER (RED)
            if (hasKillCharge) {
                BloodShrivingCharge charge = stack.get(ModComponents.SHRIVING_CHARGE);
                if (charge != null && charge.requiredKills() > 0) {
                    float progress = (float) charge.currentKills() / charge.requiredKills();
                    int filledSegments = Math.round(progress * 10);

                    MutableText barLine = Text.literal("[ ").formatted(Formatting.DARK_GRAY);
                    for (int i = 0; i < filledSegments; i++) barLine.append(Text.literal("█").formatted(Formatting.DARK_RED));
                    for (int i = filledSegments; i < 10; i++) barLine.append(Text.literal("░").formatted(Formatting.DARK_GRAY));
                    barLine.append(Text.literal(" ] ").formatted(Formatting.DARK_GRAY));

                    MutableText fractionLabel = Text.literal(charge.currentKills() + " / " + charge.requiredKills() + " Kills").formatted(Formatting.DARK_RED);

                    finalOrderedTooltip.add(Text.empty());
                    finalOrderedTooltip.add(Text.empty().append(barLine).append(fractionLabel));
                }
            }

            // 2. GEOLOGIC SHRIVING RE-ROLL PROGRESS TRACKER (GOLD)
            if (hasBlockCharge) {
                BlockShrivingCharge bCharge = stack.get(ModComponents.BLOCK_CHARGE);
                if (bCharge != null && bCharge.requiredBlocks() > 0) {
                    float bProgress = (float) bCharge.currentBlocks() / bCharge.requiredBlocks();
                    int bFilled = Math.round(bProgress * 10);

                    MutableText bBar = Text.literal("[ ").formatted(Formatting.DARK_GRAY);
                    for (int i = 0; i < bFilled; i++) bBar.append(Text.literal("█").formatted(Formatting.GOLD));
                    for (int i = bFilled; i < 10; i++) bBar.append(Text.literal("░").formatted(Formatting.DARK_GRAY));
                    bBar.append(Text.literal(" ] ").formatted(Formatting.DARK_GRAY));

                    MutableText bCount = Text.literal(bCharge.currentBlocks() + " / " + bCharge.requiredBlocks() + " Blocks").formatted(Formatting.GOLD);

                    finalOrderedTooltip.add(Text.empty());
                    finalOrderedTooltip.add(Text.empty().append(bBar).append(bCount));
                }
            }

            // 3. BLESSED AURA DEVOTION PROGRESS TRACKER (CYAN)
            if (hasBlessedCharge) {
                BlessedCharge blCharge = stack.get(ModComponents.BLESSED_CHARGE);
                if (blCharge != null && blCharge.requiredPoints() > 0) {
                    float blProgress = (float) blCharge.currentPoints() / blCharge.requiredPoints();
                    int blFilled = Math.round(blProgress * 10);

                    MutableText blBar = Text.literal("[ ").formatted(Formatting.DARK_GRAY);
                    for (int i = 0; i < blFilled; i++) blBar.append(Text.literal("█").formatted(Formatting.AQUA));
                    for (int i = blFilled; i < 10; i++) blBar.append(Text.literal("░").formatted(Formatting.DARK_GRAY));
                    blBar.append(Text.literal(" ] ").formatted(Formatting.DARK_GRAY));

                    MutableText blCount = Text.literal(blCharge.currentPoints() + " / " + blCharge.requiredPoints() + " Devotion").formatted(Formatting.AQUA);

                    finalOrderedTooltip.add(Text.empty());
                    finalOrderedTooltip.add(Text.empty().append(blBar).append(blCount));
                }
            }

            cir.setReturnValue(finalOrderedTooltip);

        } finally {
            eyelisspartmod$isSortingTooltip.set(false);
        }
    }

    // =========================================================================
    // PART 5: TOOL SPEED BALANCERS MATRIX ENGINE
    // =========================================================================
    @Inject(method = "getMiningSpeedMultiplier(Lnet/minecraft/block/BlockState;)F", at = @At("RETURN"), cancellable = true)
    private void injectCustomToolEngravingBreakSpeeds(net.minecraft.block.BlockState state, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = (ItemStack)(Object)this;
        List<EngravingContents> engravings = stack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of());
        if (engravings.isEmpty()) return;

        float speedMultiplier = cir.getReturnValue();

        for (EngravingContents e : engravings) {
            String id = e.engravingId();

            if (id.equals("ethereal") && !stack.isSuitableFor(state)) {
                speedMultiplier = Math.max(speedMultiplier, 3.5f);
            }

            if (id.equals("dwarven") && stack.isSuitableFor(state)) {
                net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
                if (client.player != null) {
                    double currentY = client.player.getY();
                    float dwarvenBoost = 0.0f;

                    if (currentY <= 0) {
                        dwarvenBoost = 0.33f;
                    } else if (currentY < 64) {
                        dwarvenBoost = (float) ((64.0 - currentY) / 64.0) * 0.33f;
                    }
                    speedMultiplier *= (1.0f + dwarvenBoost);
                }
            }
        }

        cir.setReturnValue(speedMultiplier);
    }
}
