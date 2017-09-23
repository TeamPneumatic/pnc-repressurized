package igwmod.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

public class FurnaceRetrievalEvent extends Event{
    public ItemStack inputStack;
    public ItemStack resultStack;
    public final String key;

    public FurnaceRetrievalEvent(String key){
        this.key = key;
    }
}
