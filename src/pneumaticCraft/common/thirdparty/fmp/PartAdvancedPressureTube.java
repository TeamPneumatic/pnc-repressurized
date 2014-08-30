package pneumaticCraft.common.thirdparty.fmp;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;

public class PartAdvancedPressureTube extends PartPressureTube{
    public PartAdvancedPressureTube(){}

    public PartAdvancedPressureTube(TubeModule[] tubeModules){
        super(PneumaticValues.DANGER_PRESSURE_ADVANCED_PRESSURE_TUBE, PneumaticValues.MAX_PRESSURE_ADVANCED_PRESSURE_TUBE, PneumaticValues.VOLUME_ADVANCED_PRESSURE_TUBE);
        convertedModules = tubeModules;
    }

    @Override
    public ItemStack getItem(){
        return new ItemStack(Blockss.advancedPressureTube);
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.MODEL_ADVANCED_PRESSURE_TUBE;
    }

    @Override
    public String getType(){
        return "tile.advancedPressureTube";
    }
}
