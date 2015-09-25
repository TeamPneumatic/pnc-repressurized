package pneumaticCraft.common.block;

import net.minecraft.block.BlockAir;
import net.minecraft.world.IBlockAccess;

public class BlockKeroseneLampLight extends BlockAir{

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z){
        return 15;//world.getBlockMetadata(x, y, z);
    }
}
