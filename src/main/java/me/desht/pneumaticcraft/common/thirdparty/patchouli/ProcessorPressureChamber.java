package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;

public class ProcessorPressureChamber /*implements IComponentProcessor*/ {
    private IPressureChamberRecipe recipe = null;

//    @Override
//    public void setup(IVariableProvider<String> iVariableProvider) {
//        ItemStack result = PatchouliAPI.instance.deserializeItemStack(iVariableProvider.get("item"));
//
//        // FIXME: only supports recipes with a single output
//        for (IPressureChamberRecipe recipe : BasicPressureChamberRecipe.recipes) {
//            if (ItemStack.areItemStacksEqual(result, recipe.getResult().get(0))) {
//                this.recipe = recipe;
//                break;
//            }
//        }
//    }
//
//    @Override
//    public String process(String s) {
//        if (recipe == null) return null;
//
//        if (s.startsWith("input")) {
//            int index = Integer.parseInt(s.substring(5)) - 1;
//            if (index >= 0 && index < recipe.getInput().size()) {
//                return ItemStackUtil.serializeStack(recipe.getInput().get(index).getSingleStack());
//            }
//        } else if (s.startsWith("output")) {
//            int index = Integer.parseInt(s.substring(6)) - 1;
//            if (index >= 0 && index < recipe.getResult().size()) {
//                return ItemStackUtil.serializeStack(recipe.getResult().get(index));
//            }
//        } else if (s.equals("pressure")) {
//            return String.format("Required pressure: %.1f bar", recipe.getCraftingPressure());
//        }
//
//        return null;
//    }
}
