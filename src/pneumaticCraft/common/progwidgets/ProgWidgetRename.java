package pneumaticCraft.common.progwidgets;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetRename extends ProgWidget implements IRenamingWidget{

    @Override
    public boolean hasStepInput(){
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetString.class};
    }

    @Override
    public String getWidgetString(){
        return "rename";
    }

    @Override
    public String getGuiTabText(){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getGuiTabColor(){
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.FLYING_FLOWER_DAMAGE;
    }

    @Override
    public WidgetCategory getCategory(){
        return WidgetCategory.FLOW_CONTROL;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_RENAME;
    }

    @Override
    public EntityAIBase getWidgetAI(final EntityDrone drone, final IProgWidget widget){
        return new DroneAIRename(drone, (IRenamingWidget)widget);
    }

    private class DroneAIRename extends EntityAIBase{
        private final EntityDrone drone;
        private final IRenamingWidget widget;

        public DroneAIRename(EntityDrone drone, IRenamingWidget widget){
            this.drone = drone;
            this.widget = widget;
        }

        @Override
        public boolean shouldExecute(){
            drone.setCustomNameTag(widget.getNewName() != null ? widget.getNewName() : I18n.format("entity.PneumaticCraft.Drone.name"));
            return false;
        }

    }

    @Override
    public String getNewName(){
        return getConnectedParameters()[0] != null ? ((ProgWidgetString)getConnectedParameters()[0]).string : null;
    }

}
