package ca.naln1.rainflake.blockguide.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import ca.naln1.rainflake.blockguide.ConfigHandler;

public class EnableGuide implements Command<CommandSource> {

    public static final EnableGuide CMD = new EnableGuide();
    public static final String name = "enable";

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.argument(name, StringArgumentType.word())
                .suggests((context, builder) -> {
                    return builder.suggest("start")
                            .suggest("stop")
                            .buildFuture();
                })
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (StringArgumentType.getString(context,name).equals("start")){
            ConfigHandler.enableGuide.set(true);
        } else if (StringArgumentType.getString(context,name).equals("stop")){
            ConfigHandler.enableGuide.set(false);
        } else {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        }
        //ConfigHandler.COMMON.save();
        //ConfigHandler.enableGuide.save();
        return 0;
    }
}
