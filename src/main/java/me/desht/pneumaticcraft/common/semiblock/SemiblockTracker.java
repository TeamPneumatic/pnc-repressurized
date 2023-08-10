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

package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.semiblock.IDirectionalSemiblock;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Server side tracker to find the semiblock entities at a given world and blockpos
 * (Note that one blockpos could have up to 7 semiblocks - one non-sided plus six sided semiblocks)
 */
@Mod.EventBusSubscriber(modid = Names.MOD_ID)
public enum SemiblockTracker {
    INSTANCE;

    private static final Map<ResourceLocation, Map<BlockPos, SemiblockCollection>> semiblockMap = new HashMap<>();

    public static SemiblockTracker getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (!event.getServer().isDedicatedServer()) {
            // this is needed for integrated server, otherwise there will be "already exists" errors when starting up again
            semiblockMap.values().forEach(Map::clear);
            semiblockMap.clear();
        }
    }

    /**
     * Retrieve the semiblock entity at the given world/pos
     * @param world the world
     * @param pos the block
     * @return the entity at the given pos
     */
    public ISemiBlock getSemiblock(Level world, BlockPos pos) {
        return getSemiblock(world, pos, null);
    }

    /**
     * Retrieve the semiblock at the given world/pos/face
     * @param world the world
     * @param pos the blockpos
     * @param direction face of the blockpos, or null for the block itself
     * @return the entity, or null if none was found, or the blockpos in question isn't loaded
     */
    public ISemiBlock getSemiblock(Level world, BlockPos pos, Direction direction) {
        if (!world.isLoaded(pos)) return null;

        Map<BlockPos, SemiblockCollection> map = semiblockMap.get(getKey(world));
        if (map == null) return null;
        SemiblockCollection sc = map.get(pos);
        return sc == null ? null : sc.get(direction);
    }

    /**
     * Retrieve all the semiblocks at the given position.
     * @param world the world
     * @param pos the blockpos
     * @return a collection of all the semiblocks at the given position
     */
    public Stream<ISemiBlock> getAllSemiblocks(Level world, BlockPos pos) {
        return getAllSemiblocks(world, pos, null);
    }

    /**
     * Retrieve all the semiblocks at the given position. If there's nothing at the given position, try the position
     * offset by one block in the given the direction.
     * @param world the world
     * @param pos the blockpos
     * @param offsetDir a direction to offset if needed
     * @return a stream of all the semiblocks at the given position
     */
    public Stream<ISemiBlock> getAllSemiblocks(Level world, BlockPos pos, Direction offsetDir) {
        if (!world.isLoaded(pos)) return Stream.empty();

        Map<BlockPos, SemiblockCollection> map = semiblockMap.computeIfAbsent(getKey(world), k -> new HashMap<>());
        if (map.isEmpty()) return Stream.empty();
        SemiblockCollection sc = map.get(pos);
        if (sc == null && offsetDir != null) sc = map.get(pos.relative(offsetDir));
        return sc == null ? Stream.empty() : sc.getAll();
    }

    /**
     * Clear any record of a semiblock at the given world/pos/face
     * @param world the world
     * @param pos the blockpos
     * @param direction the side of the block, or null for the block itself
     */
    public void clearSemiblock(Level world, BlockPos pos, Direction direction) {
        Map<BlockPos, SemiblockCollection> map = semiblockMap.computeIfAbsent(getKey(world), k -> new HashMap<>());
        SemiblockCollection sc = map.get(pos);
        if (sc != null) sc.clear(direction);
    }

    /**
     * Add a semiblock at the given world/pos
     *
     * @param world the world
     * @param pos the blockpos
     * @param entity the semiblock entity
     * @return true if it was added OK, false if there was already a semiblock there (which is an error)
     */
    public boolean putSemiblock(Level world, BlockPos pos, ISemiBlock entity) {
        Map<BlockPos, SemiblockCollection> map = semiblockMap.computeIfAbsent(getKey(world), k -> new HashMap<>());

        SemiblockCollection sc = map.get(pos);
        if (sc == null) {
            map.put(pos, new SemiblockCollection(entity));
            return true;
        } else {
            return sc.set(entity);
        }
    }

    /**
     * Retrieve all the semiblocks in the given area.
     * @param world the world
     * @param aabb a bounding box which contains all the wanted semiblocks
     * @return a stream of semiblock in the area
     */
    public Stream<ISemiBlock> getSemiblocksInArea(Level world, AABB aabb) {
        Map<BlockPos, SemiblockCollection> map = semiblockMap.computeIfAbsent(getKey(world), k -> new HashMap<>());

        return map.entrySet().stream()
                .filter(e -> aabbContainsBlockPos(aabb, e.getKey()))
                .flatMap(e -> e.getValue().getAll());
    }

    private boolean aabbContainsBlockPos(AABB aabb, BlockPos pos) {
        // like AABB#contains() but works with blockpos instead of vec3
        // and works for AABB's with min == max
        return pos.getX() >= aabb.minX && pos.getX() <= aabb.maxX
                && pos.getY() >= aabb.minY && pos.getY() <= aabb.maxY
                && pos.getZ() >= aabb.minZ && pos.getZ() <= aabb.maxZ;
    }

    private ResourceLocation getKey(Level world) {
        return world.dimension().location();
    }

    private static class SemiblockCollection {
        private WeakReference<ISemiBlock> center = new WeakReference<>(null);
        private final List<WeakReference<ISemiBlock>> sides = new ArrayList<>();

        SemiblockCollection(ISemiBlock e) {
            set(e);
        }

        public ISemiBlock get(Direction direction) {
            if (direction == null) return center.get();
            return sides.isEmpty() ? null : sides.get(direction.get3DDataValue()).get();
        }

        boolean set(ISemiBlock semiBlock) {
            Direction dir = semiBlock instanceof IDirectionalSemiblock ? ((IDirectionalSemiblock) semiBlock).getSide() : null;
            if (dir == null) {
                if (center.get() != null) return false;
                center = new WeakReference<>(semiBlock);
            } else {
                if (sides.isEmpty()) {
                    for (int i = 0; i < 6; i++) {
                        sides.add(new WeakReference<>(null));
                    }
                }
                if (sides.get(dir.get3DDataValue()).get() != null) return false;
                sides.set(dir.get3DDataValue(), new WeakReference<>(semiBlock));
            }
            return true;
        }

        public void clear(Direction direction) {
            if (direction == null) {
                if (center != null) center.clear();
            } else {
                if (!sides.isEmpty()) sides.get(direction.get3DDataValue()).clear();
            }
        }

        Stream<ISemiBlock> getAll() {
            Stream<ISemiBlock> s1 = center.get() == null  ? Stream.empty() : Stream.of(center.get());
            Stream<ISemiBlock> s2 = sides.stream()
                    .filter(ref -> ref.get() != null)
                    .map(Reference::get);
            return Stream.concat(s1, s2);
        }
    }
}
