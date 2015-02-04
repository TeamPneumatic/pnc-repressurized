package pneumaticCraft.common.nei;

import java.util.ArrayList;
import java.util.List;

import pneumaticCraft.client.gui.GuiUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.client.FMLClientHandler;

public abstract class PneumaticCraftPlugins extends TemplateRecipeHandler{
    public class MultipleInputOutputRecipe extends CachedRecipe{
        private final List<PositionedStack> input = new ArrayList<PositionedStack>();
        private final List<PositionedStack> output = new ArrayList<PositionedStack>();

        public void addIngredient(PositionedStack stack){
            input.add(stack);
        }

        public void addIngredient(PositionedStack[] stacks){
            for(PositionedStack stack : stacks) {
                input.add(stack);
            }
        }

        public void addOutput(PositionedStack stack){
            output.add(stack);
        }

        @Override
        public PositionedStack getResult(){
            return null;
        }

        @Override
        public List<PositionedStack> getIngredients(){
            return getCycledIngredients(cycleticks / 20, input);
        }

        @Override
        public List<PositionedStack> getOtherStacks(){
            return output;
        }
    }

    @Override
    public int recipiesPerPage(){
        return 1;
    }

    public void drawAnimatedPressureGauge(int x, int y, float minPressure, float minWorkingPressure, float dangerPressure, float maxPressure, float progress){
        GuiUtils.drawPressureGauge(FMLClientHandler.instance().getClient().fontRenderer, minPressure, maxPressure, dangerPressure, minWorkingPressure, minWorkingPressure * progress, x, y, -90);
    }

}
