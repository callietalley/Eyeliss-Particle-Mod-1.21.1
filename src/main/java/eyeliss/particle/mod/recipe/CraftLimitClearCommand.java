package eyeliss.particle.mod.recipe;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.stream.Collectors;

public class CraftLimitClearCommand {

    private static final SuggestionProvider<ServerCommandSource> LIMITED_RECIPE_SUGGESTIONS = (context, builder) -> {
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

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess ignoredRegistryAccess, CommandManager.RegistrationEnvironment ignoredEnvironment) {
        dispatcher.register(CommandManager.literal("eyeliss")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("limit")
                        .then(CommandManager.literal("reset")
                                .executes(CraftLimitClearCommand::resetAllCounters)
                                .then(CommandManager.argument("recipe_id", IdentifierArgumentType.identifier())
                                        .suggests(LIMITED_RECIPE_SUGGESTIONS)
                                        .executes(CraftLimitClearCommand::resetSpecificCounter)
                                )
                        )
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("recipe_id", IdentifierArgumentType.identifier())
                                        .suggests(LIMITED_RECIPE_SUGGESTIONS)
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(CraftLimitClearCommand::setSpecificCounter)
                                        )
                                )
                        )
                )
        );
    }

    private static int resetAllCounters(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getServer() != null) {
            CraftCounterState state = CraftCounterState.getServerState(source.getServer());
            state.clearAllCounts();

            source.sendFeedback(() -> Text.literal("§a[Success] All server-wide recipe limits have been reset!§r"), true);
            return 1;
        }
        return 0;
    }

    private static int resetSpecificCounter(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getServer() != null) {
            Identifier recipeId = IdentifierArgumentType.getIdentifier(context, "recipe_id");
            String idString = recipeId.toString();

            CraftCounterState state = CraftCounterState.getServerState(source.getServer());
            state.clearSpecificCount(idString);

            // UPDATED: Appends the custom translatable name text component dynamically
            source.sendFeedback(() -> Text.literal("§a[Success] Limits for recipe ")
                    .append(HardLimitedRecipe.getTranslatableName(idString).formatted(Formatting.YELLOW))
                    .append(" have been completely reset!§r"), true);
            return 1;
        }
        return 0;
    }

    private static int setSpecificCounter(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getServer() != null) {
            Identifier recipeId = IdentifierArgumentType.getIdentifier(context, "recipe_id");
            String idString = recipeId.toString();
            int amount = IntegerArgumentType.getInteger(context, "amount");

            CraftCounterState state = CraftCounterState.getServerState(source.getServer());
            state.setSpecificCount(idString, amount);

            // UPDATED: Appends the custom translatable name text component dynamically
            source.sendFeedback(() -> Text.literal("§a[Success] Set current craft count for ")
                    .append(HardLimitedRecipe.getTranslatableName(idString).formatted(Formatting.YELLOW))
                    .append(" to §6" + amount + "§a.§r"), true);
            return 1;
        }
        return 0;
    }
}
