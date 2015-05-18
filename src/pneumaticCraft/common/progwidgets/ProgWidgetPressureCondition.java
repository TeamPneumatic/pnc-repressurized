package pneumaticCraft.common.progwidgets;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.ai.DroneAIBlockCondition;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.lib.Textures;

public class ProgWidgetPressureCondition extends ProgWidgetCondition{

    @Override
    public String getWidgetString(){
        return "conditionPressure";
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget){
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase)widget){

            @Override
            protected boolean evaluate(ChunkPosition pos){
                TileEntity te = drone.getWorld().getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
                if(te instanceof IPneumaticMachine) {
                    IAirHandler airHandler = ((IPneumaticMachine)te).getAirHandler();
                    float pressure = Float.MIN_VALUE;
                    for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                        if(getSides()[d.ordinal()]) {
                            pressure = Math.max(airHandler.getPressure(d), pressure);
                        }
                    }
                    return ((ICondition)widget).getOperator() == ICondition.Operator.EQUALS ? pressure == ((ICondition)widget).getRequiredCount() : pressure >= ((ICondition)widget).getRequiredCount();
                }
                return false;
            }

        };
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_PRESSURE;
    }

}
