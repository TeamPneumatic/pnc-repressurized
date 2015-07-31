package pneumaticCraft.common.itemBlock;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlockPressureChamberWall extends ItemBlockPneumaticCraft{

    public ItemBlockPressureChamberWall(Block block){
        super(block);
        setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack is){
        switch(is.getItemDamage()){
            case 0:
                return super.getUnlocalizedName(is) + ".wall";
            case 6:
                return super.getUnlocalizedName(is) + ".window";
        }
        return super.getUnlocalizedName(is) + ".wall";
    }

    @Override
    public int getMetadata(int meta){
        return meta;
    }

}
