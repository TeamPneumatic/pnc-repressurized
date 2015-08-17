package pneumaticCraft.common.thirdparty.waila;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class WailaPneumaticHandler implements IWailaDataProvider{

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
        NBTTagCompound tCompound = accessor.getNBTData();
        TileEntity te = accessor.getTileEntity();
        if(te instanceof IPneumaticMachine) {
            addTipToMachine(currenttip, (IPneumaticMachine)te, tCompound.getFloat("pressure"));
        }
    }

    public static void addTipToMachine(List<String> currenttip, IPneumaticMachine machine){
        addTipToMachine(currenttip, machine, machine.getAirHandler().getPressure(ForgeDirection.UNKNOWN));
    }

    public static void addTipToMachine(List<String> currenttip, IPneumaticMachine machine, float pressure){
        //This is used so that we can split values later easier and have them all in the same layout.
        Map<String, String> values = new HashMap<String, String>();

        values.put("Pressure", PneumaticCraftUtils.roundNumberTo(pressure, 1) + " bar");

        TileEntityPneumaticBase base = (TileEntityPneumaticBase)machine.getAirHandler();
        values.put("Max Pressure", base.DANGER_PRESSURE + " bar");

        //Get all the values from the map and put them in the list.
        for(Map.Entry<String, String> entry : values.entrySet()) {
            currenttip.add(entry.getKey() + ": " + /*SpecialChars.ALIGNRIGHT +*/SpecialChars.WHITE + entry.getValue());
        }
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config){
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z){
        if(te instanceof IPneumaticMachine) {
            tag.setFloat("pressure", ((IPneumaticMachine)te).getAirHandler().getPressure(ForgeDirection.UNKNOWN));
        }
        return tag;
    }
}
