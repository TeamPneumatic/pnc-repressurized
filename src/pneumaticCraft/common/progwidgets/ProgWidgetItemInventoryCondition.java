package pneumaticCraft.common.progwidgets;

import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.ai.DroneAIBlockCondition;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;

public class ProgWidgetItemInventoryCondition extends ProgWidgetCondition implements ISidedWidget{
    public boolean[] accessingSides = new boolean[]{true, true, true, true, true, true};

    @Override
    public String getWidgetString(){
        return "conditionItemInventory";
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
        return new Class[]{ProgWidgetArea.class, ProgWidgetItemFilter.class, ProgWidgetString.class};
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(EntityDrone drone, IProgWidget widget){
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase)widget){

            @Override
            protected boolean evaluate(ChunkPosition pos){
                TileEntity te = drone.worldObj.getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
                if(te instanceof IInventory) {
                    IInventory inv = (IInventory)te;
                    Set<Integer> accessibleSlots = PneumaticCraftUtils.getAccessibleSlotsForInventoryAndSides(inv, ((ISidedWidget)widget).getSides());
                    for(Integer i : accessibleSlots) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if(stack != null && widget.isItemValidForFilters(stack)) {

                            return true;
                        }
                    }
                }
                return false;
            }

        };
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_ITEM_INVENTORY;
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
