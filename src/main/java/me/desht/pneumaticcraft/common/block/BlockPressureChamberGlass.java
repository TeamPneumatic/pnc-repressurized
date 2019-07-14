package me.desht.pneumaticcraft.common.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPressureChamberGlass extends BlockPressureChamberWallBase {

    public static final IUnlistedProperty<Integer> DOWN = new PropertyObject<>("down", Integer.class);
    public static final IUnlistedProperty<Integer> UP = new PropertyObject<>("up", Integer.class);
    public static final IUnlistedProperty<Integer> NORTH = new PropertyObject<>("north", Integer.class);
    public static final IUnlistedProperty<Integer> SOUTH = new PropertyObject<>("south", Integer.class);
    public static final IUnlistedProperty<Integer> WEST = new PropertyObject<>("west", Integer.class);
    public static final IUnlistedProperty<Integer> EAST = new PropertyObject<>("east", Integer.class);
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

    public BlockPressureChamberGlass() {
        super("pressure_chamber_glass");
        setResistance(20000.f);
    }

    private boolean isGlass(IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == this;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        IProperty[] listedProperties = new IProperty[0]; // no listed properties
        IUnlistedProperty[] unlistedProperties = new IUnlistedProperty[] { DOWN, UP, NORTH, SOUTH, WEST, EAST };
        return new ExtendedBlockState(this, listedProperties, unlistedProperties);
    }

    @Override
    public BlockState getExtendedState(BlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;

        return extendedBlockState
                .withProperty(DOWN, getTextureIndex(world, pos, Direction.DOWN))
                .withProperty(UP, getTextureIndex(world, pos, Direction.UP))
                .withProperty(NORTH, getTextureIndex(world, pos, Direction.NORTH))
                .withProperty(SOUTH, getTextureIndex(world, pos, Direction.SOUTH))
                .withProperty(WEST, getTextureIndex(world, pos, Direction.WEST))
                .withProperty(EAST, getTextureIndex(world, pos, Direction.EAST));
    }

    private int getTextureIndex(IBlockAccess world, BlockPos pos, Direction face) {
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

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(BlockState blockState, IBlockAccess world, BlockPos pos, Direction side) {
        BlockState iblockstate = world.getBlockState(pos.offset(side));
        return !iblockstate.isOpaqueCube() && iblockstate.getBlock() != this;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

}
