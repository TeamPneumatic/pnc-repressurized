package pneumaticCraft.client;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import pneumaticCraft.common.block.Blockss;

public class CreativeTabPneumaticCraft extends CreativeTabs{

    public CreativeTabPneumaticCraft(String par2Str){
        super(par2Str);
    }

    @Override
    public Item getTabIconItem(){
        return Item.getItemFromBlock(Blockss.airCannon);
    }

}
