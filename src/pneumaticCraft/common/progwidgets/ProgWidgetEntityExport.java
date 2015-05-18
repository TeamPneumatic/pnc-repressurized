package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.ai.DroneAIBlockInteraction;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetEntityExport extends ProgWidgetAreaItemBase{

    @Override
    public String getWidgetString(){
        return "entityExport";
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_ENTITY_EX;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.PROPULSION_PLANT_DAMAGE;
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return new DroneAIBlockInteraction(drone, (ProgWidgetAreaItemBase)widget){

            @Override
            public boolean shouldExecute(){
                return drone.getCarryingEntity() == null || !widget.isEntityValid(drone.getCarryingEntity()) ? false : super.shouldExecute();
            }

            @Override
            protected boolean isValidPosition(ChunkPosition pos){
                return true;
            }

            @Override
            protected boolean moveIntoBlock(){
                return true;
            }

            @Override
            protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
                drone.setCarryingEntity(null);
                return false;
            }

        };
    }
}
