package pneumaticCraft.common.progwidgets;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.ai.DroneAIBlockCondition;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;

public class ProgWidgetRedstoneCondition extends ProgWidgetCondition{

    @Override
    public String getWidgetString(){
        return "conditionRedstone";
    }

    @Override
    public String getGuiTabText(){
        return "bla";
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    public int getGuiTabColor(){
        return 0xFFFFFFFF;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_REDSTONE;
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(EntityDrone drone, IProgWidget widget){
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase)widget){

            @Override
            protected boolean evaluate(ChunkPosition pos){
                int redstoneLevel = PneumaticCraftUtils.getRedstoneLevel(drone.worldObj, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
                int requiredRedstone = ((ICondition)widget).getRequiredCount();
                return ((ICondition)widget).getOperator() == ICondition.Operator.EQUALS ? requiredRedstone == redstoneLevel : redstoneLevel >= requiredRedstone;
            }

        };
    }
}
