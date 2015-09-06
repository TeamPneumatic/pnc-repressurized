package pneumaticCraft.common.thirdparty.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import codechicken.nei.PositionedStack;

public class NEIPlasticMixerManager extends PneumaticCraftPlugins{

    @Override
    public String getRecipeName(){
        return StatCollector.translateToLocal(Blockss.plasticMixer.getUnlocalizedName() + ".name");
    }

    @Override
    public String getGuiTexture(){
        return Textures.GUI_PLASTIC_MIXER;
    }

    @Override
    public void drawBackground(int recipe){
        GL11.glColor4f(1, 1, 1, 1);
        changeTexture(getGuiTexture());
        drawTexturedModalRect(0, 0, 6, 3, 166, 79);
    }

    @Override
    public Class<? extends GuiContainer> getGuiClass(){
        return null;
    }

    private class PlasticMixerNEIRecipe extends MultipleInputOutputRecipe{

        private PlasticMixerNEIRecipe(ItemStack input, FluidStack output){

            addOutputLiquid(output, 146, 11);
            addIngredient(new PositionedStack(input, 92, 23));
            setUsedTemperature(76, 22, PneumaticValues.PLASTIC_MIXER_MELTING_TEMP);
        }

        private PlasticMixerNEIRecipe(FluidStack input, ItemStack output){
            addInputLiquid(input, 146, 11);
            addIngredient(new PositionedStack(new ItemStack(Items.dye, 1, 1), 121, 19));
            addIngredient(new PositionedStack(new ItemStack(Items.dye, 1, 2), 121, 37));
            addIngredient(new PositionedStack(new ItemStack(Items.dye, 1, 4), 121, 55));
            addOutput(new PositionedStack(output, 92, 55));
            setUsedTemperature(76, 22, PneumaticValues.PLASTIC_MIXER_MELTING_TEMP);
        }
    }

    @Override
    protected List<MultipleInputOutputRecipe> getAllRecipes(){
        List<MultipleInputOutputRecipe> recipes = new ArrayList<MultipleInputOutputRecipe>();
        for(int i = 0; i < 16; i++)
            recipes.add(new PlasticMixerNEIRecipe(new ItemStack(Itemss.plastic, 1, i), new FluidStack(Fluids.plastic, 1000)));
        for(int i = 0; i < 16; i++)
            recipes.add(new PlasticMixerNEIRecipe(new FluidStack(Fluids.plastic, 1000), new ItemStack(Itemss.plastic, 1, i)));
        return recipes;
    }
}
