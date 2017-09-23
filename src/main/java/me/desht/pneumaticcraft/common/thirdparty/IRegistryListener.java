package me.desht.pneumaticcraft.common.thirdparty;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface IRegistryListener {
    void onItemRegistry(Item item);

    void onBlockRegistry(Block block);
}