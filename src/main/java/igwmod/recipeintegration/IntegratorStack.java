package igwmod.recipeintegration;

import java.util.List;

import igwmod.WikiUtils;
import igwmod.api.IRecipeIntegrator;
import igwmod.gui.GuiWiki;
import igwmod.gui.IReservedSpace;
import igwmod.gui.IWidget;
import igwmod.gui.LocatedStack;
import igwmod.gui.LocatedString;
import net.minecraft.item.ItemStack;

public class IntegratorStack implements IRecipeIntegrator{

    @Override
    public String getCommandKey(){
        return "stack";
    }

    @Override
    public void onCommandInvoke(String[] arguments, List<IReservedSpace> reservedSpaces, List<LocatedString> locatedStrings, List<LocatedStack> locatedStacks, List<IWidget> locatedTextures) throws IllegalArgumentException{
        if(arguments.length != 3) throw new IllegalArgumentException("stack command takes 3 arguments! Currently it has " + arguments.length);
        int x;
        try {
            x = Integer.parseInt(arguments[0]);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("The first parameter (the x coordinate) contains an invalid number. Check for invalid characters!");
        }
        int y;
        try {
            y = Integer.parseInt(arguments[1]);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("The second parameter (the y coordinate) contains an invalid number. Check for invalid characters!");
        }

        ItemStack stack = WikiUtils.getStackFromName(arguments[2]);
        if(stack == null) throw new IllegalArgumentException("Item not found for the name " + arguments[2]);
        locatedStacks.add(new LocatedStack(stack, (int)(x * GuiWiki.TEXT_SCALE), (int)(y * GuiWiki.TEXT_SCALE)));
    }

}
