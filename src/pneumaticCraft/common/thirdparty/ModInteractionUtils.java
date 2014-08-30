package pneumaticCraft.common.thirdparty;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.block.tubes.IPneumaticPosProvider;
import pneumaticCraft.common.item.ItemTubeModule;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;

public class ModInteractionUtils{
    private static final ModInteractionUtils INSTANCE = new ModInteractionUtilImplementation();

    public static ModInteractionUtils getInstance(){
        return INSTANCE;
    }

    public boolean isWrench(Item item){
        return false;
    }

    /**
     * Used to get a IPneumaticMachine if the TE is one. When FMP is installed this will also look for a multipart containing IPneumaticMachine.
     * @param te
     * @return
     */
    public IPneumaticMachine getMachine(TileEntity te){
        return te instanceof IPneumaticMachine ? (IPneumaticMachine)te : null;
    }

    public Item getModuleItem(String moduleName){
        return new ItemTubeModule(moduleName);
    }

    public void registerModulePart(String partName){}

    public boolean isMultipart(TileEntity te){
        return false;
    }

    public ItemStack exportStackToTEPipe(TileEntity te, ItemStack stack, ForgeDirection side){
        return stack;
    }

    public ItemStack exportStackToBCPipe(TileEntity te, ItemStack stack, ForgeDirection side){
        return stack;
    }

    public boolean isBCPipe(TileEntity te){
        return false;
    }

    public boolean isTEPipe(TileEntity te){
        return false;
    }

    public boolean isMultipartWiseConnected(TileEntity te, ForgeDirection dir){
        return false;
    }

    public void sendDescriptionPacket(IPneumaticPosProvider te){
        ((TileEntityPressureTube)te).sendDescriptionPacket();
    }
}
