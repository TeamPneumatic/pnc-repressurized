package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProcessorAssemblySystem implements IComponentProcessor {
    private AssemblyRecipe recipe = null;

    @Override
    public void setup(IVariableProvider iVariableProvider) {
        String recipeId = iVariableProvider.get("recipe").asString();
        this.recipe = PneumaticCraftRecipeType.ASSEMBLY_DRILL.getRecipe(Minecraft.getInstance().world, new ResourceLocation(recipeId));
        if (recipe == null) {
            this.recipe = PneumaticCraftRecipeType.ASSEMBLY_LASER.getRecipe(Minecraft.getInstance().world, new ResourceLocation(recipeId));
            if (recipe == null) {
                this.recipe = PneumaticCraftRecipeType.ASSEMBLY_DRILL_LASER.getRecipe(Minecraft.getInstance().world, new ResourceLocation(recipeId));
            }
        }
    }

    @Override
    public IVariable process(String key) {
        if (recipe == null) return null;

        ItemStack programStack = new ItemStack(ItemAssemblyProgram.fromProgramType(recipe.getProgramType()));
        switch (key) {
            case "input":
                return Patchouli.Util.getStacks(recipe.getInput());
            case "output":
                return IVariable.from(recipe.getOutput());
            case "program":
                return IVariable.from(programStack);
            case "name":
                return IVariable.wrap(recipe.getOutput().getDisplayName().getString());
            case "desc":
                return IVariable.wrap(xlate("pneumaticcraft.patchouli.processor.assembly.desc",
                        recipe.getOutput().getDisplayName(),
                        programStack.getDisplayName()
                ).getString());
        }

        return null;
    }

}
