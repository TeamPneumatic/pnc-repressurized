package pneumaticCraft.common.thirdparty.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.block.tubes.ModuleRegistrator;
import pneumaticCraft.common.block.tubes.TubeModule;

public class WailaTubeModuleHandler implements IWailaDataProvider{

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
        ForgeDirection dir = (ForgeDirection)accessor.getPosition().hitInfo;
        if(dir != ForgeDirection.UNKNOWN) {
            NBTTagList moduleList = accessor.getNBTData().getTagList("modules", 10);
            for(int i = 0; i < moduleList.tagCount(); i++) {
                NBTTagCompound moduleTag = moduleList.getCompoundTagAt(i);
                if(dir == ForgeDirection.getOrientation(moduleTag.getInteger("side"))) {
                    TubeModule module = ModuleRegistrator.getModule(moduleTag.getString("type"));
                    module.readFromNBT(moduleTag);
                    module.addInfo(currenttip);
                }
            }
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config){
        return currenttip;
    }

}
