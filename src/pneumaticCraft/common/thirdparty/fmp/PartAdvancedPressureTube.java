package pneumaticCraft.common.thirdparty.fmp;

import net.minecraft.item.ItemStack;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;
import pneumaticCraft.lib.PneumaticValues;

public class PartAdvancedPressureTube extends PartPressureTube{
    public PartAdvancedPressureTube(){}

    public PartAdvancedPressureTube(TileEntityPressureTube tube){
        super(tube);
    }

    @Override
    public ItemStack getItem(){
        return new ItemStack(Blockss.advancedPressureTube);
    }

    @Override
    public String getType(){
        return "tile.advancedPressureTube";
    }

    @Override
    protected TileEntityPressureTube getNewTube(){
        return new TileEntityPressureTube(PneumaticValues.DANGER_PRESSURE_ADVANCED_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_ADVANCED_PRESSURE_TUBE, PneumaticValues.VOLUME_ADVANCED_PRESSURE_TUBE);
    }
}
