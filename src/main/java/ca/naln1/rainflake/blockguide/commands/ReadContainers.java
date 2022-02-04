package ca.naln1.rainflake.blockguide.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import ca.naln1.rainflake.blockguide.ConfigHandler;

public class ReadContainers implements Command<CommandSource> {
    public static final ReadContainers CMD = new ReadContainers();
    public static final String name = "containers";

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal(name).then(Commands.argument(name, BoolArgumentType.bool())
                .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ConfigHandler.outputContainers.set(BoolArgumentType.getBool(context, name));
        return 0;
    }
}
