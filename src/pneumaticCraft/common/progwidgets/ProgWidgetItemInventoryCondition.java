package pneumaticCraft.common.progwidgets;

import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.ai.DroneAIBlockCondition;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.util.IOHelper;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;

public class ProgWidgetItemInventoryCondition extends ProgWidgetCondition{

    @Override
    public String getWidgetString(){
        return "conditionItemInventory";
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetItemFilter.class, ProgWidgetString.class};
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(IDroneBase drone, IProgWidget widget){
        return new DroneAIBlockCondition(drone, (ProgWidgetAreaItemBase)widget){

            @Override
            protected boolean evaluate(ChunkPosition pos){
                IInventory inv = IOHelper.getInventoryForTE(drone.getWorld().getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ));
                if(inv != null) {
                    int count = 0;
                    Set<Integer> accessibleSlots = PneumaticCraftUtils.getAccessibleSlotsForInventoryAndSides(inv, ((ISidedWidget)widget).getSides());
                    for(Integer i : accessibleSlots) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if(stack != null && widget.isItemValidForFilters(stack)) {
                            count += stack.stackSize;
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
        return Textures.PROG_WIDGET_CONDITION_ITEM_INVENTORY;
    }

}
