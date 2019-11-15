package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;

public class ProcessorAssemblySystem /*implements IComponentProcessor*/ {
    private AssemblyRecipe recipe = null;

//    @Override
//    public void setup(IVariableProvider<String> iVariableProvider) {
//        ItemStack result = PatchouliAPI.instance.deserializeItemStack(iVariableProvider.get("item"));
//        recipe = AssemblyRecipe.findRecipeForOutput(result);
//    }
//
//    @Override
//    public String process(String key) {
//        if (recipe == null) return null;
//
//        switch (key) {
//            case "input":
//                return ItemStackUtil.serializeStack(recipe.getInput());
//            case "output":
//                return ItemStackUtil.serializeStack(recipe.getOutput());
//            case "program":
//                return ItemStackUtil.serializeStack(recipe.getProgram());
//            case "name":
//                return recipe.getOutput().getDisplayName();
//            case "desc":
//                return I18n.format("patchouli.processor.assembly.desc", recipe.getOutput().getDisplayName(), recipe.getProgram().getDisplayName());
//        }
//
//        return null;
//    }
}
