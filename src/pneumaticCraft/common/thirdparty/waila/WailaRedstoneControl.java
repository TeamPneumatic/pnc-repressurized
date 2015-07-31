package pneumaticCraft.common.thirdparty.waila;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.gui.GuiPneumaticContainerBase;
import pneumaticCraft.common.block.BlockPneumaticCraft;
import pneumaticCraft.common.tileentity.IRedstoneControl;

public class WailaRedstoneControl implements IWailaDataProvider{

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config){
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config){
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config){
        addTipToMachine(currenttip, accessor);
        return currenttip;
    }

    public static void addTipToMachine(List<String> currenttip, IWailaDataAccessor accessor){
        NBTTagCompound tag = accessor.getNBTData();
        //This is used so that we can split values later easier and have them all in the same layout.
        Map<String, String> values = new HashMap<String, String>();

        if(tag.hasKey("redstoneMode")) {
            int mode = tag.getInteger("redstoneMode");
            GuiPneumaticContainerBase gui = (GuiPneumaticContainerBase)PneumaticCraft.proxy.getClientGuiElement(((BlockPneumaticCraft)accessor.getBlock()).getGuiID().ordinal(), accessor.getPlayer(), accessor.getWorld(), accessor.getPosition().blockX, accessor.getPosition().blockY, accessor.getPosition().blockZ);
            if(gui != null) {
                values.put(gui.getRedstoneString(), gui.getRedstoneButtonText(mode));
            }
        }

        //Get all the values from the map and put them in the list.
        for(Map.Entry<String, String> entry : values.entrySet()) {
            currenttip.add(EnumChatFormatting.RED + I18n.format(entry.getKey()) + ": " + /*SpecialChars.ALIGNRIGHT +*/I18n.format(entry.getValue()));
        }
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config){
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z){
        if(te instanceof IRedstoneControl) {
            tag.setInteger("redstoneMode", ((IRedstoneControl)te).getRedstoneMode());
        }
        return tag;
    }

}
