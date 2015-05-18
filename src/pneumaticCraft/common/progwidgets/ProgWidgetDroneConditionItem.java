package pneumaticCraft.common.progwidgets;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.lib.Textures;

public class ProgWidgetDroneConditionItem extends ProgWidgetDroneEvaluation implements IItemFiltering{

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetItemFilter.class, ProgWidgetString.class};
    }

    @Override
    public String getWidgetString(){
        return "droneConditionItem";
    }

    @Override
    protected int getCount(IDroneBase drone, IProgWidget widget){
        int count = 0;
        for(int i = 0; i < drone.getInventory().getSizeInventory(); i++) {
            ItemStack droneStack = drone.getInventory().getStackInSlot(i);

            if(droneStack != null && ((IItemFiltering)widget).isItemValidForFilters(droneStack)) {
                count += droneStack.stackSize;
            }
        }
        return count;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_DRONE_ITEM_INVENTORY;
    }

    @Override
    public boolean isItemValidForFilters(ItemStack item){
        return ProgWidgetItemFilter.isItemValidForFilters(item, ProgWidget.getConnectedWidgetList(this, 0), ProgWidget.getConnectedWidgetList(this, getParameters().length), -1);
    }

}
