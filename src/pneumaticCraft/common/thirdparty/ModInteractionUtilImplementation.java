package pneumaticCraft.common.thirdparty;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.block.IPneumaticWrenchable;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.block.BlockPressureTube;
import pneumaticCraft.common.block.tubes.IPneumaticPosProvider;
import pneumaticCraft.common.thirdparty.fmp.FMP;
import pneumaticCraft.common.thirdparty.fmp.PartPressureTube;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;
import pneumaticCraft.lib.ModIds;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipeTile;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.NormallyOccludedPart;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cofh.api.item.IToolHammer;
import cpw.mods.fml.common.Optional;

public class ModInteractionUtilImplementation extends ModInteractionUtils{
    @Override
    @Optional.Method(modid = ModIds.BUILDCRAFT)
    protected boolean isBCWrench(Item item){
        return item instanceof IToolWrench;
    }

    @Override
    @Optional.Method(modid = ModIds.COFH_CORE)
    protected boolean isTEWrench(Item item){
        return item instanceof IToolHammer;
    }

    @Override
    @Optional.Method(modid = ModIds.BUILDCRAFT)
    public ItemStack exportStackToBCPipe(TileEntity te, ItemStack stack, ForgeDirection side){
        if(isBCPipe(te)) {
            int amount = ((IPipeTile)te).injectItem(stack, true, side);
            stack.stackSize -= amount;
            if(stack.stackSize <= 0) stack = null;
        }
        return stack;
    }

    @Override
    @Optional.Method(modid = ModIds.BUILDCRAFT)
    public boolean isBCPipe(TileEntity te){
        return te instanceof IPipeTile && ((IPipeTile)te).getPipeType() == IPipeTile.PipeType.ITEM;
    }

    @Override
    @Optional.Method(modid = ModIds.TE)
    public ItemStack exportStackToTEPipe(TileEntity te, ItemStack stack, ForgeDirection side){
        return stack;//TODO when TE updates for 1.7
    }

    @Override
    @Optional.Method(modid = ModIds.TE)
    public boolean isTEPipe(TileEntity te){
        return false;//TODO when TE updates for 1.7
    }

    /**
     *  ForgeMultipart
     */

    @Override
    @Optional.Method(modid = ModIds.FMP)
    public IPneumaticMachine getMachine(TileEntity te){
        if(te instanceof TileMultipart) {
            return FMP.getMultiPart((TileMultipart)te, IPneumaticMachine.class);
        } else {
            return super.getMachine(te);
        }
    }

    @Override
    @Optional.Method(modid = ModIds.FMP)
    public IPneumaticWrenchable getWrenchable(TileEntity te){
        if(te instanceof TileMultipart) {
            return FMP.getMultiPart((TileMultipart)te, IPneumaticWrenchable.class);
        } else {
            return super.getWrenchable(te);
        }
    }

    @Override
    @Optional.Method(modid = ModIds.FMP)
    public boolean isMultipart(TileEntity te){
        return te instanceof TileMultipart;
    }

    @Override
    @Optional.Method(modid = ModIds.FMP)
    public boolean isMultipartWiseConnected(Object part, ForgeDirection dir){
        return ((PartPressureTube)part).passesOcclusionTest(dir);
    }

    @Override
    @Optional.Method(modid = ModIds.FMP)
    public void sendDescriptionPacket(IPneumaticPosProvider te){
        if(te instanceof TMultiPart) {
            ((TMultiPart)te).sendDescUpdate();
        } else {
            super.sendDescriptionPacket(te);
        }
    }

    @Override
    @Optional.Method(modid = ModIds.FMP)
    public TileEntityPressureTube getTube(Object potentialTube){
        if(potentialTube instanceof PartPressureTube) {
            return ((PartPressureTube)potentialTube).getTube();
        } else if(potentialTube instanceof TileMultipart) {
            PartPressureTube tube = FMP.getMultiPart((TileMultipart)potentialTube, PartPressureTube.class);
            return tube != null ? tube.getTube() : null;
        } else {
            return super.getTube(potentialTube);
        }
    }

    @Override
    @Optional.Method(modid = ModIds.FMP)
    public void removeTube(TileEntity te){
        if(te instanceof TileMultipart) {
            PartPressureTube tube = FMP.getMultiPart((TileMultipart)te, PartPressureTube.class);
            if(tube != null) {
                List<ItemStack> drops = BlockPressureTube.getModuleDrops(tube.getTube());
                for(ItemStack drop : drops) {
                    EntityItem entity = new EntityItem(te.getWorldObj(), te.xCoord + 0.5, te.yCoord + 0.5, te.zCoord + 0.5);
                    entity.setEntityItemStack(drop);
                    te.getWorldObj().spawnEntityInWorld(entity);
                }
                ((TileMultipart)te).remPart(tube);
            }
        } else {
            super.removeTube(te);
        }
    }

    @Override
    @Optional.Method(modid = ModIds.FMP)
    public boolean occlusionTest(AxisAlignedBB aabb, TileEntity te){
        if(te instanceof TileMultipart) {
            return ((TileMultipart)te).occlusionTest(((TileMultipart)te).partList(), new NormallyOccludedPart(new Cuboid6(aabb)));
        } else {
            return super.occlusionTest(aabb, te);
        }
    }
}
