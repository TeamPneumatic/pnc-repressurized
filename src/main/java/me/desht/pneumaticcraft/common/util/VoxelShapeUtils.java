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
    public enum RotationAmount {
        NINETY,
        HUNDRED_EIGHTY,
        TWO_HUNDRED_SEVENTY
    }

    public static VoxelShape rotateShape(VoxelShape shape, RotationAmount rotation) {
        Set<VoxelShape> rotatedShapes = new HashSet<>();

        shape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
            x1 = (x1 * 16) - 8; x2 = (x2 * 16) - 8;
            z1 = (z1 * 16) - 8; z2 = (z2 * 16) - 8;

            switch (rotation) {
                case NINETY:
                    rotatedShapes.add(Block.makeCuboidShape(8 - z1, y1 * 16, 8 + x1, 8 - z2, y2 * 16, 8 + x2));
                    break;
                case HUNDRED_EIGHTY:
                    rotatedShapes.add(Block.makeCuboidShape(8 - x1, y1 * 16, 8 - z1, 8 - x2, y2 * 16, 8 - z2));
                    break;
                case TWO_HUNDRED_SEVENTY:
                    rotatedShapes.add(Block.makeCuboidShape(8 + z1, y1 * 16, 8 - x1, 8 + z2, y2 * 16, 8 - x2));
                    break;
            }
        });

        return rotatedShapes.stream().reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR)).get();
    }
}
