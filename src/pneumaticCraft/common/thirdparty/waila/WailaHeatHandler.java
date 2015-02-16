package pneumaticCraft.common.thirdparty.waila;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.SpecialChars;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.tileentity.IHeatExchanger;

public class WailaHeatHandler implements IWailaDataProvider{

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

        if(tag.hasKey("heat")) {
            NBTTagList tagList = tag.getTagList("heat", 10);
            for(int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound heatTag = tagList.getCompoundTagAt(i);
                values.put("waila.temperature." + ForgeDirection.getOrientation(heatTag.getByte("side")).toString().toLowerCase(), heatTag.getInteger("temp") - 273 + "C");
            }
        } else {
            values.put("waila.temperature", tag.getInteger("temp") - 273 + "C");
        }

        //Get all the values from the map and put them in the list.
        for(Map.Entry<String, String> entry : values.entrySet()) {
            currenttip.add(I18n.format(entry.getKey()) + ": " + /*SpecialChars.ALIGNRIGHT +*/SpecialChars.WHITE + entry.getValue());
        }
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config){
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z){
        if(te instanceof IHeatExchanger) {
            Set<IHeatExchangerLogic> heatExchangers = new HashSet<IHeatExchangerLogic>();
            IHeatExchangerLogic logic = null;
            boolean isMultisided = true;
            for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                logic = ((IHeatExchanger)te).getHeatExchangerLogic(d);
                if(logic != null) {
                    if(heatExchangers.contains(logic)) {
                        isMultisided = false;
                        break;
                    } else {
                        heatExchangers.add(logic);
                    }
                }
            }

            if(isMultisided) {
                NBTTagList tagList = new NBTTagList();
                for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                    logic = ((IHeatExchanger)te).getHeatExchangerLogic(d);
                    if(logic != null) {
                        NBTTagCompound heatTag = new NBTTagCompound();
                        heatTag.setByte("side", (byte)d.ordinal());
                        heatTag.setInteger("temp", (int)logic.getTemperature());
                        tagList.appendTag(heatTag);
                    }
                }
                tag.setTag("heat", tagList);
            } else if(logic != null) {
                tag.setInteger("temp", (int)logic.getTemperature());
            }
        }
        return tag;
    }
}
