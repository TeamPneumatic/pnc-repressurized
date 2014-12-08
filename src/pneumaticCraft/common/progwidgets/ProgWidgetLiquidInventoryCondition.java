package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.common.ai.DroneAIBlockCondition;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.lib.Textures;

public class ProgWidgetLiquidInventoryCondition extends ProgWidgetCondition{

    @Override
    public String getWidgetString(){
        return "conditionLiquidInventory";
    }

    @Override
    public String getGuiTabText(){
        return "bla";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFFFFFFFF;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetLiquidFilter.class, ProgWidgetString.class};
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(EntityDrone drone, IProgWidget widget){
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase)widget){

            @Override
            protected boolean evaluate(ChunkPosition pos){
                TileEntity te = drone.worldObj.getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
                if(te instanceof IFluidHandler) {
                    List<FluidStack> visitedStacks = new ArrayList<FluidStack>();
                    int count = 0;
                    IFluidHandler inv = (IFluidHandler)te;
                    for(int i = 0; i < 6; i++) {
                        if(((ISidedWidget)widget).getSides()[i]) {
                            FluidTankInfo[] info = inv.getTankInfo(ForgeDirection.getOrientation(i));
                            if(info != null) {
                                for(FluidTankInfo inf : info) {
                                    if(inf.fluid != null && !visitedStacks.contains(visitedStacks) && ProgWidgetLiquidFilter.isLiquidValid(inf.fluid.getFluid(), widget, 1)) {
                                        visitedStacks.add(inf.fluid);
                                        count += inf.fluid.amount;
                                    }
                                }
                            }
                        }
                    }
                    return ((ICondition)widget).getOperator() == ICondition.Operator.EQUALS ? count == ((ICondition)widget).getRequiredCount() : count >= ((ICondition)widget).getRequiredCount();
                }
                return false;
            }

        };
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_LIQUID_INVENTORY;
    }

}
