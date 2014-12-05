package pneumaticCraft.common.progwidgets;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.ai.DroneAIBlockCondition;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;

public class ProgWidgetRedstoneCondition extends ProgWidgetCondition implements IRedstoneCondition{

    public int requiredRedstone = 0;
    public IRedstoneCondition.Operator operator = IRedstoneCondition.Operator.HIGHER_THAN;

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
                int requiredRedstone = ((IRedstoneCondition)widget).getRequiredRedstone();
                return ((IRedstoneCondition)widget).getOperator() == IRedstoneCondition.Operator.EQUALS ? requiredRedstone == redstoneLevel : redstoneLevel > requiredRedstone;
            }

        };
    }

    @Override
    public int getRequiredRedstone(){
        return requiredRedstone;
    }

    @Override
    public Operator getOperator(){
        return operator;
    }

}
