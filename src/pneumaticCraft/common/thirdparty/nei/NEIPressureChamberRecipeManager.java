package pneumaticCraft.common.thirdparty.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.recipe.PressureChamberRecipe;
import pneumaticCraft.client.gui.GuiPressureChamber;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import codechicken.nei.PositionedStack;
import cpw.mods.fml.client.FMLClientHandler;

public class NEIPressureChamberRecipeManager extends PneumaticCraftPlugins{
    ResourceLocation texture;

    public class ChamberRecipe extends MultipleInputOutputRecipe{
        public float recipePressure;
    }

    @Override
    public String getRecipeName(){
        return "Pressure Chamber";
    }

    @Override
    public String getGuiTexture(){
        return Textures.GUI_NEI_PRESSURE_CHAMBER_LOCATION;
    }

    @Override
    public void drawBackground(int recipe){
        GL11.glColor4f(1, 1, 1, 1);
        if(texture == null) texture = new ResourceLocation(getGuiTexture());
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(texture);
        GL11.glColor4f(1, 1, 1, 1);
        Gui.func_146110_a(0, 0, 5, 11, 166, 130, 256, 256);
    }

    @Override
    public void drawExtras(int recipe){
        float recipePressure = ((ChamberRecipe)arecipes.get(recipe)).recipePressure;
        drawAnimatedPressureGauge(120, 27, -1, recipePressure, PneumaticValues.DANGER_PRESSURE_PRESSURE_CHAMBER, PneumaticValues.MAX_PRESSURE_PRESSURE_CHAMBER, cycleticks % 48 / 48F);
    }

    @Override
    public void loadTransferRects(){
        addTransferRect(new Rectangle(100, 7, 40, 40));
    }

    protected ChamberRecipe getShape(PressureChamberRecipe recipe){
        ChamberRecipe shape = new ChamberRecipe();
        for(int i = 0; i < recipe.input.length; i++) {
            PositionedStack stack;
            int posX = 19 + i % 3 * 17;
            int posY = 93 - i / 3 * 17;

            int[] oreIDs = OreDictionary.getOreIDs(recipe.input[i]);
            if(oreIDs.length > 0) {
                List<ItemStack> oreInputs = new ArrayList<ItemStack>();

                for(int oreID : oreIDs) {
                    for(ItemStack oreInput : OreDictionary.getOres(OreDictionary.getOreName(oreID))) {
                        oreInput = oreInput.copy();
                        oreInput.stackSize = recipe.input[i].stackSize;
                        oreInputs.add(oreInput);
                    }
                }
                stack = new PositionedStack(oreInputs, posX, posY, true);
            } else {
                stack = new PositionedStack(recipe.input[i], posX, posY);
            }
            shape.addIngredient(stack);
        }

        for(int i = 0; i < recipe.output.length; i++) {
            PositionedStack stack = new PositionedStack(recipe.output[i], 101 + i % 3 * 18, 59 + i / 3 * 18);
            shape.addOutput(stack);
        }
        shape.recipePressure = recipe.pressure;
        return shape;
    }

    @Override
    public Class<? extends GuiContainer> getGuiClass(){
        return GuiPressureChamber.class;
    }

    @Override
    public boolean hasOverlay(GuiContainer gui, Container container, int recipe){
        return false;
    }

    @Override
    protected List<MultipleInputOutputRecipe> getAllRecipes(){
        List<MultipleInputOutputRecipe> recipes = new ArrayList<MultipleInputOutputRecipe>();
        for(PressureChamberRecipe recipe : PressureChamberRecipe.chamberRecipes) {
            recipes.add(getShape(recipe));
        }
        return recipes;
    }

}
