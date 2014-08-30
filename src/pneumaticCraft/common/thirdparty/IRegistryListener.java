package pneumaticCraft.common.thirdparty;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface IRegistryListener{
    public void onItemRegistry(Item item);

    public void onBlockRegistry(Block block);
}