package me.desht.pneumaticcraft.common.heat.behaviour;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.BlockFurnace;
import net.minecraft.tileentity.TileEntityFurnace;

public class HeatBehaviourFurnace extends HeatBehaviour<TileEntityFurnace> {
    @Override
    public String getId() {
        return Names.MOD_ID + ":furnace";
    }

    @Override
    public boolean isApplicable() {
        return getTileEntity() instanceof TileEntityFurnace;
    }

    @Override
    public void update() {
        TileEntityFurnace furnace = getTileEntity();
        if (getHeatExchanger().getTemperature() > 373) {
            int furnaceBurnTime = furnace.getField(0);
            int furnaceCookTime = furnace.getField(2);
            if (furnaceBurnTime < 190 && !furnace.getStackInSlot(0).isEmpty()) {
                if (furnaceBurnTime == 0) BlockFurnace.setState(true, furnace.getWorld(), furnace.getPos());
                furnace.setField(1, 200); // currentItemBurnTime
                furnace.setField(0, furnaceBurnTime + 10); // furnaceBurnTime
                getHeatExchanger().addHeat(-1);
            }
            if (furnaceCookTime > 0) {
                // Easy performance saver, the Furnace won't be ticked unnecessary when there's nothing to
                // cook (or when just started cooking).
                int progress = Math.max(0, ((int) getHeatExchanger().getTemperature() - 343) / 30);
                progress = Math.min(5, progress);
                for (int i = 0; i < progress; i++) {
                    furnace.update();
                }
            }
        }
    }

}
