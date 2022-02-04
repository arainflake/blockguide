package ca.naln1.rainflake.blockguide.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import ca.naln1.rainflake.blockguide.BlockGuide;

public class CommandHandler {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> commandNode = dispatcher.register(
                Commands.literal(BlockGuide.MOD_ID)
                        .then(EnableGuide.register(dispatcher))
                        .then(ReadTooltips.register(dispatcher))
                        .then(ReadContainers.register(dispatcher))
                        .then(ReadPickups.register(dispatcher))
                        .then(ExtendedMode.register(dispatcher))
                        .then(ExtendedRange.register(dispatcher))
                        .then(ReadBlocks.register(dispatcher))
                        .then(ReadEntities.register(dispatcher))
        );

        dispatcher.register(Commands.literal("guide").redirect(commandNode));
    }
}
