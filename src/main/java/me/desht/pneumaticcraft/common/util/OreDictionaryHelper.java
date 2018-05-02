package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class OreDictionaryHelper {
    public static boolean isItemEqual(String oreDictName, ItemStack stack) {
        for (ItemStack s : OreDictionary.getOres(oreDictName)) {
            if (OreDictionary.itemMatches(stack, s, false)) return true;
        }
        return false;
    }

    public static void addOreDictEntries() {
        OreDictionary.registerOre(Names.INGOT_IRON_COMPRESSED, Itemss.INGOT_IRON_COMPRESSED);
        OreDictionary.registerOre(Names.BLOCK_IRON_COMPRESSED, Blockss.COMPRESSED_IRON);
        OreDictionary.registerOre(Names.GEAR_IRON_COMPRESSED, Itemss.COMPRESSED_IRON_GEAR);
        for (int i = 0; i < ItemPlastic.ORE_NAMES.length; i++){
            OreDictionary.registerOre(ItemPlastic.ORE_NAMES[i], new ItemStack(Itemss.PLASTIC, 1, i));
        }
    }
}
