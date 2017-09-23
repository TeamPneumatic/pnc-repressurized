package me.desht.pneumaticcraft.common.commands;

import me.desht.pneumaticcraft.common.remote.GlobalVariableManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.List;

public class CommandSetGlobalVariable extends CommandBase {

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender par1ICommandSender) {
        return true;
    }

    @Override
    public String getName() {
        return "setGlobalVariable";
    }

    @Override
    public String getUsage(ICommandSender p_71518_1_) {
        return "setGlobalVariable <variableName> <x> <y> <z>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 4) throw new WrongUsageException("command.deliverAmazon.args");
        String varName = args[0].startsWith("#") ? args[0].substring(1) : args[0];
        BlockPos newPos = new BlockPos(parseInt(args[1]), parseInt(args[2]), parseInt(args[3]));
        GlobalVariableManager.getInstance().set(varName, newPos);
        sender.sendMessage(new TextComponentTranslation("command.setGlobalVariable.output", varName, newPos.getX(), newPos.getY(), newPos.getZ()));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length >= 1 ? getListOfStringsMatchingLastWord(args, GlobalVariableManager.getInstance().getAllActiveVariableNames()) : null;
    }
}
