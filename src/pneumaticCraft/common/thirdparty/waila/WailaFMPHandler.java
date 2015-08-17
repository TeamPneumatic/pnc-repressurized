package pneumaticCraft.common.thirdparty.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaFMPAccessor;
import mcp.mobius.waila.api.IWailaFMPProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.common.block.BlockPressureTube;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;

public class WailaFMPHandler implements IWailaFMPProvider{

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaFMPAccessor accessor, IWailaConfigHandler config){
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaFMPAccessor accessor, IWailaConfigHandler config){
        TileEntityPressureTube tube = ModInteractionUtils.getInstance().getTube(accessor.getTileEntity());
        if(tube != null) {
            NBTTagCompound tubeTag = accessor.getNBTData().getCompoundTag("tube");
            tube.currentAir = tubeTag.getInteger("currentAir");
            WailaPneumaticHandler.addTipToMachine(currenttip, tube);
            TubeModule module = BlockPressureTube.getLookedModule(accessor.getWorld(), accessor.getTileEntity().xCoord, accessor.getTileEntity().yCoord, accessor.getTileEntity().zCoord, accessor.getPlayer());
            if(module != null) {
                WailaTubeModuleHandler.addModuleInfo(currenttip, tube, tubeTag, module.getDirection());
            }
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaFMPAccessor accessor, IWailaConfigHandler config){
        return currenttip;
    }

}
