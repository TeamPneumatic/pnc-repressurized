package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.ai.StringFilterEntitySelector;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetDroneConditionEntity extends ProgWidgetDroneEvaluation implements IEntityProvider{

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetString.class, ProgWidgetString.class};
    }

    @Override
    public String getWidgetString(){
        return "droneConditionEntity";
    }

    @Override
    protected int getCount(IDroneBase d, IProgWidget widget){
        EntityDrone drone = (EntityDrone)d;
        return drone.riddenByEntity == null || !((IEntityProvider)widget).isEntityValid(drone.riddenByEntity) ? 0 : 1;
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

    @Override
    public List<Entity> getValidEntities(World world){
        return new ArrayList<Entity>();
    }

    @Override
    public boolean isEntityValid(Entity entity){
        StringFilterEntitySelector whitelistFilter = ProgWidgetAreaItemBase.getEntityFilter((ProgWidgetString)getConnectedParameters()[0], true);
        StringFilterEntitySelector blacklistFilter = ProgWidgetAreaItemBase.getEntityFilter((ProgWidgetString)getConnectedParameters()[getParameters().length], false);
        return whitelistFilter.isEntityApplicable(entity) && !blacklistFilter.isEntityApplicable(entity);
    }

}
