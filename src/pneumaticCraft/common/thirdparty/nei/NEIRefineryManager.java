package pneumaticCraft.common.thirdparty.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.GuiRefinery;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.tileentity.TileEntityRefinery;
import pneumaticCraft.lib.Textures;

public class NEIRefineryManager extends PneumaticCraftPlugins{

    @Override
    public String getRecipeName(){
        return StatCollector.translateToLocal(Blockss.refinery.getUnlocalizedName() + ".name");
    }

    @Override
    public String getGuiTexture(){
        return Textures.GUI_REFINERY;
    }

    @Override
    public void drawBackground(int recipe){
        GL11.glColor4f(1, 1, 1, 1);
        changeTexture(getGuiTexture());
        drawTexturedModalRect(0, 0, 6, 3, 166, 79);
    }

    @Override
    public Class<? extends GuiContainer> getGuiClass(){
        return GuiRefinery.class;
    }

    @Override
    public void loadTransferRects(){
        addTransferRect(new Rectangle(25, 20, 48, 22));
    }

    /*   
       private boolean tankClick(GuiRecipe gui, int recipe, boolean usage){
           Point pos = getMousePosition();
           Point offset = gui.getRecipePosition(recipe);
           Point relMouse = new Point(pos.x - gui.guiLeft - offsetx, pos.y - gui.guiTop - offsety);
       }*/

    private class RefineryNEIRecipe extends MultipleInputOutputRecipe{
        public final int refineries;

        private RefineryNEIRecipe(int refineries, int[] outputs){
            this.refineries = refineries;
            addInputLiquid(new FluidStack(Fluids.oil, 10), 2, 10);
            int x = 69;
            int y = 18;
            for(int i = 0; i < outputs.length; i++) {
                if(outputs[i] == 0) continue;
                x += 20;
                y -= 4;
                addOutputLiquid(new FluidStack(TileEntityRefinery.getRefiningFluids()[i], outputs[i]), x, y);
            }
            setUsedTemperature(26, 18, 373);
        }

    }

    @Override
    protected List<MultipleInputOutputRecipe> getAllRecipes(){
        List<MultipleInputOutputRecipe> recipes = new ArrayList<MultipleInputOutputRecipe>();
        for(int i = 0; i < TileEntityRefinery.REFINING_TABLE.length; i++) {
            recipes.add(new RefineryNEIRecipe(2 + i, TileEntityRefinery.REFINING_TABLE[i]));
        }
        return recipes;
    }
}
