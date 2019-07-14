package me.desht.pneumaticcraft.client.semiblock;

import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShapes;

public interface ISemiBlockRenderer<S extends ISemiBlock> {
    void render(S semiBlock, float partialTick);

    default AxisAlignedBB getBounds(S semiBlock) {
        return semiBlock.getWorld() == null ?
                VoxelShapes.fullCube().getBoundingBox() :
                semiBlock.getWorld().getBlockState(semiBlock.getPos()).getShape(semiBlock.getWorld(), semiBlock.getPos()).getBoundingBox();
    }
}
