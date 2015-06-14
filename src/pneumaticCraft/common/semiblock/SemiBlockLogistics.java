package pneumaticCraft.common.semiblock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public abstract class SemiBlockLogistics extends SemiBlockBasic{
    protected final Map<ItemStack, Integer> incomingStacks = new HashMap<ItemStack, Integer>();

    @Override
    public boolean canPlace(){
        return getTileEntity() instanceof IInventory;
    }

    public abstract int getColor();

    public abstract int getPriority();

    public boolean shouldProvideTo(int level){
        return true;
    }

    @Override
    public void update(){
        super.update();
        Iterator<Map.Entry<ItemStack, Integer>> iterator = incomingStacks.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<ItemStack, Integer> entry = iterator.next();
            int counter = entry.getValue();
            if(counter > 10) {
                iterator.remove();
            } else {
                entry.setValue(counter + 1);
            }
        }
    }

    public void informIncomingStack(ItemStack stack){
        incomingStacks.put(stack, 0);
    }

    public void clearIncomingStack(ItemStack stack){
        incomingStacks.remove(stack);
    }

}
