package pneumaticCraft.common.heat.behaviour;

import net.minecraft.block.BlockFurnace;
import net.minecraft.tileentity.TileEntityFurnace;
import pneumaticCraft.api.tileentity.HeatBehaviour;
import pneumaticCraft.lib.Names;

public class HeatBehaviourFurnace extends HeatBehaviour<TileEntityFurnace>{
    @Override
    public String getId(){
        return Names.MOD_ID + ":furnace";
    }

    @Override
    public boolean isApplicable(){
        return getTileEntity() instanceof TileEntityFurnace;
    }

    @Override
    public void update(){
        TileEntityFurnace furnace = getTileEntity();
        if(getHeatExchanger().getTemperature() > 373) {
            if(furnace.furnaceBurnTime < 190 && furnace.getStackInSlot(0) != null) {
                if(furnace.furnaceBurnTime == 0) BlockFurnace.updateFurnaceBlockState(true, furnace.getWorldObj(), furnace.xCoord, furnace.yCoord, furnace.zCoord);
                furnace.currentItemBurnTime = 200;
                furnace.furnaceBurnTime += 10;
                getHeatExchanger().addHeat(-1);
            }
            furnace.updateEntity();
        }
    }

}
