package me.desht.pneumaticcraft.api.block;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Should be implemented by any block that can be rotated by a Pneumatic Wrench. It uses almost the same
 * rotate method as the Vanilla (Forge) method. However it uses energy to rotate (when rotateBlock() return true).
 * Also, this method gets passed the player who did the rotation.
 */
public interface IPneumaticWrenchable {
    boolean rotateBlock(World world, EntityPlayer player, BlockPos pos, EnumFacing side, EnumHand hand);

    static IPneumaticWrenchable forBlock(Block b) {
        return b instanceof IPneumaticWrenchable ? (IPneumaticWrenchable) b : null;
    }
}
