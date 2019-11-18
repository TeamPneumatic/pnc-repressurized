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

        shape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
            x1 = (x1 * 16) - 8; x2 = (x2 * 16) - 8;
            z1 = (z1 * 16) - 8; z2 = (z2 * 16) - 8;

            switch (rotation) {
                case 90:
                    rotatedShapes.add(Block.makeCuboidShape(8 - z1, y1 * 16, 8 + x1, 8 - z2, y2 * 16, 8 + x2));
                    break;
                case 180:
                    rotatedShapes.add(Block.makeCuboidShape(8 - x1, y1 * 16, 8 - z1, 8 - x2, y2 * 16, 8 - z2));
                    break;
                case 270:
                    rotatedShapes.add(Block.makeCuboidShape(8 + z1, y1 * 16, 8 - x1, 8 + z2, y2 * 16, 8 - x2));
                    break;
                default:
                    throw new IllegalArgumentException("invalid rotation " + rotation + " (must be 90,180 or 270)");
            }
        });

        return rotatedShapes.stream().reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR)).get();
    }

    public static VoxelShape rotateX(VoxelShape shape, int rotation) {
        Set<VoxelShape> rotatedShapes = new HashSet<>();

        shape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
            y1 = (y1 * 16) - 8; y2 = (y2 * 16) - 8;
            z1 = (z1 * 16) - 8; z2 = (z2 * 16) - 8;

            switch (rotation) {
                case 90:
                    rotatedShapes.add(Block.makeCuboidShape(x1 * 16, 8 - z1, 8 + y1, x2 * 16, 8 - z2, 8 + y2));
                    break;
                case 180:
                    rotatedShapes.add(Block.makeCuboidShape(x1 * 16, 8 - z1, 8 - y1, x2 * 16, 8 - z2, 8 - y2));
                    break;
                case 270:
                    rotatedShapes.add(Block.makeCuboidShape(x1 * 16, 8 + z1, 8 - y1, x2 * 16, 8 + z2, 8 - y2));
                    break;
                default:
                    throw new IllegalArgumentException("invalid rotation " + rotation + " (must be 90,180 or 270)");
            }
        });

        return rotatedShapes.stream().reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR)).get();
    }
}
