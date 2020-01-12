package me.desht.pneumaticcraft.common.block;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;

public class BlockKeroseneLampLight extends AirBlock {
    public BlockKeroseneLampLight() {
        super(Block.Properties.create(Material.AIR).doesNotBlockMovement().lightValue(15).noDrops());
    }

    @Override
    public int getLightValue(BlockState p_149750_1_) {
        return 15;
    }
}
