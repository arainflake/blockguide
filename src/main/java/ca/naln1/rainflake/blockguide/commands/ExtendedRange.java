package ca.naln1.rainflake.blockguide.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import ca.naln1.rainflake.blockguide.ConfigHandler;

public class ExtendedRange implements Command<CommandSource> {
    public static final ExtendedRange CMD = new ExtendedRange();
    public static final String name = "extended_range";

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal(name)
                .then(Commands.argument(name, IntegerArgumentType.integer(6, 256))
                .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ConfigHandler.extendedRange.set(IntegerArgumentType.getInteger(context, name));
        return 0;
    }
}
