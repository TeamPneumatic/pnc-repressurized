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

package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import java.util.List;

public class TileEntityPressureChamberGlass extends TileEntityPressureChamberWall {
    public static final ModelProperty<Integer> DOWN = new ModelProperty<>();
    public static final ModelProperty<Integer> UP = new ModelProperty<>();
    public static final ModelProperty<Integer> NORTH = new ModelProperty<>();
    public static final ModelProperty<Integer> SOUTH = new ModelProperty<>();
    public static final ModelProperty<Integer> WEST = new ModelProperty<>();
    public static final ModelProperty<Integer> EAST = new ModelProperty<>();
    public static final List<ModelProperty<Integer>> DIR_PROPS = ImmutableList.of(DOWN, UP, NORTH, SOUTH, WEST, EAST);

    // This comes from Amadornes, in the Blue Power mod.  I'm not gonna pretend to understand it...
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

    public TileEntityPressureChamberGlass(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRESSURE_CHAMBER_GLASS.get(), pos, state,0);
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(DOWN, getTextureIndex(level, worldPosition, Direction.DOWN))
                .withInitial(UP, getTextureIndex(level, worldPosition, Direction.UP))
                .withInitial(NORTH, getTextureIndex(level, worldPosition, Direction.NORTH))
                .withInitial(SOUTH, getTextureIndex(level, worldPosition, Direction.SOUTH))
                .withInitial(WEST, getTextureIndex(level, worldPosition, Direction.WEST))
                .withInitial(EAST, getTextureIndex(level, worldPosition, Direction.EAST))
                .build();
    }

    private int getTextureIndex(BlockGetter world, BlockPos pos, Direction face) {
        boolean[] bitMatrix = new boolean[8];
        switch (face) {
            case DOWN, UP -> {
                bitMatrix[0] = isGlass(world, pos.offset(face == Direction.DOWN ? 1 : -1, 0, -1));
                bitMatrix[1] = isGlass(world, pos.offset(0, 0, -1));
                bitMatrix[2] = isGlass(world, pos.offset(face == Direction.UP ? 1 : -1, 0, -1));
                bitMatrix[3] = isGlass(world, pos.offset(face == Direction.DOWN ? 1 : -1, 0, 0));
                bitMatrix[4] = isGlass(world, pos.offset(face == Direction.UP ? 1 : -1, 0, 0));
                bitMatrix[5] = isGlass(world, pos.offset(face == Direction.DOWN ? 1 : -1, 0, 1));
                bitMatrix[6] = isGlass(world, pos.offset(0, 0, 1));
                bitMatrix[7] = isGlass(world, pos.offset(face == Direction.UP ? 1 : -1, 0, 1));
            }
            case NORTH, SOUTH -> {
                bitMatrix[0] = isGlass(world, pos.offset(face == Direction.NORTH ? 1 : -1, 1, 0));
                bitMatrix[1] = isGlass(world, pos.offset(0, 1, 0));
                bitMatrix[2] = isGlass(world, pos.offset(face == Direction.SOUTH ? 1 : -1, 1, 0));
                bitMatrix[3] = isGlass(world, pos.offset(face == Direction.NORTH ? 1 : -1, 0, 0));
                bitMatrix[4] = isGlass(world, pos.offset(face == Direction.SOUTH ? 1 : -1, 0, 0));
                bitMatrix[5] = isGlass(world, pos.offset(face == Direction.NORTH ? 1 : -1, -1, 0));
                bitMatrix[6] = isGlass(world, pos.offset(0, -1, 0));
                bitMatrix[7] = isGlass(world, pos.offset(face == Direction.SOUTH ? 1 : -1, -1, 0));
            }
            case WEST, EAST -> {
                bitMatrix[0] = isGlass(world, pos.offset(0, 1, face == Direction.EAST ? 1 : -1));
                bitMatrix[1] = isGlass(world, pos.offset(0, 1, 0));
                bitMatrix[2] = isGlass(world, pos.offset(0, 1, face == Direction.WEST ? 1 : -1));
                bitMatrix[3] = isGlass(world, pos.offset(0, 0, face == Direction.EAST ? 1 : -1));
                bitMatrix[4] = isGlass(world, pos.offset(0, 0, face == Direction.WEST ? 1 : -1));
                bitMatrix[5] = isGlass(world, pos.offset(0, -1, face == Direction.EAST ? 1 : -1));
                bitMatrix[6] = isGlass(world, pos.offset(0, -1, 0));
                bitMatrix[7] = isGlass(world, pos.offset(0, -1, face == Direction.WEST ? 1 : -1));
            }
        }

        int idBuilder = 0;
        for (int i = 0; i < bitMatrix.length; i++) idBuilder |= (bitMatrix[i] ? 1 << i : 0);
        return idBuilder > 255 || idBuilder < 0 ? 0 : TEXTURE_LOOKUP_TABLE[idBuilder];
    }

    private boolean isGlass(BlockGetter world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == ModBlocks.PRESSURE_CHAMBER_GLASS.get();
    }
}
