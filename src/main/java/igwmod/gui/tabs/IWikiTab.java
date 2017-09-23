package igwmod.gui.tabs;

import java.util.List;

import igwmod.gui.GuiWiki;
import igwmod.gui.IPageLink;
import igwmod.gui.IReservedSpace;
import igwmod.gui.LocatedTexture;
import net.minecraft.item.ItemStack;

public interface IWikiTab{
    /**
     * The returned string will be displayed on the tooltip when you hover over the tab.
     * @return
     */
    String getName();

    /**
     * Will be called by the GUI to render the tab. The render matrix will already be translated dependant on where this tab is.
     * @return When you return an ItemStack, this stack will be drawn rotating. Returning null is valid, nothing will be drawn (you will).
     */
    ItemStack renderTabIcon(GuiWiki gui);

    /**
     * With this you can specify which spaces in the wikipage are prohibited for text to occur. This method is also used to add standard widgets,
     * like images that need to exist on every wikipage of this tab. Just add a {@link LocatedTexture} to this list and it will be rendered.
     * @return
     */
    List<IReservedSpace> getReservedSpaces();

    /**
     * In here you should return the full list of pages. This will also define how people will be able to navigate through pages on this tab.
     * The most simplest way is to use {@link LinkedLocatedString}.
     * @param pageIndexes : This array will be null when every existing page is requested (used for search queries). When specific pages are
     * requested (as a result of a search query), this array will contain the indexes it wants of the list returner earlier. Return a list
     * with only the elements of the indexes given. This way, you can decide where you want to put pagelinks (spacings, only vertical or in pairs
     * of two) however you want. You're in charge on the location of each of the elements.
     * @return
     */
    List<IPageLink> getPages(int[] pageIndexes);

    /**
     * The value returned defines how high the textfield will appear. On a basic page this is on the top of the screen, on the item/block & entity
     * page this is somewhere in the middle.
     * @return
     */
    int getSearchBarAndScrollStartY();

    /**
     * Return the amount of page links that fit on one page (it will allow scrolling if there are more pages than that).
     * @return
     */
    int pagesPerTab();

    /**
     * How many elements (page links) are being scrolled per scroll. This is usually 1, but for the Item/Blocks tab this is 2 (to move two items at once per scroll).
     * @return
     */
    int pagesPerScroll();

    /**
     * 
     * @param gui
     * @param mouseX
     * @param mouseY
     */
    void renderForeground(GuiWiki gui, int mouseX, int mouseY);

    /**
     * 
     * @param gui
     * @param mouseX
     * @param mouseY
     */
    void renderBackground(GuiWiki gui, int mouseX, int mouseY);

    /**
     * 
     * @param gui
     * @param mouseX
     * @param mouseY
     * @param mouseKey
     */
    void onMouseClick(GuiWiki gui, int mouseX, int mouseY, int mouseKey);

    /**
     * Called when navigated to a page of this tab. pageName is the actual path of the .txt file. metadata can be empty, or containing an itemstack or Class<? extends Entity>
     * @param gui
     * @param pageName
     * @param metadata
     */
    void onPageChange(GuiWiki gui, String pageName, Object... metadata);

}
