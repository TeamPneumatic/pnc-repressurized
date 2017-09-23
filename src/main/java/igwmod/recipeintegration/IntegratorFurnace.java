package igwmod.recipeintegration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import igwmod.TextureSupplier;
import igwmod.WikiUtils;
import igwmod.api.FurnaceRetrievalEvent;
import igwmod.api.IRecipeIntegrator;
import igwmod.gui.GuiWiki;
import igwmod.gui.IReservedSpace;
import igwmod.gui.IWidget;
import igwmod.gui.LocatedStack;
import igwmod.gui.LocatedString;
import igwmod.gui.LocatedTexture;
import igwmod.lib.Paths;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public class IntegratorFurnace implements IRecipeIntegrator{

    public static Map<String, ItemStack> autoMappedFurnaceRecipes = new HashMap<String, ItemStack>();

    @Override
    public String getCommandKey(){
        return "furnace";
    }

    @Override
    public void onCommandInvoke(String[] arguments, List<IReservedSpace> reservedSpaces, List<LocatedString> locatedStrings, List<LocatedStack> locatedStacks, List<IWidget> locatedTextures) throws IllegalArgumentException{
        if(arguments.length != 3 && arguments.length != 4) throw new IllegalArgumentException("Code needs to contain 3 or 4 arguments: x, y, inputstack, outputstack. It now contains " + arguments.length + ".");
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
        locatedTextures.add(new LocatedTexture(TextureSupplier.getTexture(Paths.MOD_ID_WITH_COLON + "textures/GuiFurnace.png"), x, y, (int)(82 / GuiWiki.TEXT_SCALE), (int)(54 / GuiWiki.TEXT_SCALE)));
        x = (int)(x * GuiWiki.TEXT_SCALE);
        y = (int)(y * GuiWiki.TEXT_SCALE);
        ItemStack inputStack = null;
        ItemStack resultStack = null;
        if(arguments[2].startsWith("key=")) {
            String resultStackCode = arguments[2].substring(4);
            inputStack = autoMappedFurnaceRecipes.get(resultStackCode);
            if(inputStack != null) {
                resultStack = WikiUtils.getStackFromName(resultStackCode);
            } else {
                FurnaceRetrievalEvent recipeEvent = new FurnaceRetrievalEvent(resultStackCode);
                MinecraftForge.EVENT_BUS.post(recipeEvent);
                inputStack = recipeEvent.inputStack;
                resultStack = recipeEvent.resultStack;
            }
        } else {
            inputStack = WikiUtils.getStackFromName(arguments[2]);
            resultStack = WikiUtils.getStackFromName(arguments[3]);
        }
        if(inputStack != null) {
            locatedStacks.add(new LocatedStack(inputStack, x + IntegratorCraftingRecipe.STACKS_X_OFFSET, y + IntegratorCraftingRecipe.STACKS_Y_OFFSET));
        }
        if(resultStack != null) {
            locatedStacks.add(new LocatedStack(resultStack, x + IntegratorCraftingRecipe.STACKS_X_OFFSET + 60, y + IntegratorCraftingRecipe.STACKS_Y_OFFSET + 18));
        }
    }

}
