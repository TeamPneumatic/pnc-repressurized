package pneumaticCraft.common.thirdparty.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.recipe.AssemblyRecipe;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemAssemblyProgram;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.recipes.programs.AssemblyProgram;
import pneumaticCraft.lib.Textures;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import cpw.mods.fml.client.FMLClientHandler;

public class NEIAssemblyControllerRecipeManager extends PneumaticCraftPlugins{
    ResourceLocation texture;

    @Override
    public String getRecipeName(){
        return "Assembly Controller";
    }

    @Override
    public String getGuiTexture(){
        return Textures.GUI_NEI_ASSEMBLY_CONTROLLER;
    }

    @Override
    public void drawBackground(int recipe){
        if(texture == null) texture = new ResourceLocation(getGuiTexture());
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(texture);
        GL11.glColor4f(1, 1, 1, 1);
        Gui.func_146110_a(0, 0, 5, 11, 166, 130, 256, 256);
    }

    private String getRecipesID(){
        return "AssemblyController";
    }

    @Override
    public void loadTransferRects(){
        transferRects.add(new RecipeTransferRect(new Rectangle(68, 75, 24, 17), getRecipesID()));
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results){
        if(outputId.equals(getRecipesID())) {
            for(int i = 0; i < ItemAssemblyProgram.PROGRAMS_AMOUNT; i++) {
                AssemblyProgram program = ItemAssemblyProgram.getProgramFromItem(i);
                for(int j = 0; j < program.getRecipeList().size(); j++)
                    arecipes.add(getShape(i, j));
            }
        } else super.loadCraftingRecipes(outputId, results);
    }

    protected MultipleInputOutputRecipe getShape(int programMetadata, int recipeIndex){
        AssemblyProgram program = ItemAssemblyProgram.getProgramFromItem(programMetadata);
        AssemblyRecipe recipe = program.getRecipeList().get(recipeIndex);
        MultipleInputOutputRecipe shape = new MultipleInputOutputRecipe();
        ItemStack[] inputStacks = new ItemStack[]{recipe.getInput()};//for now not useful to put it in an array, but supports when adding multiple input/output.
        for(int i = 0; i < inputStacks.length; i++) {
            PositionedStack stack = new PositionedStack(inputStacks[i], 29 + i % 2 * 18, 66 + i / 2 * 18);
            shape.addIngredient(stack);
        }

        ItemStack[] outputStacks = new ItemStack[]{recipe.getOutput()};
        for(int i = 0; i < outputStacks.length; i++) {
            PositionedStack stack = new PositionedStack(outputStacks[i], 96 + i % 2 * 18, 66 + i / 2 * 18);
            shape.addOutput(stack);
        }
        shape.addIngredient(new PositionedStack(new ItemStack(Itemss.assemblyProgram, 1, programMetadata), 133, 22));
        ItemStack[] requiredMachines = getMachinesFromEnum(program.getRequiredMachines());
        for(int i = 0; i < requiredMachines.length; i++) {
            shape.addIngredient(new PositionedStack(requiredMachines[i], 5 + i * 18, 25));
        }

        return shape;
    }

    protected ItemStack[] getMachinesFromEnum(AssemblyProgram.EnumMachine[] requiredMachines){
        ItemStack[] machineStacks = new ItemStack[requiredMachines.length];
        for(int i = 0; i < requiredMachines.length; i++) {
            switch(requiredMachines[i]){
                case PLATFORM:
                    machineStacks[i] = new ItemStack(Blockss.assemblyPlatform);
                    break;
                case DRILL:
                    machineStacks[i] = new ItemStack(Blockss.assemblyDrill);
                    break;
                case LASER:
                    machineStacks[i] = new ItemStack(Blockss.assemblyLaser);
                    break;
                case IO_UNIT_IMPORT:
                    machineStacks[i] = new ItemStack(Blockss.assemblyIOUnit, 1, 0);
                    break;
                case IO_UNIT_EXPORT:
                    machineStacks[i] = new ItemStack(Blockss.assemblyIOUnit, 1, 1);
                    break;
            }
        }
        return machineStacks;
    }

    @Override
    public void loadCraftingRecipes(ItemStack result){
        for(int i = 0; i < ItemAssemblyProgram.PROGRAMS_AMOUNT; i++) {
            AssemblyProgram program = ItemAssemblyProgram.getProgramFromItem(i);
            for(int j = 0; j < program.getRecipeList().size(); j++) {
                if(NEIClientUtils.areStacksSameTypeCrafting(program.getRecipeList().get(j).getOutput(), result)) {
                    arecipes.add(getShape(i, j));
                    break;
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient){
        for(int i = 0; i < ItemAssemblyProgram.PROGRAMS_AMOUNT; i++) {
            AssemblyProgram program = ItemAssemblyProgram.getProgramFromItem(i);
            boolean[] addedRecipe = new boolean[program.getRecipeList().size()];
            for(int j = 0; j < program.getRecipeList().size(); j++) {
                if(NEIClientUtils.areStacksSameTypeCrafting(program.getRecipeList().get(j).getInput(), ingredient)) {
                    arecipes.add(getShape(i, j));
                    addedRecipe[j] = true;
                }
            }
            if(ingredient.getItem() == Itemss.assemblyProgram && ingredient.getItemDamage() == i) {
                for(int j = 0; j < program.getRecipeList().size(); j++)
                    if(!addedRecipe[j]) arecipes.add(getShape(i, j));
            } else {
                for(ItemStack machine : getMachinesFromEnum(program.getRequiredMachines())) {
                    if(NEIClientUtils.areStacksSameTypeCrafting(machine, ingredient)) {
                        for(int j = 0; j < program.getRecipeList().size(); j++)
                            if(!addedRecipe[j]) arecipes.add(getShape(i, j));
                        break;
                    }
                }
            }
        }
    }

    /*  @Override
      public Class<? extends GuiContainer> getGuiClass(){
          return GuiAssemblyController.class;
      }*/

    @Override
    public boolean hasOverlay(GuiContainer gui, Container container, int recipe){
        return false;
    }

    @Override
    public void drawExtras(int recipe){
        drawProgressBar(68, 75, 173, 0, 24, 17, 48, 0);
        FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
        fontRenderer.drawString("Required Machines", 5, 15, 4210752);
        fontRenderer.drawString("Prog.", 129, 9, 4210752);
    }

    @Override
    protected List<MultipleInputOutputRecipe> getAllRecipes(){
        return new ArrayList<MultipleInputOutputRecipe>();
    }

}
