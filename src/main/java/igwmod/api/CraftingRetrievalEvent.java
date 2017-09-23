package igwmod.api;

import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This event is fired on MinecraftForge.EVENT_BUS when a wikipage contains a [crafting{key=recipeName}] block, in which the 'recipeName' is a string that is passed to this event.
 * This event is a way to save you the time of writing a manual recipe, like 
 * [crafting{www,cic,crc,w=block/wood,c=block/stonebrick,i=item/ingotIron,r=block/redstoneDust}block/pistonBase] for a piston.
 * With this event, you can write [crafting{piston}]. IGW detects this, and fires this event with the 'piston' key. You see the key, and set
 * the 'recipe' field to the ShapelessRecipes of the piston recipe. Instead of searching for the instance of this recipe, you can keep a 
 * reference to it when you register the recipe (GameRegistry#addShapedRecipe(ItemStack, Object...) returns an IRecipe).
 */
public class CraftingRetrievalEvent extends Event{
    public IRecipe recipe; //Only pass ShapedRecipes, ShapedOreRecipe or ShapelessRecipes (or I'll throw an exception, mhoehaha!)
    public final String key;

    public CraftingRetrievalEvent(String key){
        this.key = key;
    }
}
