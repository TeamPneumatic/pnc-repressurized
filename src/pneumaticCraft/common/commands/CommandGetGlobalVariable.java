package pneumaticCraft.common.commands;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketCommandGetGlobalVariableOutput;
import pneumaticCraft.common.remote.GlobalVariableManager;

public class CommandGetGlobalVariable extends CommandBase{

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender){
        return true;
    }

    @Override
    public String getCommandName(){
        return "getGlobalVariable";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_){
        return "getGlobalVariable <variableName>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args){
        if(sender instanceof EntityPlayerMP) {
            if(args.length != 1) throw new WrongUsageException("command.deliverAmazon.args");
            String varName = args[0].startsWith("#") ? args[0].substring(1) : args[0];
            ChunkPosition pos = GlobalVariableManager.getInstance().getPos(varName);
            ItemStack stack = GlobalVariableManager.getInstance().getItem(varName);
            NetworkHandler.sendTo(new PacketCommandGetGlobalVariableOutput(varName, pos, stack), (EntityPlayerMP)sender);
        }
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    @Override
    public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_){
        return p_71516_2_.length >= 1 ? getListOfStringsMatchingLastWord(p_71516_2_, GlobalVariableManager.getInstance().getAllActiveVariableNames()) : null;
    }

}
