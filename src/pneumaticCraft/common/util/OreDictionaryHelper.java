package pneumaticCraft.common.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class OreDictionaryHelper{
    private static Map<String, List<ItemStack>> entryCache = new HashMap<String, List<ItemStack>>();

    public static List<ItemStack> getOreDictEntries(String oreDictName){
        List<ItemStack> list = entryCache.get(oreDictName);
        if(list == null) {
            list = OreDictionary.getOres(oreDictName, true);
            entryCache.put(oreDictName, list);
        }
        return list;
    }

    public static boolean isItemEqual(String oreDictName, ItemStack stack){
        for(ItemStack s : getOreDictEntries(oreDictName)) {
            if(OreDictionary.itemMatches(stack, s, false)) return true;
        }
        return false;
    }

}
