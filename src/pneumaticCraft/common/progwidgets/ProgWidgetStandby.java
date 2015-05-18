package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetStandby extends ProgWidget{
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
        return null;
    }

    @Override
    public String getWidgetString(){
        return "standby";
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.REPULSION_PLANT_DAMAGE;
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.EASY;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_STANDBY;
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return new DroneAIStandby((EntityDrone)drone);
    }

    public static class DroneAIStandby extends EntityAIBase{

        private final EntityDrone drone;

        public DroneAIStandby(EntityDrone drone){
            this.drone = drone;
        }

        @Override
        public boolean shouldExecute(){
            drone.setStandby(true);
            return false;
        }
    }
}
