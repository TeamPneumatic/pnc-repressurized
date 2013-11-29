package pneumaticExample;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import pneumaticCraft.api.universalSensor.EntityPollSensor;

public class DiamondSensor extends EntityPollSensor{
    @Override
    public String getSensorPath(){
        return super.getSensorPath() + "/Diamond";
    }

    @Override
    public boolean needsTextBox(){
        return false;
    }

    @Override
    public List<String> getDescription(){
        List<String> text = new ArrayList<String>();
        text.add(EnumChatFormatting.AQUA + "This example sensor emits a redstone level for every diamond within range");
        return text;
    }

    @Override
    public Class getEntityTracked(){
        return EntityItem.class;
    }

    @Override
    public int getRedstoneValue(List<Entity> entities, String textBoxText){
        int diamonds = 0;
        for(Entity item : entities) {
            ItemStack stack = ((EntityItem)item).getEntityItem();
            if(stack.itemID == Item.diamond.itemID) diamonds += stack.stackSize;
        }
        return Math.min(15, diamonds);
    }

}
