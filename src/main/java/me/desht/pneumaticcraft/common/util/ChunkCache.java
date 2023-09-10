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

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Like a Region, but should be OK to use get a cache from a different thread (since we go through
 * ServerChunkProvider#getChunk(), which returns async chunk supplier when called off the main thread.
 * This should only be used server-side.
 */
public class ChunkCache implements CollisionGetter {
    protected final int chunkX;
    protected final int chunkZ;
    protected final ChunkAccess[][] chunks;
    protected boolean empty;
    protected final Level world;

    public ChunkCache(Level worldIn, BlockPos pos1, BlockPos pos2) {
        this.world = worldIn;
        this.chunkX = pos1.getX() >> 4;
        this.chunkZ = pos1.getZ() >> 4;
        int endX = pos2.getX() >> 4;
        int endZ = pos2.getZ() >> 4;
        this.chunks = new ChunkAccess[endX - this.chunkX + 1][endZ - this.chunkZ + 1];
        this.empty = true;

        for (int x = this.chunkX; x <= endX; ++x) {
            for (int z = this.chunkZ; z <= endZ; ++z) {
                this.chunks[x - this.chunkX][z - this.chunkZ] = world.getChunk(x, z, ChunkStatus.FULL, true);
            }
        }

        for (int x = pos1.getX() >> 4; x <= pos2.getX() >> 4; ++x) {
            for (int z = pos1.getZ() >> 4; z <= pos2.getZ() >> 4; ++z) {
                ChunkAccess ichunk = this.chunks[x - this.chunkX][z - this.chunkZ];
                if (ichunk != null && !ichunk.isYSpaceEmpty(pos1.getY(), pos2.getY())) {
                    this.empty = false;
                    return;
                }
            }
        }
    }

    private ChunkAccess getChunk(BlockPos pos) {
        return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private ChunkAccess getChunk(int chunkX, int chunkZ) {
        int x = chunkX - this.chunkX;
        int z = chunkZ - this.chunkZ;
        if (x >= 0 && x < this.chunks.length && z >= 0 && z < this.chunks[x].length) {
            ChunkAccess ichunk = this.chunks[x][z];
            return (ichunk != null ? ichunk : new EmptyLevelChunk(this.world, new ChunkPos(chunkX, chunkZ), world.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS)));
        } else {
            return new EmptyLevelChunk(this.world, new ChunkPos(chunkX, chunkZ), world.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS));
        }
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.world.getWorldBorder();
    }

    @Override
    public BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ);
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity pEntity, AABB pCollisionBox) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        ChunkAccess ichunk = this.getChunk(pos);
        return ichunk.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (world.isOutsideBuildHeight(pos)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            ChunkAccess ichunk = this.getChunk(pos);
            return ichunk.getBlockState(pos);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        if (world.isOutsideBuildHeight(pos)) {
            return Fluids.EMPTY.defaultFluidState();
        } else {
            ChunkAccess ichunk = this.getChunk(pos);
            return ichunk.getFluidState(pos);
        }
    }

    @Override
    public int getHeight() {
        return world.getHeight();
    }

    @Override
    public int getMinBuildHeight() {
        return world.getMinBuildHeight();
    }
}
