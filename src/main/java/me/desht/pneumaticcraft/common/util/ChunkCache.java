package me.desht.pneumaticcraft.common.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ICollisionReader;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunk;

import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Like a Region, but should be OK to use get a cache from a different thread (since we go through
 * ServerChunkProvider#getChunk(), which returns async chunk supplier when called off the main thread.
 * This should only be used server-side.
 */
public class ChunkCache implements ICollisionReader {
    protected final int chunkX;
    protected final int chunkZ;
    protected final IChunk[][] chunks;
    protected boolean empty;
    protected final World world;

    public ChunkCache(World worldIn, BlockPos pos1, BlockPos pos2) {
        this.world = worldIn;
        this.chunkX = pos1.getX() >> 4;
        this.chunkZ = pos1.getZ() >> 4;
        int endX = pos2.getX() >> 4;
        int endZ = pos2.getZ() >> 4;
        this.chunks = new IChunk[endX - this.chunkX + 1][endZ - this.chunkZ + 1];
        this.empty = true;

        for (int x = this.chunkX; x <= endX; ++x) {
            for (int z = this.chunkZ; z <= endZ; ++z) {
                this.chunks[x - this.chunkX][z - this.chunkZ] = world.getChunk(x, z, ChunkStatus.FULL, true);
            }
        }

        for (int x = pos1.getX() >> 4; x <= pos2.getX() >> 4; ++x) {
            for (int z = pos1.getZ() >> 4; z <= pos2.getZ() >> 4; ++z) {
                IChunk ichunk = this.chunks[x - this.chunkX][z - this.chunkZ];
                if (ichunk != null && !ichunk.isEmptyBetween(pos1.getY(), pos2.getY())) {
                    this.empty = false;
                    return;
                }
            }
        }
    }

    private IChunk getChunk(BlockPos pos) {
        return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private IChunk getChunk(int chunkX, int chunkZ) {
        int x = chunkX - this.chunkX;
        int z = chunkZ - this.chunkZ;
        if (x >= 0 && x < this.chunks.length && z >= 0 && z < this.chunks[x].length) {
            IChunk ichunk = this.chunks[x][z];
            return (ichunk != null ? ichunk : new EmptyChunk(this.world, new ChunkPos(chunkX, chunkZ)));
        } else {
            return new EmptyChunk(this.world, new ChunkPos(chunkX, chunkZ));
        }
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.world.getWorldBorder();
    }

    @Override
    public IBlockReader getBlockReader(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ);
    }

    @Override
    public Stream<VoxelShape> func_230318_c_(@Nullable Entity p_230318_1_, AxisAlignedBB p_230318_2_, Predicate<Entity> p_230318_3_) {
        return Stream.empty();
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        IChunk ichunk = this.getChunk(pos);
        return ichunk.getTileEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (World.isOutsideBuildHeight(pos)) {
            return Blocks.AIR.getDefaultState();
        } else {
            IChunk ichunk = this.getChunk(pos);
            return ichunk.getBlockState(pos);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        if (World.isOutsideBuildHeight(pos)) {
            return Fluids.EMPTY.getDefaultState();
        } else {
            IChunk ichunk = this.getChunk(pos);
            return ichunk.getFluidState(pos);
        }
    }
}
