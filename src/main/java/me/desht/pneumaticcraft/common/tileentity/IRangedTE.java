package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.HashSet;
import java.util.Set;

public interface IRangedTE {
    void toggleShowRange();

    boolean shouldShowRange();

    int getRange();

    default ITextComponent rangeText() {
        return new StringTextComponent("R").mergeStyle(shouldShowRange() ? TextFormatting.AQUA : TextFormatting.GRAY);
    }

    static Set<BlockPos> getFrame(AxisAlignedBB extents) {
        Set<BlockPos> res = new HashSet<>();
        int minX = (int) extents.minX;
        int minY = (int) extents.minY;
        int minZ = (int) extents.minZ;
        int maxX = (int) extents.maxX;
        int maxY = (int) extents.maxY;
        int maxZ = (int) extents.maxZ;
        for (int x = minX; x <= maxX; x++) {
            for (int y = Math.max(0, minY); y <= maxY && y < 256; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                        res.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return res;
    }
}
