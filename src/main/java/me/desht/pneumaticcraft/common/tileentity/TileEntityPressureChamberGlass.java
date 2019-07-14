package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;

public class TileEntityPressureChamberGlass extends TileEntityPressureChamberWall {
    public static final ModelProperty<Integer> DOWN = new ModelProperty<>();
    public static final ModelProperty<Integer> UP = new ModelProperty<>();
    public static final ModelProperty<Integer> NORTH = new ModelProperty<>();
    public static final ModelProperty<Integer> SOUTH = new ModelProperty<>();
    public static final ModelProperty<Integer> WEST = new ModelProperty<>();
    public static final ModelProperty<Integer> EAST = new ModelProperty<>();
    private static final int[] TEXTURE_LOOKUP_TABLE = {
            0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15,
            1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14,
            0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15,
            1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14,
            4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26,
            16, 16, 20, 20, 16, 16, 28, 28, 21, 21, 46, 42, 21, 21, 43, 38,
            4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12,
            16, 16, 20, 20, 16, 16, 28, 28, 25, 25, 45, 37, 25, 25, 40, 32,
            0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15,
            1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14,
            0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15,
            1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14,
            4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26,
            7, 7, 24, 24, 7, 7, 10, 10, 29, 29, 44, 41, 29, 29, 39, 33,
            4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12,
            7, 7, 24, 24, 7, 7, 10, 10, 8, 8, 36, 35, 8, 8, 34, 11
    };

    public TileEntityPressureChamberGlass() {
        super(ModTileEntityTypes.PRESSURE_CHAMBER_GLASS, 0);
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();

        requestModelDataUpdate();
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(DOWN, getTextureIndex(world, pos, Direction.DOWN))
                .withInitial(UP, getTextureIndex(world, pos, Direction.UP))
                .withInitial(NORTH, getTextureIndex(world, pos, Direction.NORTH))
                .withInitial(SOUTH, getTextureIndex(world, pos, Direction.SOUTH))
                .withInitial(WEST, getTextureIndex(world, pos, Direction.WEST))
                .withInitial(EAST, getTextureIndex(world, pos, Direction.EAST))
                .build();
    }

    private int getTextureIndex(IBlockReader world, BlockPos pos, Direction face) {
        boolean[] bitMatrix = new boolean[8];
        switch (face) {
            case DOWN:case UP:
                bitMatrix[0] = isGlass(world, pos.add(face == Direction.DOWN ? 1 : -1, 0, -1));
                bitMatrix[1] = isGlass(world, pos.add(0, 0, -1));
                bitMatrix[2] = isGlass(world, pos.add(face == Direction.UP ? 1 : -1,  0, -1));
                bitMatrix[3] = isGlass(world, pos.add(face == Direction.DOWN ? 1 : -1, 0, 0));
                bitMatrix[4] = isGlass(world, pos.add(face == Direction.UP ? 1 : - 1, 0, 0));
                bitMatrix[5] = isGlass(world, pos.add(face == Direction.DOWN ? 1 : -1, 0, 1));
                bitMatrix[6] = isGlass(world, pos.add(0, 0, 1));
                bitMatrix[7] = isGlass(world, pos.add(face == Direction.UP ? 1 : - 1, 0, 1));
                break;
            case NORTH:case SOUTH:
                bitMatrix[0] = isGlass(world, pos.add(face == Direction.NORTH ? 1 : -1, 1, 0));
                bitMatrix[1] = isGlass(world, pos.add(0, 1, 0));
                bitMatrix[2] = isGlass(world, pos.add(face == Direction.SOUTH ? 1 : -1, 1, 0));
                bitMatrix[3] = isGlass(world, pos.add(face == Direction.NORTH ? 1 : -1, 0, 0));
                bitMatrix[4] = isGlass(world, pos.add(face == Direction.SOUTH ? 1 : -1, 0, 0));
                bitMatrix[5] = isGlass(world, pos.add(face == Direction.NORTH ? 1 : -1, -1, 0));
                bitMatrix[6] = isGlass(world, pos.add(0, -1, 0));
                bitMatrix[7] = isGlass(world, pos.add(face == Direction.SOUTH ? 1 : -1, -1, 0));
                break;
            case WEST:case EAST:
                bitMatrix[0] = isGlass(world, pos.add(0, 1, face == Direction.EAST ? 1 : -1));
                bitMatrix[1] = isGlass(world, pos.add(0, 1, 0));
                bitMatrix[2] = isGlass(world, pos.add(0, 1, face == Direction.WEST ? 1 : -1));
                bitMatrix[3] = isGlass(world, pos.add(0, 0, face == Direction.EAST ? 1 : -1));
                bitMatrix[4] = isGlass(world, pos.add(0, 0, face == Direction.WEST ? 1 : -1));
                bitMatrix[5] = isGlass(world, pos.add(0, -1, face == Direction.EAST ? 1 : -1));
                bitMatrix[6] = isGlass(world, pos.add(0, -1, 0));
                bitMatrix[7] = isGlass(world, pos.add(0, -1, face == Direction.WEST ? 1 : -1));
                break;
        }

        int idBuilder = 0;
        for (int i = 0; i < bitMatrix.length; i++) idBuilder |= (bitMatrix[i] ? 1 << i : 0);
        return idBuilder > 255 || idBuilder < 0 ? 0 : TEXTURE_LOOKUP_TABLE[idBuilder];
    }

    private boolean isGlass(IBlockReader world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == ModBlocks.PRESSURE_CHAMBER_GLASS;
    }
}
