package me.desht.pneumaticcraft.common.thirdparty.waila;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class WailaTubeModuleHandler implements IWailaDataProvider {
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
        EnumFacing face = accessor.getMOP().sideHit;
        addModuleInfo(currenttip, (TileEntityPressureTube) accessor.getTileEntity(), accessor.getNBTData(), face);
        return currenttip;
    }

    private static void addModuleInfo(List<String> currenttip, TileEntityPressureTube tube, NBTTagCompound tubeTag, EnumFacing face) {
        if (face != null) {
            NBTTagList moduleList = tubeTag.getTagList("modules", 10);
            for (int i = 0; i < moduleList.tagCount(); i++) {
                NBTTagCompound moduleTag = moduleList.getCompoundTagAt(i);
                if (face == EnumFacing.getFront(moduleTag.getInteger("side"))) {
                    if (tube != null && tube.modules[face.ordinal()] != null) {
                        TubeModule module = tube.modules[face.ordinal()];
                        module.readFromNBT(moduleTag);
                        module.addInfo(currenttip);
                    }
                }
            }
        }
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (te instanceof TileEntityPressureTube) {
            ((TileEntityPressureTube) te).writeModulesToNBT(tag);
        }
        return tag;
    }
}
