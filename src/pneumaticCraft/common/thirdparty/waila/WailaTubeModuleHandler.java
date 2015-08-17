package pneumaticCraft.common.thirdparty.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;

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
        addModuleInfo(currenttip, (TileEntityPressureTube)accessor.getTileEntity(), accessor.getNBTData(), dir);
        return currenttip;
    }

    public static void addModuleInfo(List<String> currenttip, TileEntityPressureTube tube, NBTTagCompound tubeTag, ForgeDirection dir){
        if(dir != ForgeDirection.UNKNOWN) {
            NBTTagList moduleList = tubeTag.getTagList("modules", 10);
            for(int i = 0; i < moduleList.tagCount(); i++) {
                NBTTagCompound moduleTag = moduleList.getCompoundTagAt(i);
                if(dir == ForgeDirection.getOrientation(moduleTag.getInteger("side"))) {
                    if(tube != null && tube.modules[dir.ordinal()] != null) {
                        TubeModule module = tube.modules[dir.ordinal()];
                        module.readFromNBT(moduleTag);
                        module.addInfo(currenttip);
                    }
                }
            }
        }
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config){
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z){
        if(te instanceof TileEntityPressureTube) {
            ((TileEntityPressureTube)te).writeModulesToNBT(tag);
        }
        return tag;
    }

}
