package eyeliss.particle.mod.api.engraving;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import eyeliss.particle.mod.component.*;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EngraveCommandExecutor {

    public static final List<String> KNOWN_ENGRAVINGS = List.of(
            "absorption", "crushing", "fortitude", "lightweight", "pulverizing",
            "sturdy", "restoration", "transcendence", "wisdom", "stagnation", "ruin",
            "ethereal", "dwarven", "shattering",
            "magic_touch", "exorcism", "sweeping", "truth", "blessed", "hasted"
    );

    public static CompletableFuture<Suggestions> suggestEngravingNames(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(KNOWN_ENGRAVINGS, builder);
    }

    public static int executeAddEngraving(com.mojang.brigadier.context.CommandContext<net.minecraft.server.command.ServerCommandSource> context, boolean incomingDwarvenTouchParam) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        net.minecraft.server.network.ServerPlayerEntity targetPlayer = net.minecraft.command.argument.EntityArgumentType.getPlayer(context, "target");
        String engName = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "engraving_name");

        ItemStack stack = targetPlayer.getMainHandStack();
        if (stack.isEmpty()) {
            context.getSource().sendError(net.minecraft.text.Text.literal("Target player must be holding an item in their main hand!"));
            return 0;
        }

        if (incomingDwarvenTouchParam) {
            if (stack.getOrDefault(eyeliss.particle.mod.component.ModComponents.DWARVEN_TOUCH, false)) {
                context.getSource().sendError(net.minecraft.text.Text.literal("Operation Aborted! This item already has a permanent Dwarven Touch applied. Only non-dwarven modifications are allowed."));
                return 0;
            }
        }

        List<EngravingContents> list = new ArrayList<>(stack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of()));

        int existingLevel = 0;
        for (EngravingContents e : list) {
            if (e.engravingId().equalsIgnoreCase(engName)) {
                existingLevel = e.level();
                break;
            }
        }

        list.removeIf(e -> e.engravingId().equalsIgnoreCase(engName));

        int newTargetLevel = existingLevel + 1;

        boolean isCappedAtOne = eyeliss.particle.mod.component.ModEngravings.isBlessingOrCurse(engName);
        if (isCappedAtOne && newTargetLevel > 1) {
            newTargetLevel = 1;
            context.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("Note: '" + engName + "' is a single-level trait and cannot be leveled past I."), false);
        }

        list.add(new EngravingContents(engName, newTargetLevel));
        stack.set(ModComponents.ENGRAVING_CONTENTS, list);

        if (stack.contains(net.minecraft.component.DataComponentTypes.ENCHANTMENTS)) {
            var enchants = stack.get(net.minecraft.component.DataComponentTypes.ENCHANTMENTS);
            stack.set(net.minecraft.component.DataComponentTypes.ENCHANTMENTS, enchants);
        } else {
            stack.set(net.minecraft.component.DataComponentTypes.ENCHANTMENTS, net.minecraft.component.type.ItemEnchantmentsComponent.DEFAULT);
        }

        // 4. APPLY PERMANENT COMPONENT DATA ATTRIBUTES
        if (incomingDwarvenTouchParam) {
            stack.set(eyeliss.particle.mod.component.ModComponents.DWARVEN_TOUCH, true);
        }

        // Initialize progress bar properties if the admin applied "blessed" for the first time
        if (engName.equalsIgnoreCase("blessed") && !stack.contains(ModComponents.BLESSED_CHARGE)) {
            int maxPointsCeilingValue = 300;
            stack.set(ModComponents.BLESSED_CHARGE, new BlessedCharge(0, maxPointsCeilingValue));
        }

        final int finalDisplayLevel = newTargetLevel;
        context.getSource().sendFeedback(() -> net.minecraft.text.Text.literal("Successfully applied '" + engName + "' at Level " + finalDisplayLevel + (incomingDwarvenTouchParam ? " with permanent Dwarven Touch!" : ".")), true);
        return 1;
    }

    public static int executeRemoveEngraving(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target");
        String engName = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "engraving_name");

        ItemStack stack = targetPlayer.getMainHandStack();
        if (stack.isEmpty()) return 0;

        List<EngravingContents> list = new ArrayList<>(stack.getOrDefault(ModComponents.ENGRAVING_CONTENTS, List.of()));
        boolean removed = list.removeIf(e -> e.engravingId().equals(engName));

        if (removed) {
            stack.set(ModComponents.ENGRAVING_CONTENTS, list);

            // Clean up its progression tracking component bar as well if the core trait is stripped
            if (engName.equalsIgnoreCase("blessed")) {
                stack.remove(ModComponents.BLESSED_CHARGE);
            }
            context.getSource().sendFeedback(() -> Text.literal("Successfully removed engraving '" + engName + "' from target item."), true);
        } else {
            context.getSource().sendError(Text.literal("Target item does not carry that engraving type."));
        }
        return 1;
    }

    public static int executeSetKills(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target");
        int amount = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "amount");

        ItemStack stack = targetPlayer.getMainHandStack();
        if (stack.isEmpty() || !stack.contains(ModComponents.SHRIVING_CHARGE)) {
            context.getSource().sendError(Text.literal("Target item is not carrying an active Blood Shriving project."));
            return 0;
        }

        BloodShrivingCharge charge = stack.get(ModComponents.SHRIVING_CHARGE);
        if (charge != null) {
            int setAmount = Math.min(amount, charge.requiredKills());
            stack.set(ModComponents.SHRIVING_CHARGE, new BloodShrivingCharge(setAmount, charge.requiredKills()));
            context.getSource().sendFeedback(() -> Text.literal("Updated Blood points to: " + setAmount), true);
        }
        return 1;
    }

    public static int executeRemoveShriving(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target");
        ItemStack stack = targetPlayer.getMainHandStack();

        if (!stack.isEmpty()) {
            stack.remove(ModComponents.SHRIVING_CHARGE);
            stack.remove(ModComponents.BLOCK_CHARGE);
            stack.remove(ModComponents.BLESSED_CHARGE);
            context.getSource().sendFeedback(() -> Text.literal("Successfully stripped all active progression projects from target item."), true);
        }
        return 1;
    }

    public static int executeSetBlocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target");
        int amount = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "amount");

        ItemStack stack = targetPlayer.getMainHandStack();
        if (stack.isEmpty() || !stack.contains(ModComponents.BLOCK_CHARGE)) {
            context.getSource().sendError(Text.literal("Target item is not carrying an active Geologic Shriving project."));
            return 0;
        }

        BlockShrivingCharge charge = stack.get(ModComponents.BLOCK_CHARGE);
        if (charge != null) {
            int setAmount = Math.min(amount, charge.requiredBlocks());
            stack.set(ModComponents.BLOCK_CHARGE, new BlockShrivingCharge(setAmount, charge.requiredBlocks()));
            context.getSource().sendFeedback(() -> Text.literal("Updated Geologic points to: " + setAmount), true);
        }
        return 1;
    }

    public static int executeSetDevotion(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target");
        int amount = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "amount");

        ItemStack stack = targetPlayer.getMainHandStack();
        if (stack.isEmpty() || !stack.contains(ModComponents.BLESSED_CHARGE)) {
            context.getSource().sendError(Text.literal("Target player is not holding an item with an active Blessed Aura project."));
            return 0;
        }

        BlessedCharge current = stack.get(ModComponents.BLESSED_CHARGE);
        if (current != null) {
            int setAmount = Math.min(amount, current.requiredPoints());
            stack.set(ModComponents.BLESSED_CHARGE, new BlessedCharge(setAmount, current.requiredPoints()));
            context.getSource().sendFeedback(() -> Text.literal("Successfully updated " + targetPlayer.getName().getString() + "'s Devotion points to: " + setAmount), true);
        }
        return 1;
    }
}
