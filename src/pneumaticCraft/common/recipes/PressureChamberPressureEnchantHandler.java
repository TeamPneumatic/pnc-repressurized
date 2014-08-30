package pneumaticCraft.common.recipes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import pneumaticCraft.api.recipe.IPressureChamberRecipe;

public class PressureChamberPressureEnchantHandler implements IPressureChamberRecipe{

    @Override
    public float getCraftingPressure(){
        return 2F;
    }

    @Override
    public ItemStack[] isValidRecipe(ItemStack[] inputStacks){
        List<ItemStack> enchantedBooks = new ArrayList<ItemStack>();
        for(ItemStack book : inputStacks) {
            if(book.getItem() == Items.enchanted_book) enchantedBooks.add(book);
        }
        if(enchantedBooks.size() == 0) return null;
        for(ItemStack inputStack : inputStacks) {
            if((inputStack.isItemEnchantable() || inputStack.isItemEnchanted()) && inputStack.getItem() != Items.enchanted_book) {
                //Map map = EnchantmentHelper.getEnchantments(inputStack);
                // Iterator toolEnchantIterator = map.keySet().iterator();
                // while(toolEnchantIterator.hasNext()){
                //    Enchantment toolEnchant = Enchantment.enchantmentsList[((Integer)toolEnchantIterator.next()).intValue()];
                for(ItemStack enchantedBook : enchantedBooks) {
                    Map bookMap = EnchantmentHelper.getEnchantments(enchantedBook);
                    Iterator bookEnchantIterator = bookMap.keySet().iterator();
                    while(bookEnchantIterator.hasNext()) {
                        Enchantment bookEnchant = Enchantment.enchantmentsList[((Integer)bookEnchantIterator.next()).intValue()];
                        if(bookEnchant.canApply(inputStack)) {
                            return new ItemStack[]{inputStack, enchantedBook};
                        }
                    }
                }
                //  }
            }
        }
        return null;
    }

    @Override
    public ItemStack[] craftRecipe(ItemStack[] inputStacks, ItemStack[] removedStacks){
        //ItemStack enchantedTool = removedStacks[0];
        ItemStack enchantedBook = removedStacks[1];
        Map bookMap = EnchantmentHelper.getEnchantments(enchantedBook);
        Iterator bookEnchantIterator = bookMap.keySet().iterator();
        while(bookEnchantIterator.hasNext()) {
            //     Enchantment bookEnchant = Enchantment.enchantmentsList[((Integer)bookEnchantIterator.next()).intValue()];
            //enchantedTool.addEnchantment(bookEnchant, par2)
        }
        return null;
    }

    @Override
    public boolean shouldRemoveExactStacks(){
        return true;
    }

}
