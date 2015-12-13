package pneumaticCraft.common.progwidgets;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.ai.DroneAIBlockCondition;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.lib.Textures;

public class ProgWidgetLightCondition extends ProgWidgetCondition{

    @Override
    public String getWidgetString(){
        return "conditionLight";
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_LIGHT;
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget){
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase)widget){

            @Override
            protected boolean evaluate(ChunkPosition pos){
                int lightLevel = drone.getWorld().getBlockLightValue(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
                int requiredLight = ((ICondition)widget).getRequiredCount();
                return ((ICondition)widget).getOperator() == ICondition.Operator.EQUALS ? requiredLight == lightLevel : lightLevel >= requiredLight;
            }

        };
    }
}
