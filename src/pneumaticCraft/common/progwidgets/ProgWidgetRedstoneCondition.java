package pneumaticCraft.common.progwidgets;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.ai.DroneAIBlockCondition;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;

public class ProgWidgetRedstoneCondition extends ProgWidgetCondition{

    @Override
    public String getWidgetString(){
        return "conditionRedstone";
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_REDSTONE;
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget){
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase)widget){

            @Override
            protected boolean evaluate(ChunkPosition pos){
                int redstoneLevel = PneumaticCraftUtils.getRedstoneLevel(drone.getWorld(), pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
                int requiredRedstone = ((ICondition)widget).getRequiredCount();
                return ((ICondition)widget).getOperator() == ICondition.Operator.EQUALS ? requiredRedstone == redstoneLevel : redstoneLevel >= requiredRedstone;
            }

        };
    }
}
