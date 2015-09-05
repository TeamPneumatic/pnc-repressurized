package pneumaticCraft.common.commands;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.common.util.IOHelper;

public class CommandAmazonDelivery extends CommandBase{

    @Override
    public String getCommandName(){
        return "deliverAmazon";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_){
        return "deliverAmazon <x> <y> <z> <inventoryX> <inventorY> <inventoryZ> OR deliverAmazon <player> <inventoryX> <inventorY> <inventoryZ>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args){
        if(args.length < 2) throw new WrongUsageException("command.deliverAmazon.args");
        int x, y, z;
        int curArg;
        String regex = "-?\\d+";
        if(args[0].matches(regex)) {
            if(args.length < 4) throw new WrongUsageException("command.deliverAmazon.args");
            if(!args[1].matches(regex) || !args[2].matches(regex)) throw new WrongUsageException("command.deliverAmazon.coords");
            x = Integer.parseInt(args[0]);
            y = Integer.parseInt(args[1]);
            z = Integer.parseInt(args[2]);
            curArg = 3;
        } else {
            EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[0]);
            if(player != null) {
                x = (int)Math.floor(player.posX);
                y = (int)Math.floor(player.posY) + 1;
                z = (int)Math.floor(player.posZ);
                curArg = 1;
            } else {
                throw new WrongUsageException("command.deliverAmazon.playerName");
            }
        }

        if(args.length < curArg + 3) throw new WrongUsageException("command.deliverAmazon.args");
        if(!args[curArg].matches(regex) || !args[curArg + 1].matches(regex) || !args[curArg + 2].matches(regex)) throw new WrongUsageException("command.deliverAmazon.coords");
        TileEntity te = sender.getEntityWorld().getTileEntity(Integer.parseInt(args[curArg]), Integer.parseInt(args[curArg + 1]), Integer.parseInt(args[curArg + 2]));
        IInventory inv = IOHelper.getInventoryForTE(te);
        if(inv != null) {
            List<ItemStack> deliveredStacks = new ArrayList<ItemStack>();
            for(int i = 0; i < inv.getSizeInventory() && deliveredStacks.size() < 65; i++) {
                if(inv.getStackInSlot(i) != null) deliveredStacks.add(inv.getStackInSlot(i));
            }
            if(deliveredStacks.size() > 0) {
                PneumaticRegistry.getInstance().deliverItemsAmazonStyle(sender.getEntityWorld(), x, y, z, deliveredStacks.toArray(new ItemStack[deliveredStacks.size()]));
                sender.addChatMessage(new ChatComponentTranslation("command.deliverAmazon.success"));
            } else {
                sender.addChatMessage(new ChatComponentTranslation("command.deliverAmazon.noItems"));
            }
        } else {
            throw new WrongUsageException("command.deliverAmazon.noInventory");
        }

    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    @Override
    public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_){
        return p_71516_2_.length >= 1 ? getListOfStringsMatchingLastWord(p_71516_2_, MinecraftServer.getServer().getAllUsernames()) : null;
    }

}
