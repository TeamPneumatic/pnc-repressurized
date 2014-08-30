package pneumaticCraft.common.itemBlock;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlockAdvancedPressureTube extends ItemBlockPressureTube{

    public ItemBlockAdvancedPressureTube(Block block){
        super(block);
    }

    @Override
    public String getItemStackDisplayName(ItemStack is){
        return (is.getItemDamage() > 0 ? "Advanced " : "") + super.getItemStackDisplayName(is);
    }
}
