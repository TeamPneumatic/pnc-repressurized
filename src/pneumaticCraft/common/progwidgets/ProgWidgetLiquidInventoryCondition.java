package pneumaticCraft.common.progwidgets;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.common.ai.DroneAIBlockCondition;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.lib.Textures;

public class ProgWidgetLiquidInventoryCondition extends ProgWidgetCondition implements ISidedWidget{
    public boolean[] accessingSides = new boolean[]{true, true, true, true, true, true};

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
                    IFluidHandler inv = (IFluidHandler)te;
                    for(int i = 0; i < 6; i++) {
                        if(((ISidedWidget)widget).getSides()[i]) {
                            FluidTankInfo[] info = inv.getTankInfo(ForgeDirection.getOrientation(i));
                            if(info != null) {
                                for(FluidTankInfo inf : info) {
                                    if(inf.fluid != null && ProgWidgetLiquidFilter.isLiquidValid(inf.fluid.getFluid(), widget, 1)) return true;
                                }
                            }
                        }
                    }
                }
                return false;
            }

        };
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_LIQUID_INVENTORY;
    }

    @Override
    public void setSides(boolean[] sides){
        accessingSides = sides;
    }

    @Override
    public boolean[] getSides(){
        return accessingSides;
    }

}
