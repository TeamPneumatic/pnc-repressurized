package igwmod.api;

import java.util.List;

import igwmod.gui.IReservedSpace;
import igwmod.gui.IWidget;
import igwmod.gui.LocatedStack;
import igwmod.gui.LocatedString;

/**
 * Implement this class and register it to the WikiRegistry to add support for custom recipes, like Pressure Chambers, Carpenters, Assembly Tables, ...
 * You could also implement this interface for general commands that might not have to do with recipes at all.
 */
public interface IRecipeIntegrator{

    /**
     * Return the name of the command here. for normal crafting recipes this is 'crafting', and for furnace recipes 'furnace'
     * @return
     */
    String getCommandKey();

    /**
     * Called as soon as the parsed command started with the command key of this instance. 
     * @param arguments arguements as strings, seperated by the ',' character. These have been trimmed already. these are the arguments between the '{ }' tags.
     * @param reservedSpaces anything added in here will cause the text to wrap around it.
     * @param locatedStrings For additional info to display with the recipe.
     * @param locatedStacks For recipes you can add stacks to this. These are interfacable, meaning that you can press 'R' and 'U' with NEI, and press 'I' to navigate to the wikipage.
     * @param locatedTextures If you add a recipe handler, add a texture here as underlay for the items.
     * @throws IllegalArgumentException throw this when the wiki writer tries to give illegal arguments. It won't crash the game, instead it will display the error on the generated page.
     */
    void onCommandInvoke(String[] arguments, List<IReservedSpace> reservedSpaces, List<LocatedString> locatedStrings, List<LocatedStack> locatedStacks, List<IWidget> locatedTextures) throws IllegalArgumentException;
}
