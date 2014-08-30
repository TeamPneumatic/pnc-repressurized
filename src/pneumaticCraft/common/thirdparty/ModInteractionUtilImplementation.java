package pneumaticCraft.common.thirdparty;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.block.tubes.IPneumaticPosProvider;
import pneumaticCraft.common.thirdparty.fmp.FMP;
import pneumaticCraft.lib.ModIds;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipeTile;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.common.Optional;

public class ModInteractionUtilImplementation extends ModInteractionUtils{
    @Override
    @Optional.Method(modid = ModIds.BUILDCRAFT)
    public boolean isWrench(Item item){
        return item instanceof IToolWrench;
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

    /* @Override 
     @Optional.Method(modid = ModIds.FMP)
    FIXME public Item getModuleItem(String moduleName){
         return Config.convertMultipartsToBlocks ? super.getModuleItem(moduleName) : new ItemPartTubeModule(moduleName);
     }

    @Override
    @Optional.Method(modid = ModIds.FMP)
    public void registerModulePart(String partName){
        ThirdPartyManager.instance().registerPart(partName, PartTubeModule.class);
    }*/

    @Override
    @Optional.Method(modid = ModIds.FMP)
    public boolean isMultipart(TileEntity te){
        return te instanceof TileMultipart;
    }

    @Override
    @Optional.Method(modid = ModIds.FMP)
    public boolean isMultipartWiseConnected(TileEntity te, ForgeDirection dir){
        return FMP.getMultiPart((TileMultipart)te, IPneumaticMachine.class).isConnectedTo(dir);
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
}
