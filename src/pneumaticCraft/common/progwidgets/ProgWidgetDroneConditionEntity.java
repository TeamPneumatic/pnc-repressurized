package pneumaticCraft.common.progwidgets;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetDroneConditionEntity extends ProgWidgetDroneEvaluation{

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetString.class, ProgWidgetString.class};
    }

    @Override
    public String getWidgetString(){
        return "droneConditionEntity";
    }

    @Override
    protected int getCount(EntityDrone drone){
        return drone.riddenByEntity == null || !ProgWidgetAreaItemBase.getEntityFilter((ProgWidgetString)getConnectedParameters()[0], true).isEntityApplicable(drone.riddenByEntity) || ProgWidgetAreaItemBase.getEntityFilter((ProgWidgetString)getConnectedParameters()[2], false).isEntityApplicable(drone.riddenByEntity) ? 0 : 1;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_DRONE_ENTITY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return null;
    }

}
