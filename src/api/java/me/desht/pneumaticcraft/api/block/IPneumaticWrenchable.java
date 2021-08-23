package me.desht.pneumaticcraft.api.block;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Should be implemented by any block or entity that can be clicked by a Pneumatic Wrench. It uses almost the same
 * rotate method as the Vanilla (Forge) method. However it uses energy to rotate (when rotateBlock() return true).
 * Also, this method gets passed the player who did the rotation.
 */
public interface IPneumaticWrenchable {
    boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction side, Hand hand);

    static IPneumaticWrenchable forBlock(Block b) {
        return b instanceof IPneumaticWrenchable ? (IPneumaticWrenchable) b : null;
    }
}
