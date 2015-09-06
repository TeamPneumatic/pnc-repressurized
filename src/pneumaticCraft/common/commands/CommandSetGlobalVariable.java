package pneumaticCraft.common.commands;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.remote.GlobalVariableManager;

public class CommandSetGlobalVariable extends CommandBase{

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender){
        return true;
    }

    @Override
    public String getCommandName(){
        return "setGlobalVariable";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_){
        return "setGlobalVariable <variableName> <x> <y> <z>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args){
        if(args.length != 4) throw new WrongUsageException("command.deliverAmazon.args");
        String varName = args[0].startsWith("#") ? args[0].substring(1) : args[0];
        ChunkPosition newPos = new ChunkPosition(parseInt(sender, args[1]), parseInt(sender, args[2]), parseInt(sender, args[3]));
        GlobalVariableManager.getInstance().set(varName, newPos);
        sender.addChatMessage(new ChatComponentTranslation("command.setGlobalVariable.output", varName, newPos.chunkPosX, newPos.chunkPosY, newPos.chunkPosZ));
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    @Override
    public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_){
        return p_71516_2_.length >= 1 ? getListOfStringsMatchingLastWord(p_71516_2_, GlobalVariableManager.getInstance().getAllActiveVariableNames()) : null;
    }

}
