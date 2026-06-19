package eyeliss.particle.mod.api;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import eyeliss.particle.mod.recipe.CraftLimitClearCommand;
import eyeliss.particle.mod.item.trinkets.util.BloodStoneTickHandler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(ModCommands::buildTree);
    }

    private static void buildTree(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment env) {

        // Updated limit branch to handle both global resets and targeting specific players
        var limitBranch = CommandManager.literal("limit")
                .then(CommandManager.literal("reset")
                        .executes(CraftLimitClearCommand::resetAllCounters)
                        .then(CommandManager.argument("recipe_id", IdentifierArgumentType.identifier())
                                .suggests(CraftLimitClearCommand.LIMITED_RECIPE_SUGGESTIONS)
                                .executes(CraftLimitClearCommand::resetSpecificCounter)
                                // NEW: Allows running /eyeliss limit reset <recipe_id> <player>
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(CraftLimitClearCommand::resetPlayerCounter))))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("recipe_id", IdentifierArgumentType.identifier())
                                .suggests(CraftLimitClearCommand.LIMITED_RECIPE_SUGGESTIONS)
                                // NEW: Configured to take a target player username before the amount argument
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(CraftLimitClearCommand::setPlayerCounter)))));

        var cooldownBranch = CommandManager.literal("cooldown")
                .then(CommandManager.literal("reset")
                        .then(CommandManager.literal("bloodstone")
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    ServerPlayerEntity player = source.getPlayer();
                                    if (player != null) {
                                        BloodStoneTickHandler.resetCooldown(player);
                                        source.sendFeedback(() -> Text.literal("Blood Stone cooldown reset!").copy().formatted(Formatting.RED), false);
                                        return 1;
                                    }
                                    source.sendError(Text.literal("Must be executed by a player."));
                                    return 0;
                                })));

        dispatcher.register(CommandManager.literal("eyeliss")
                .requires(source -> source.hasPermissionLevel(2))
                .then(limitBranch)
                .then(cooldownBranch)
        );
    }
}
