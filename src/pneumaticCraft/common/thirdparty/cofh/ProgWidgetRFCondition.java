package pneumaticCraft.common.thirdparty.cofh;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.ai.DroneAIBlockCondition;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.progwidgets.ICondition;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetArea;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.progwidgets.ProgWidgetCondition;
import pneumaticCraft.common.progwidgets.ProgWidgetString;
import pneumaticCraft.lib.Textures;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;

public class ProgWidgetRFCondition extends ProgWidgetCondition{

    @Override
    public String getWidgetString(){
        return "conditionRF";
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
                int energy = 0;
                if(te instanceof IEnergyReceiver) {
                    for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                        if(getSides()[d.ordinal()]) {
                            energy = Math.max(((IEnergyReceiver)te).getEnergyStored(d), energy);
                        }
                    }
                }
                if(te instanceof IEnergyProvider) {
                    for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                        if(getSides()[d.ordinal()]) {
                            energy = Math.max(((IEnergyProvider)te).getEnergyStored(d), energy);
                        }
                    }
                }
                return ((ICondition)widget).getOperator() == ICondition.Operator.EQUALS ? energy == ((ICondition)widget).getRequiredCount() : energy >= ((ICondition)widget).getRequiredCount();
            }
        };
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_RF;
    }

}
