package pneumaticCraft.common.recipes;

import java.util.Random;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import pneumaticCraft.api.recipe.IPressureChamberRecipe;

public class PressureChamberVacuumEnchantHandler implements IPressureChamberRecipe{

    /**
     * Returns the threshold which is minimal to craft the recipe. Negative pressures also work.
     * @return threshold pressure
     */
    @Override
    public float getCraftingPressure(){
        return -0.75F;
    }

    /**
     * This method should return the used items in the recipe when the right items are provided to craft this recipe.
     * @param inputStacks
     * @return usedStacks, return null when the inputStacks aren't valid for this recipe.
     */
    @Override
    public ItemStack[] isValidRecipe(ItemStack[] inputStacks){
        int enchantmentCount = 0;
        for(ItemStack stack : inputStacks) {
            if(stack.getItem() == Items.enchanted_book) continue;
            enchantmentCount = EnchantmentHelper.getEnchantments(stack).size();
            if(enchantmentCount > 0) break;
        }
        if(enchantmentCount == 0) return null;
        for(ItemStack stack : inputStacks) {
            if(stack.getItem() == Items.book) {
                return new ItemStack[]{new ItemStack(Items.book)};
            }
        }
        return null;

    }

    /**
     * This method will be called when the recipe should output its items. te stacks the recipe outputs, maybe dependent on the input stacks.
     * @param inputStacks. These stacks can be modified (like adding/removing NBT data eg.)
     * @return outputStacks. Stacks that will pop 'out of the chamber'
     */
    @Override
    public ItemStack[] craftRecipe(ItemStack[] inputStacks, ItemStack[] removedStacks){
        ItemStack enchantedStack = null;
        int enchantmentCount = 0;
        for(ItemStack stack : inputStacks) {
            if(stack.getItem() == Items.enchanted_book) continue;
            enchantmentCount = EnchantmentHelper.getEnchantments(stack).size();
            if(enchantmentCount > 0) {
                enchantedStack = stack;
                break;
            }
        }
        if(enchantedStack == null) {
            System.err.println("[Pressure Chamber Vacuum Enchantment Handler] No enchanted stack found! Report to MineMaarten!");
            return null;
        }

        // take a random enchantment of the enchanted stack.
        int enchIndex = new Random().nextInt(((NBTTagList)enchantedStack.getTagCompound().getTag("ench")).tagCount());
        NBTTagCompound enchTag = ((NBTTagList)enchantedStack.getTagCompound().getTag("ench")).getCompoundTagAt(enchIndex);

        ((NBTTagList)enchantedStack.getTagCompound().getTag("ench")).removeTag(enchIndex);
        if(((NBTTagList)enchantedStack.getTagCompound().getTag("ench")).tagCount() == 0) {
            enchantedStack.getTagCompound().removeTag("ench");
            if(enchantedStack.getTagCompound().hasNoTags()) {
                enchantedStack.setTagCompound(null);
            }
        }

        //and create an enchanted book.
        ItemStack enchantedBook = new ItemStack(Items.enchanted_book);
        NBTTagCompound baseTag = new NBTTagCompound();
        NBTTagList enchList = new NBTTagList();
        enchList.appendTag(enchTag);
        baseTag.setTag("StoredEnchantments", enchList);
        enchantedBook.setTagCompound(baseTag);
        return new ItemStack[]{enchantedBook};
    }

    @Override
    public boolean shouldRemoveExactStacks(){
        return false;
    }

}
