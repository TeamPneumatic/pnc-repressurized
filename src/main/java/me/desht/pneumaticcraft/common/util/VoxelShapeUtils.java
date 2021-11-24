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

import net.minecraft.block.Block;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.HashSet;
import java.util.Set;

/**
 * With thanks to JTK222 | Lukas for this
 */
public class VoxelShapeUtils {
    public static VoxelShape rotateY(VoxelShape shape, int rotation) {
        Set<VoxelShape> rotatedShapes = new HashSet<>();

        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            x1 = (x1 * 16) - 8; x2 = (x2 * 16) - 8;
            z1 = (z1 * 16) - 8; z2 = (z2 * 16) - 8;

            switch (rotation) {
                case 90:
                    rotatedShapes.add(Block.box(8 - z1, y1 * 16, 8 + x1, 8 - z2, y2 * 16, 8 + x2));
                    break;
                case 180:
                    rotatedShapes.add(Block.box(8 - x1, y1 * 16, 8 - z1, 8 - x2, y2 * 16, 8 - z2));
                    break;
                case 270:
                    rotatedShapes.add(Block.box(8 + z1, y1 * 16, 8 - x1, 8 + z2, y2 * 16, 8 - x2));
                    break;
                default:
                    throw new IllegalArgumentException("invalid rotation " + rotation + " (must be 90,180 or 270)");
            }
        });

        return rotatedShapes.stream().reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).orElse(shape);
    }

    public static VoxelShape rotateX(VoxelShape shape, int rotation) {
        Set<VoxelShape> rotatedShapes = new HashSet<>();

        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            y1 = (y1 * 16) - 8; y2 = (y2 * 16) - 8;
            z1 = (z1 * 16) - 8; z2 = (z2 * 16) - 8;

            switch (rotation) {
                case 90:
                    rotatedShapes.add(Block.box(x1 * 16, 8 - z1, 8 + y1, x2 * 16, 8 - z2, 8 + y2));
                    break;
                case 180:
                    rotatedShapes.add(Block.box(x1 * 16, 8 - z1, 8 - y1, x2 * 16, 8 - z2, 8 - y2));
                    break;
                case 270:
                    rotatedShapes.add(Block.box(x1 * 16, 8 + z1, 8 - y1, x2 * 16, 8 + z2, 8 - y2));
                    break;
                default:
                    throw new IllegalArgumentException("invalid rotation " + rotation + " (must be 90,180 or 270)");
            }
        });

        return rotatedShapes.stream().reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).orElse(shape);
    }

    public static VoxelShape combine(IBooleanFunction func, VoxelShape... shapes) {
        VoxelShape result = VoxelShapes.empty();
        for (VoxelShape shape : shapes) {
            result = VoxelShapes.joinUnoptimized(result, shape, func);
        }
        return result.optimize();
    }
}
