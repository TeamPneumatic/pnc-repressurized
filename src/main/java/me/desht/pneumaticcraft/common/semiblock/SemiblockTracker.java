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
import me.desht.pneumaticcraft.api.semiblock.SemiblockAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

/**
 * Server side tracker to find the semiblock entities at a given world and blockpos
 * (Note that one blockpos could have up to 7 semiblocks - one non-sided plus six sided semiblocks)
 */
public enum SemiblockTracker implements SemiblockAccess {
    INSTANCE;

    private final Map<Identifier, Map<BlockPos, SemiblockCollection>> semiblockMap = new HashMap<>();

    public static SemiblockTracker getInstance() {
        return INSTANCE;
    }

    @Override
    public ISemiBlock getSemiblock(Level level, BlockPos pos) {
        return getSemiblock(level, pos, null);
    }

    @Override
    public ISemiBlock getSemiblock(Level level, BlockPos pos, Direction direction) {
        if (!level.isLoaded(pos)) return null;

        Map<BlockPos, SemiblockCollection> map = semiblockMap.get(getKey(level));
        if (map == null) return null;
        SemiblockCollection sc = map.get(pos);
        return sc == null ? null : sc.get(level, direction);
    }

    @Override
    public Stream<ISemiBlock> getAllSemiblocks(Level world, BlockPos pos) {
        return getAllSemiblocks(world, pos, null);
    }

    @Override
    public Stream<ISemiBlock> getAllSemiblocks(Level level, BlockPos pos, Direction offsetDir) {
        if (!level.isLoaded(pos)) return Stream.empty();

        Map<BlockPos, SemiblockCollection> map = semiblockMap.computeIfAbsent(getKey(level), k -> new HashMap<>());
        if (map.isEmpty()) return Stream.empty();
        SemiblockCollection sc = map.get(pos);
        if (sc == null && offsetDir != null) {
            sc = map.get(pos.relative(offsetDir));
        }
        return sc == null ? Stream.empty() : sc.getAll(level);
    }

    /**
     * Clear any record of a semiblock at the given world/pos/face
     * @param level the world
     * @param pos the blockpos
     * @param direction the side of the block, or null for the block itself
     */
    public void clearSemiblock(Level level, BlockPos pos, Direction direction) {
        Map<BlockPos, SemiblockCollection> map = semiblockMap.computeIfAbsent(getKey(level), k -> new HashMap<>());
        SemiblockCollection sc = map.get(pos);
        if (sc != null) {
            sc.clear(direction);
            if (sc.isEmpty()) {
                map.remove(pos);
            }
        }
    }

    /**
     * Add a semiblock at the given world/pos
     *
     * @param level the world
     * @param pos the blockpos
     * @param entity the semiblock entity
     */
    public void putSemiblock(Level level, BlockPos pos, ISemiBlock entity) {
        Map<BlockPos, SemiblockCollection> map = semiblockMap.computeIfAbsent(getKey(level), k -> new HashMap<>());

        SemiblockCollection sc = map.get(pos);
        if (sc == null) {
            map.put(pos, new SemiblockCollection(entity));
        } else {
            sc.set(entity);
        }
    }

    /**
     * Retrieve all the semiblocks in the given area.
     * @param level the world
     * @param aabb a bounding box which contains all the wanted semiblocks
     * @return a stream of semiblock in the area
     */
    public Stream<ISemiBlock> getSemiblocksInArea(Level level, BoundingBox aabb) {
        Map<BlockPos, SemiblockCollection> map = semiblockMap.computeIfAbsent(getKey(level), k -> new HashMap<>());

        return map.entrySet().stream()
                .filter(e -> boxContainsBlockPos(aabb, e.getKey()))
                .flatMap(e -> e.getValue().getAll(level));
    }

    private boolean boxContainsBlockPos(BoundingBox aabb, BlockPos pos) {
        return pos.getX() >= aabb.minX() && pos.getX() <= aabb.maxX()
                && pos.getY() >= aabb.minY() && pos.getY() <= aabb.maxY()
                && pos.getZ() >= aabb.minZ() && pos.getZ() <= aabb.maxZ();
    }

    private Identifier getKey(Level world) {
        return world.dimension().identifier();
    }

    private static class SemiblockCollection {
        private final Map<CenterDirection,Integer> sides = new EnumMap<>(CenterDirection.class);

        SemiblockCollection(ISemiBlock e) {
            set(e);
        }

        boolean isEmpty() {
            return sides.isEmpty();
        }

        ISemiBlock get(Level level, Direction dir) {
            return getValidSemiblock(level, CenterDirection.of(dir));
        }

        void set(ISemiBlock semiBlock) {
            CenterDirection cDir = CenterDirection.of(IDirectionalSemiblock.getDirection(semiBlock));
            sides.put(cDir, semiBlock.getTrackingId());
        }

        void clear(Direction direction) {
            sides.remove(CenterDirection.of(direction));
        }

        Stream<ISemiBlock> getAll(Level level) {
            return Arrays.stream(CenterDirection.values())
                    .map(d -> getValidSemiblock(level, d))
                    .filter(Objects::nonNull);
        }

        ISemiBlock getValidSemiblock(Level level, CenterDirection cDir) {
            int id = sides.getOrDefault(cDir, 0);
            if (id != 0) {
                if (level.getEntity(id) instanceof ISemiBlock semiBlock && semiBlock.isValid()) {
                    return semiBlock;
                } else {
                    sides.remove(cDir);
                }
            }
            return null;
        }
    }

    private enum CenterDirection {
        CENTER,
        DOWN,
        UP,
        NORTH,
        SOUTH,
        WEST,
        EAST;

        public static CenterDirection of(@Nullable Direction dir) {
            if (dir == null) {
                return CENTER;
            }
            return switch (dir) {
                case DOWN -> DOWN;
                case UP -> UP;
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                case EAST -> EAST;
            };
        }
    }

    @EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            if (!event.getServer().isDedicatedServer()) {
                // this is needed for integrated server, to clear down map between runs
                getInstance().semiblockMap.values().forEach(Map::clear);
                getInstance().semiblockMap.clear();
            }
        }
    }
}
