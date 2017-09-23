package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.common.block.Blockss;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CreativeTabPneumaticCraft extends CreativeTabs {

    public CreativeTabPneumaticCraft(String par2Str) {
        super(par2Str);
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(Item.getItemFromBlock(Blockss.AIR_CANNON));
    }

}
