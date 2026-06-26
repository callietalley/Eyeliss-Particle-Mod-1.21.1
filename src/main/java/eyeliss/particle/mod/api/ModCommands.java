package eyeliss.particle.mod.api;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import eyeliss.particle.mod.api.engraving.EngraveCommandExecutor;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ModCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(ModCommands::buildTree);
    }

    private static void buildTree(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment env) {

        var limitBranch = CommandManager.literal("limit");
        var cooldownBranch = CommandManager.literal("cooldown");

        dispatcher.register(CommandManager.literal("eyeliss")
                .requires(source -> source.hasPermissionLevel(2))
                .then(limitBranch)
                .then(cooldownBranch)
        );

        // --- ROOT PATHWAY DIRECTORY: /engrave ---
        dispatcher.register(CommandManager.literal("engrave")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("target", EntityArgumentType.player())

                        // BRANCH 1: /engrave <target> engraving ...
                        .then(CommandManager.literal("engraving")
                                .then(CommandManager.literal("add")
                                        .then(CommandManager.argument("engraving_name", StringArgumentType.word())
                                                .suggests(EngraveCommandExecutor::suggestEngravingNames)
                                                .executes(context -> EngraveCommandExecutor.executeAddEngraving(context, false))
                                                .then(CommandManager.argument("dwarven_touch", BoolArgumentType.bool())
                                                        .executes(context -> EngraveCommandExecutor.executeAddEngraving(context, BoolArgumentType.getBool(context, "dwarven_touch"))))))
                                .then(CommandManager.literal("remove")
                                        .then(CommandManager.argument("engraving_name", StringArgumentType.word())
                                                .suggests(EngraveCommandExecutor::suggestEngravingNames)
                                                .executes(EngraveCommandExecutor::executeRemoveEngraving))))

                        // BRANCH 2: /engrave <target> kills ...
                        .then(CommandManager.literal("kills")
                                .then(CommandManager.literal("set")
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(EngraveCommandExecutor::executeSetKills)))
                                .then(CommandManager.literal("remove")
                                        .executes(EngraveCommandExecutor::executeRemoveShriving)))

                        // BRANCH 3: /engrave <target> blocks ...
                        .then(CommandManager.literal("blocks")
                                .then(CommandManager.literal("set")
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(EngraveCommandExecutor::executeSetBlocks))))

                        // NEW BRANCH 4: /engrave <target> devotion ...
                        // FIX: Grants full operator control over your new single-level Blessed project bars
                        .then(CommandManager.literal("devotion")
                                .then(CommandManager.literal("set")
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(EngraveCommandExecutor::executeSetDevotion))))));
    }
}
