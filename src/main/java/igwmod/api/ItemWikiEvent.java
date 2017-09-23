package igwmod.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Event fired on MinecraftForge.EVENT_BUS when the player opens the IGW GUI by looking at an item in another GUI and pressing 'i' or when the player
 * navigates between wikipages. This event is also fired when looking
 * at item entities. For info about the pageOpened field, look at {@link BlockWikiEvent}
 * when you don't alter the pageOpened field, it will default to assets/igwmod/wiki/item/<itemStack.getUnlocalizedName()>
 */
public class ItemWikiEvent extends Event{
    public final ItemStack itemStack; //you can change the drawn stack in the top left corner by altering this stack.
    public String pageOpened;

    public ItemWikiEvent(ItemStack itemStack, String pageOpened){
        this.itemStack = itemStack;
        this.pageOpened = pageOpened;
    }
}
