package igwmod.api;

import java.util.List;

import igwmod.gui.IReservedSpace;
import igwmod.gui.IWidget;
import igwmod.gui.LocatedStack;
import igwmod.gui.LocatedString;
import net.minecraft.client.gui.FontRenderer;

/**
 * When IGWMod's internal markup language doesn't suffice, or does not suit you, you can plugin your own text interpreter with this.
 * If you've come up with a nice interpeter, and want to help others with it as well, feel free to create a Pull Request, to put it in natively into IGW-Mod :).
 */
public interface ITextInterpreter{

    /**
     * Is called when a page is loaded. Giving the raw page contents, and lists you can fill to lay out the page, all the tools are needed to interpret the page how you want to.
     * All lists (reservedSpaces, locatedStrings, locatedStacks, locatedTextures) can have elements in upon invoking, these need to be respected with text wrapping.
     * @param fontRenderer
     * @param rawText
     * @param reservedSpaces Rectangles on the screen where text is prohibited to appear (it needs to be wrapped around).
     * @param locatedStrings The actual text, plus location. Can be special, such as links/colors.
     * @param locatedStacks Item stacks that need to appear on the screen. They have item tooltips and can be interacted with with NEI. They, like reservedSpaces, need to have text wrapped around them, instead of text going through them.
     * @param locatedTextures images, basically. They, like reservedSpaces, need to have text wrapped around them, instead of text going through them.
     * @return true if you interpreted the text, false if you haven't. When returning true, no other text interpreters will run. If returned false, they will. It's recommended to do like a check at the beginning of the file that contains a marker that allows the interpeter to run, only for your pages.
     */
    boolean interpret(FontRenderer fontRenderer, List<String> rawText, List<IReservedSpace> reservedSpaces, List<LocatedString> locatedStrings, List<LocatedStack> locatedStacks, List<IWidget> locatedTextures);
}
