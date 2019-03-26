package me.desht.pneumaticcraft.common.commands;

import me.desht.pneumaticcraft.common.util.NBTToJsonConverter;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandDumpNBT extends CommandBase {
    @Override
    public String getName() {
        return "dumpNBT";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "dumpNBT";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) {
            throw new WrongUsageException("dumpNBT");
        }

        if (sender instanceof EntityPlayer) {
            ItemStack held = ((EntityPlayer) sender).getHeldItemMainhand();
            if (!held.hasTagCompound()) {
                sender.sendMessage(new TextComponentString("No NBT"));
                return;
            }
            NBTToJsonConverter conv = new NBTToJsonConverter(held.getTagCompound());
            String msg = conv.convert(true);
            sender.sendMessage(new TextComponentString(msg));
        }
    }
}
