package pneumaticCraft.common.thirdparty.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.WidgetAmadronOffer;
import pneumaticCraft.client.gui.widget.WidgetTank;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.recipes.AmadronOffer;
import pneumaticCraft.common.recipes.AmadronOfferManager;
import pneumaticCraft.lib.Textures;
import codechicken.nei.PositionedStack;

public class NEIAmadronTradeManager extends PneumaticCraftPlugins{
    @Override
    public String getRecipeName(){
        return StatCollector.translateToLocal(Itemss.amadronTablet.getUnlocalizedName() + ".name");
    }

    @Override
    public String getGuiTexture(){
        return Textures.WIDGET_AMADRON_OFFER_STRING;
    }

    @Override
    public void drawBackground(int recipe){
        super.drawBackground(recipe);
        GL11.glColor4f(1, 1, 1, 1);
        changeTexture(getGuiTexture());
        drawTexturedModalRect(0, 0, 0, 0, 73, 35);
    }

    @Override
    public void loadTransferRects(){
        addTransferRect(new Rectangle(25, 20, 48, 22));
    }

    private class AmadronNEIRecipe extends MultipleInputOutputRecipe{
        private AmadronNEIRecipe(AmadronOffer offer){
            if(offer.getInput() instanceof ItemStack) addIngredient(new PositionedStack(offer.getInput(), 6, 15));
            if(offer.getOutput() instanceof ItemStack) addOutput(new PositionedStack(offer.getOutput(), 51, 15));
            if(offer.getInput() instanceof FluidStack) addInputLiquid(new WidgetCustomTank(6, 15, (FluidStack)offer.getInput()));
            if(offer.getOutput() instanceof FluidStack) addOutputLiquid(new WidgetCustomTank(51, 15, (FluidStack)offer.getOutput()));
            WidgetAmadronOffer widget = new WidgetAmadronOffer(0, 0, 0, offer).setDrawBackground(false);
            widget.setCanBuy(true);
            addWidget(widget);
        }
    }

    @Override
    protected List<MultipleInputOutputRecipe> getAllRecipes(){
        List<MultipleInputOutputRecipe> recipes = new ArrayList<MultipleInputOutputRecipe>();
        for(AmadronOffer recipe : AmadronOfferManager.getInstance().getAllOffers()) {
            recipes.add(new AmadronNEIRecipe(recipe));
        }
        return recipes;
    }

    @Override
    public int recipiesPerPage(){
        return 2;
    }

    private static class WidgetCustomTank extends WidgetTank{

        public WidgetCustomTank(int x, int y, FluidStack stack){
            super(x, y, 16, 16, stack);
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTick){

        }

    }

}
