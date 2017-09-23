package me.desht.pneumaticcraft.common.commands;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandAmazonDelivery extends CommandBase {

    @Override
    public String getName() {
        return "deliverAmazon";
    }

    @Override
    public String getUsage(ICommandSender p_71518_1_) {
        return "deliverAmazon <x> <y> <z> <inventoryX> <inventorY> <inventoryZ> OR deliverAmazon <player> <inventoryX> <inventorY> <inventoryZ>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) throw new WrongUsageException("command.deliverAmazon.args");
        int x, y, z;
        int curArg;
        String regex = "-?\\d+";
        if (args[0].matches(regex)) {
            if (args.length < 4) throw new WrongUsageException("command.deliverAmazon.args");
            if (!args[1].matches(regex) || !args[2].matches(regex))
                throw new WrongUsageException("command.deliverAmazon.coords");
            x = Integer.parseInt(args[0]);
            y = Integer.parseInt(args[1]);
            z = Integer.parseInt(args[2]);
            curArg = 3;
        } else {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(args[0]);
            if (player != null) {
                x = (int) Math.floor(player.posX);
                y = (int) Math.floor(player.posY) + 1;
                z = (int) Math.floor(player.posZ);
                curArg = 1;
            } else {
                throw new WrongUsageException("command.deliverAmazon.playerName");
            }
        }

        // FIXME use IItemHandler
        if (args.length < curArg + 3) throw new WrongUsageException("command.deliverAmazon.args");
        if (!args[curArg].matches(regex) || !args[curArg + 1].matches(regex) || !args[curArg + 2].matches(regex))
            throw new WrongUsageException("command.deliverAmazon.coords");
        TileEntity te = sender.getEntityWorld().getTileEntity(new BlockPos(Integer.parseInt(args[curArg]), Integer.parseInt(args[curArg + 1]), Integer.parseInt(args[curArg + 2])));
        IItemHandler inv = IOHelper.getInventoryForTE(te);
        if (inv != null) {
            List<ItemStack> deliveredStacks = new ArrayList<ItemStack>();
            for (int i = 0; i < inv.getSlots() && deliveredStacks.size() < 65; i++) {
                if (!inv.getStackInSlot(i).isEmpty()) deliveredStacks.add(inv.getStackInSlot(i));
            }
            if (deliveredStacks.size() > 0) {
                PneumaticRegistry.getInstance().getDroneRegistry().deliverItemsAmazonStyle(sender.getEntityWorld(), new BlockPos(x, y, z), deliveredStacks.toArray(new ItemStack[deliveredStacks.size()]));
                sender.sendMessage(new TextComponentString("command.deliverAmazon.success"));
            } else {
                sender.sendMessage(new TextComponentString("command.deliverAmazon.noItems"));
            }
        } else {
            throw new WrongUsageException("command.deliverAmazon.noInventory");
        }

    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length >= 1 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : null;
    }
}
