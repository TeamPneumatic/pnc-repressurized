package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.recipes.programs.AssemblyProgram;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.FMLClientHandler;

public class JEIAssemblyControllerCategory extends PneumaticCraftCategory<JEIAssemblyControllerCategory.AssemblyRecipeWrapper> {

    public JEIAssemblyControllerCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
    }

    @Override
    public String getUid() {
        return ModCategoryUid.ASSEMBLY_CONTROLLER;
    }

    @Override
    public String getTitle() {
        return I18n.format(Blockss.ASSEMBLY_CONTROLLER.getUnlocalizedName());
    }

    @Override
    public ResourceDrawable getGuiTexture() {
        return new ResourceDrawable(Textures.GUI_NEI_ASSEMBLY_CONTROLLER, 0, 0, 5, 11, 166, 130);
    }

    /* @Override
     public void loadCraftingRecipes(String outputId, Object... results){
         if(outputId.equals(getRecipesID())) {
             for(int i = 0; i < ItemAssemblyProgram.PROGRAMS_AMOUNT; i++) {
                 AssemblyProgram program = ItemAssemblyProgram.getProgramFromItem(i);
                 for(int j = 0; j < program.getRecipeList().size(); j++)
                     arecipes.add(getShape(i, j));
             }
         } else super.loadCraftingRecipes(outputId, results);
     }*/

    static class AssemblyRecipeWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {
        AssemblyRecipeWrapper(AssemblyRecipe recipe) {
            int meta = AssemblyRecipe.drillRecipes.contains(recipe) ? 0 : AssemblyRecipe.laserRecipes.contains(recipe) ? 1 : 2;
            AssemblyProgram program = ItemAssemblyProgram.getProgramFromItem(meta);
            ItemStack[] inputStacks = new ItemStack[]{recipe.getInput()};//for now not useful to put it in an array, but supports when adding multiple input/output.
            for (int i = 0; i < inputStacks.length; i++) {
                PositionedStack stack = new PositionedStack(inputStacks[i], 29 + i % 2 * 18, 66 + i / 2 * 18);
                this.addIngredient(stack);
            }

            ItemStack[] outputStacks = new ItemStack[]{recipe.getOutput()};
            for (int i = 0; i < outputStacks.length; i++) {
                PositionedStack stack = new PositionedStack(outputStacks[i], 96 + i % 2 * 18, 66 + i / 2 * 18);
                this.addOutput(stack);
            }
            this.addIngredient(new PositionedStack(new ItemStack(Itemss.ASSEMBLY_PROGRAM, 1, meta), 133, 22));
            ItemStack[] requiredMachines = getMachinesFromEnum(program.getRequiredMachines());
            for (int i = 0; i < requiredMachines.length; i++) {
                this.addIngredient(new PositionedStack(requiredMachines[i], 5 + i * 18, 25));
            }
        }

        protected ItemStack[] getMachinesFromEnum(AssemblyProgram.EnumMachine[] requiredMachines) {
            ItemStack[] machineStacks = new ItemStack[requiredMachines.length];
            for (int i = 0; i < requiredMachines.length; i++) {
                switch (requiredMachines[i]) {
                    case PLATFORM:
                        machineStacks[i] = new ItemStack(Blockss.ASSEMBLY_PLATFORM);
                        break;
                    case DRILL:
                        machineStacks[i] = new ItemStack(Blockss.ASSEMBLY_DRILL);
                        break;
                    case LASER:
                        machineStacks[i] = new ItemStack(Blockss.ASSEMBLY_LASER);
                        break;
                    case IO_UNIT_IMPORT:
                        machineStacks[i] = new ItemStack(Blockss.ASSEMBLY_IO_UNIT, 1, 0);
                        break;
                    case IO_UNIT_EXPORT:
                        machineStacks[i] = new ItemStack(Blockss.ASSEMBLY_IO_UNIT, 1, 1);
                        break;
                }
            }
            return machineStacks;
        }
    }

    protected MultipleInputOutputRecipeWrapper getShape(AssemblyRecipe recipe) {
        int meta = AssemblyRecipe.drillRecipes.contains(recipe) ? 0 : AssemblyRecipe.laserRecipes.contains(recipe) ? 1 : 2;
        AssemblyProgram program = ItemAssemblyProgram.getProgramFromItem(meta);
        MultipleInputOutputRecipeWrapper shape = new MultipleInputOutputRecipeWrapper();
        ItemStack[] inputStacks = new ItemStack[]{recipe.getInput()};//for now not useful to put it in an array, but supports when adding multiple input/output.
        for (int i = 0; i < inputStacks.length; i++) {
            PositionedStack stack = new PositionedStack(inputStacks[i], 29 + i % 2 * 18, 66 + i / 2 * 18);
            shape.addIngredient(stack);
        }

        ItemStack[] outputStacks = new ItemStack[]{recipe.getOutput()};
        for (int i = 0; i < outputStacks.length; i++) {
            PositionedStack stack = new PositionedStack(outputStacks[i], 96 + i % 2 * 18, 66 + i / 2 * 18);
            shape.addOutput(stack);
        }
        shape.addIngredient(new PositionedStack(new ItemStack(Itemss.ASSEMBLY_PROGRAM, 1, meta), 133, 22));
        ItemStack[] requiredMachines = getMachinesFromEnum(program.getRequiredMachines());
        for (int i = 0; i < requiredMachines.length; i++) {
            shape.addIngredient(new PositionedStack(requiredMachines[i], 5 + i * 18, 25));
        }

        return shape;
    }

    protected ItemStack[] getMachinesFromEnum(AssemblyProgram.EnumMachine[] requiredMachines) {
        ItemStack[] machineStacks = new ItemStack[requiredMachines.length];
        for (int i = 0; i < requiredMachines.length; i++) {
            switch (requiredMachines[i]) {
                case PLATFORM:
                    machineStacks[i] = new ItemStack(Blockss.ASSEMBLY_PLATFORM);
                    break;
                case DRILL:
                    machineStacks[i] = new ItemStack(Blockss.ASSEMBLY_DRILL);
                    break;
                case LASER:
                    machineStacks[i] = new ItemStack(Blockss.ASSEMBLY_LASER);
                    break;
                case IO_UNIT_IMPORT:
                    machineStacks[i] = new ItemStack(Blockss.ASSEMBLY_IO_UNIT, 1, 0);
                    break;
                case IO_UNIT_EXPORT:
                    machineStacks[i] = new ItemStack(Blockss.ASSEMBLY_IO_UNIT, 1, 1);
                    break;
            }
        }
        return machineStacks;
    }

    /*  @Override
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
      }*/

    @Override
    public void drawExtras(Minecraft minecraft) {
        drawProgressBar(68, 75, 173, 0, 24, 17, StartDirection.LEFT);
        FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
        fontRenderer.drawString("Required Machines", 5, 15, 4210752);
        fontRenderer.drawString("Prog.", 129, 9, 4210752);
    }

//    @Override
//    public Class<AssemblyRecipe> getRecipeClass() {
//        return AssemblyRecipe.class;
//    }
//
//    @Override
//    public IRecipeWrapper getRecipeWrapper(AssemblyRecipe recipe) {
//        return getShape(recipe);
//    }
//
//    @Override
//    public boolean isRecipeValid(AssemblyRecipe recipe) {
//        return true;
//    }

}
