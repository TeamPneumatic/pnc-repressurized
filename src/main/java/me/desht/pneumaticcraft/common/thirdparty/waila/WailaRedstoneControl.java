package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.common.tileentity.IRedstoneControl;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WailaRedstoneControl implements IWailaDataProvider {
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return ItemStack.EMPTY;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        addTipToMachine(currenttip, accessor);
        return currenttip;
    }

    public static void addTipToMachine(List<String> currenttip, IWailaDataAccessor accessor) {
        NBTTagCompound tag = accessor.getNBTData();
        //This is used so that we can split values later easier and have them all in the same layout.
        Map<String, String> values = new HashMap<>();

        if (tag.hasKey("redstoneMode")) {
            int mode = tag.getInteger("redstoneMode");
            GuiPneumaticContainerBase gui = (GuiPneumaticContainerBase) PneumaticCraftRepressurized.proxy.getClientGuiElement(((BlockPneumaticCraft) accessor.getBlock()).getGuiID().ordinal(),
                    accessor.getPlayer(), accessor.getWorld(), accessor.getPosition().getX(), accessor.getPosition().getY(), accessor.getPosition().getZ());
            if (gui != null) {
                values.put(gui.getRedstoneString(), gui.getRedstoneButtonText(mode));
            }
        }

        //Get all the values from the map and put them in the list.
        for (Map.Entry<String, String> entry : values.entrySet()) {
            currenttip.add(TextFormatting.RED + I18n.format(entry.getKey()) + ": " + I18n.format(entry.getValue()));
        }
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (te instanceof IRedstoneControl) {
            tag.setInteger("redstoneMode", ((IRedstoneControl) te).getRedstoneMode());
        }
        return tag;
    }
}
