package eyeliss.particle.mod.recipe;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.stream.Collectors;

public class CraftLimitClearCommand {

    public static final SuggestionProvider<ServerCommandSource> LIMITED_RECIPE_SUGGESTIONS = (context, builder) -> {
        ServerCommandSource source = context.getSource();
        if (source.getServer() != null) {
            var customIds = source.getServer().getRecipeManager().values().stream()
                    .filter(entry -> entry.value() instanceof HardLimitedRecipe)
                    .map(RecipeEntry::id)
                    .map(Identifier::toString)
                    .collect(Collectors.toList());

            return CommandSource.suggestMatching(customIds, builder);
        }
        return builder.buildFuture();
    };

    public static int resetAllCounters(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getServer() != null) {
            CraftCounterState state = CraftCounterState.getServerState(source.getServer());
            state.clearAllCounts();

            source.sendFeedback(() -> Text.literal("[Success] All server-wide and player recipe limits have been reset!").copy().formatted(Formatting.GREEN), true);
            return 1;
        }
        return 0;
    }

    public static int resetSpecificCounter(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getServer() != null) {
            Identifier recipeId = IdentifierArgumentType.getIdentifier(context, "recipe_id");
            String idString = recipeId.toString();

            CraftCounterState state = CraftCounterState.getServerState(source.getServer());
            state.clearSpecificCount(idString);

            source.sendFeedback(() -> Text.literal("[Success] Limits for recipe ")
                    .copy().formatted(Formatting.GREEN)
                    .append(HardLimitedRecipe.getTranslatableName(idString).copy().formatted(Formatting.YELLOW))
                    .append(" have been completely reset!"), true);
            return 1;
        }
        return 0;
    }

    public static int resetPlayerCounter(CommandContext<ServerCommandSource> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        if (source.getServer() != null) {
            Identifier recipeId = IdentifierArgumentType.getIdentifier(context, "recipe_id");
            String idString = recipeId.toString();
            ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");

            CraftCounterState state = CraftCounterState.getServerState(source.getServer());
            state.setPlayerCount(idString, targetPlayer.getUuid(), 0);

            // FIXED: Appending the display name directly instead of mutating it avoids the IDE warning entirely
            source.sendFeedback(() -> Text.literal("[Success] Reset ")
                    .copy().formatted(Formatting.GREEN)
                    .append(targetPlayer.getDisplayName())
                    .append("'s craft count for ")
                    .append(HardLimitedRecipe.getTranslatableName(idString).copy().formatted(Formatting.YELLOW))
                    .append(" back to 0."), true);
            return 1;
        }
        return 0;
    }

    public static int setPlayerCounter(CommandContext<ServerCommandSource> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        if (source.getServer() != null) {
            Identifier recipeId = IdentifierArgumentType.getIdentifier(context, "recipe_id");
            String idString = recipeId.toString();
            ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
            int amount = IntegerArgumentType.getInteger(context, "amount");

            CraftCounterState state = CraftCounterState.getServerState(source.getServer());
            state.setPlayerCount(idString, targetPlayer.getUuid(), amount);

            // FIXED: Eliminated invalid copyOf call and handled the display name cleanly
            source.sendFeedback(() -> Text.literal("[Success] Set ")
                    .copy().formatted(Formatting.GREEN)
                    .append(targetPlayer.getDisplayName())
                    .append("'s craft count for ")
                    .append(HardLimitedRecipe.getTranslatableName(idString).copy().formatted(Formatting.YELLOW))
                    .append(" to ")
                    .append(Text.literal(String.valueOf(amount)).copy().formatted(Formatting.GOLD)), true);
            return 1;
        }
        return 0;
    }
}
