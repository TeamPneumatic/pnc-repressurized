package me.desht.pneumaticcraft.common.commands;

import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketCommandGetGlobalVariableOutput;
import me.desht.pneumaticcraft.common.remote.GlobalVariableManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

public class CommandGetGlobalVariable extends CommandBase {
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public String getName() {
        return "getGlobalVariable";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "getGlobalVariable <variableName>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            if (args.length != 1) throw new WrongUsageException("command.deliverAmazon.args");
            String varName = args[0].startsWith("#") ? args[0].substring(1) : args[0];
            BlockPos pos = GlobalVariableManager.getInstance().getPos(varName);
            ItemStack stack = GlobalVariableManager.getInstance().getItem(varName);
            NetworkHandler.sendTo(new PacketCommandGetGlobalVariableOutput(varName, pos, stack), (EntityPlayerMP) sender);
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length >= 1 ? getListOfStringsMatchingLastWord(args, GlobalVariableManager.getInstance().getAllActiveVariableNames()) : null;
    }

}
