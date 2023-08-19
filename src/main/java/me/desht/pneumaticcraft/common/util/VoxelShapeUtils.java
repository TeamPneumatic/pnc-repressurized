/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;

/**
 * With thanks to JTK222 | Lukas for this
 */
public class VoxelShapeUtils {
    public static VoxelShape rotateY(VoxelShape shape, int rotation) {
        List<VoxelShape> rotatedShapes = new ArrayList<>();

        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            x1 = (x1 * 16) - 8; x2 = (x2 * 16) - 8;
            z1 = (z1 * 16) - 8; z2 = (z2 * 16) - 8;

            switch (rotation) {
                case 90 -> rotatedShapes.add(boxSafe(8 - z1, y1 * 16, 8 + x1, 8 - z2, y2 * 16, 8 + x2));
                case 180 -> rotatedShapes.add(boxSafe(8 - x1, y1 * 16, 8 - z1, 8 - x2, y2 * 16, 8 - z2));
                case 270 -> rotatedShapes.add(boxSafe(8 + z1, y1 * 16, 8 - x1, 8 + z2, y2 * 16, 8 - x2));
                default -> throw new IllegalArgumentException("invalid rotation " + rotation + " (must be 90,180 or 270)");
            }
        });

        return rotatedShapes.stream().reduce((v1, v2) -> Shapes.joinUnoptimized(v1, v2, BooleanOp.OR)).orElse(shape).optimize();
    }

    public static VoxelShape rotateX(VoxelShape shape, int rotation) {
        List<VoxelShape> rotatedShapes = new ArrayList<>();

        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            y1 = (y1 * 16) - 8; y2 = (y2 * 16) - 8;
            z1 = (z1 * 16) - 8; z2 = (z2 * 16) - 8;

            switch (rotation) {
                case 90 -> rotatedShapes.add(boxSafe(x1 * 16, 8 - z1, 8 + y1, x2 * 16, 8 - z2, 8 + y2));
                case 180 -> rotatedShapes.add(boxSafe(x1 * 16, 8 - z1, 8 - y1, x2 * 16, 8 - z2, 8 - y2));
                case 270 -> rotatedShapes.add(boxSafe(x1 * 16, 8 + z1, 8 - y1, x2 * 16, 8 + z2, 8 - y2));
                default -> throw new IllegalArgumentException("invalid rotation " + rotation + " (must be 90,180 or 270)");
            }
        });

        return rotatedShapes.stream().reduce((v1, v2) -> Shapes.joinUnoptimized(v1, v2, BooleanOp.OR)).orElse(shape).optimize();
    }

    public static VoxelShape combine(BooleanOp func, VoxelShape... shapes) {
        return Arrays.stream(shapes).reduce((v1, v2) -> Shapes.joinUnoptimized(v1, v2, func)).orElseThrow().optimize();
    }

    public static VoxelShape or(VoxelShape... shapes) {
        return combine(BooleanOp.OR, shapes);
    }

    private static VoxelShape boxSafe(double pMinX, double pMinY, double pMinZ, double pMaxX, double pMaxY, double pMaxZ) {
        // MC 1.17+ is picky about min/max order, unlike 1.16 and earlier...
        double x1 = Math.min(pMinX, pMaxX);
        double x2 = Math.max(pMinX, pMaxX);
        double y1 = Math.min(pMinY, pMaxY);
        double y2 = Math.max(pMinY, pMaxY);
        double z1 = Math.min(pMinZ, pMaxZ);
        double z2 = Math.max(pMinZ, pMaxZ);
        return Block.box(x1, y1, z1, x2, y2, z2);
    }
}
