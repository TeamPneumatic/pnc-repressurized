package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OreDictionaryHelper {
    private static Map<String, List<ItemStack>> entryCache = new HashMap<String, List<ItemStack>>();

    public static List<ItemStack> getOreDictEntries(String oreDictName) {
        List<ItemStack> list = entryCache.get(oreDictName);
        if (list == null) {
            list = OreDictionary.getOres(oreDictName);
            entryCache.put(oreDictName, list);
        }
        return list;
    }

    public static boolean isItemEqual(String oreDictName, ItemStack stack) {
        for (ItemStack s : getOreDictEntries(oreDictName)) {
            if (OreDictionary.itemMatches(stack, s, false)) return true;
        }
        return false;
    }

    public static void addOreDictEntries() {
        OreDictionary.registerOre(Names.INGOT_IRON_COMPRESSED, Itemss.INGOT_IRON_COMPRESSED);
        OreDictionary.registerOre(Names.BLOCK_IRON_COMPRESSED, Blockss.COMPRESSED_IRON);
    }
}
