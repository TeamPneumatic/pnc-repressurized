package me.desht.pneumaticcraft.api.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Should be implemented by any block or FMP that allows to be rotated by a Pneumatic Wrench. It uses almost the same
 * rotate method as the Vanilla (Forge) method. However it uses energy to rotate (when rotateBlock() return true).
 */
public interface IPneumaticWrenchable {

    boolean rotateBlock(World world, EntityPlayer player, BlockPos pos, EnumFacing side);
}
