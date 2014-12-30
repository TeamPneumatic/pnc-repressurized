package pneumaticCraft.common.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.block.Blockss;

public class TileEntityDroneRedstoneEmitter extends TileEntity{
    @Override
    public void updateEntity(){
        for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            if(Blockss.droneRedstoneEmitter.isProvidingWeakPower(worldObj, xCoord, yCoord, zCoord, d.ordinal()) > 0) {
                return;
            }
        }
        worldObj.setBlockToAir(xCoord, yCoord, zCoord);
    }
}
