package igwmod.recipeintegration;

import java.util.List;

import igwmod.api.IRecipeIntegrator;
import igwmod.gui.IReservedSpace;
import igwmod.gui.IWidget;
import igwmod.gui.LocatedStack;
import igwmod.gui.LocatedString;

public class IntegratorComment implements IRecipeIntegrator{

    @Override
    public String getCommandKey(){
        return "comment";
    }

    @Override
    public void onCommandInvoke(String[] arguments, List<IReservedSpace> reservedSpaces, List<LocatedString> locatedStrings, List<LocatedStack> locatedStacks, List<IWidget> locatedTextures) throws IllegalArgumentException{
        //Just ignore it, which is what a comment is doing ;).
    }

}
