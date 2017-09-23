package igwmod.api;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired on MinecraftForge.EVENT_BUS when the page changes. This can be used to intercept any page that is being tried to be looked up from a .txt file, and supply it a custom (language) string instead.
 */
public class PageChangeEvent extends Event{

    /**
     * The default (partial) resource location of the page opened. examples: block/pistonBase, block/workbench Change to make IGW-Mod look for a different resource location.
     */
    public String currentFile;

    /**
     * Custom page text. When left null, the currentFile resource location will be used to look up the text from a .txt file. If not null, this
     * text will be used instead.
     */
    public List<String> pageText;

    /**
     * Will not be null when the page is opened via in-world block looking up, or when the player hovers over an item stack in a GUI and opens the wiki.
     */
    public ItemStack associatedStack;

    /**
     * Will not be null when it's an entity that has been looked up.
     */
    public Entity associatedEntity;

    public PageChangeEvent(String currentFile, ItemStack associatedStack, Entity associatedEntity){
        this.currentFile = currentFile;
        this.associatedStack = associatedStack;
        this.associatedEntity = associatedEntity;
    }
}
