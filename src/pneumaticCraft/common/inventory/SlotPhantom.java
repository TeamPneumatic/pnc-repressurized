package pneumaticCraft.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

/**
 * 
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class SlotPhantom extends SlotBase implements IPhantomSlot{

    // used for filters
    SlotPhantom(IInventory par2IInventory, int par3, int par4, int par5){
        super(par2IInventory, par3, par4, par5);
    }

    @Override
    public boolean canShift(){
        return true;
    }

    @Override
    public boolean canTakeStack(EntityPlayer par1EntityPlayer){
        return false;
    }

    @Override
    public boolean canAdjust(){
        return true;
    }

}
