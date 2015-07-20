package pneumaticCraft.common.thirdparty.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pneumaticCraft.common.semiblock.ISemiBlock;
import pneumaticCraft.common.semiblock.SemiBlockBasic;
import pneumaticCraft.common.semiblock.SemiBlockManager;

public class WailaSemiBlockHandler implements IWailaDataProvider{

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
        int x = accessor.getPosition().blockX;
        int y = accessor.getPosition().blockY;
        int z = accessor.getPosition().blockZ;
        ISemiBlock semiBlock = SemiBlockManager.getInstance(accessor.getWorld()).getSemiBlock(accessor.getWorld(), x, y, z);
        if(semiBlock instanceof SemiBlockBasic) {
            ((SemiBlockBasic)semiBlock).addWailaTooltip(currenttip, accessor.getNBTData());
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config){
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z){
        ISemiBlock semiBlock = SemiBlockManager.getInstance(world).getSemiBlock(world, x, y, z);
        if(semiBlock instanceof SemiBlockBasic) {
            ((SemiBlockBasic)semiBlock).addWailaInfoToTag(tag);
        }
        return tag;
    }
}
