package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Server side tracker to find the semiblock entities at a given world & blockpos
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
    public static void onServerStopping(FMLServerStoppingEvent event) {
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
    public ISemiBlock getSemiblock(World world, BlockPos pos) {
        return getSemiblock(world, pos, null);
    }

    /**
     * Retrieve the semiblock at the given world/pos/face
     * @param world the world
     * @param pos the blockpos
     * @param direction face of the blockpos, or null for the block itself
     * @return the entity, or null if none was found
     */
    public ISemiBlock getSemiblock(World world, BlockPos pos, Direction direction) {
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
    public Stream<ISemiBlock> getAllSemiblocks(World world, BlockPos pos) {
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
    public Stream<ISemiBlock> getAllSemiblocks(World world, BlockPos pos, Direction offsetDir) {
        Map<BlockPos, SemiblockCollection> map = semiblockMap.get(getKey(world));
        SemiblockCollection sc = map.get(pos);
        if (sc == null && offsetDir != null) sc = map.get(pos.offset(offsetDir));
        return sc == null ? Stream.empty() : sc.getAll();
    }

    /**
     * Clear any record of a semiblock at the given world/pos/face
     * @param world the world
     * @param pos the blockpos
     * @param direction the side of the block, or null for the block itself
     */
    public void clearSemiblock(World world, BlockPos pos, Direction direction) {
        Map<BlockPos, SemiblockCollection> map = semiblockMap.get(getKey(world));
        if (map != null) {
            SemiblockCollection sc = map.get(pos);
            if (sc != null) sc.clear(direction);
        }
    }

    /**
     * Add a semiblock at the given world & pos
     *
     * @param world the world
     * @param pos the blockpos
     * @param entity the semiblock entity
     * @return true if it was added OK, false if there was already a semiblock there (which is an error)
     */
    public boolean putSemiblock(World world, BlockPos pos, ISemiBlock entity) {
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
    public Stream<ISemiBlock> getSemiblocksInArea(World world, AxisAlignedBB aabb) {
        Map<BlockPos, SemiblockCollection> map = semiblockMap.get(getKey(world));

        return map.entrySet().stream()
                .filter(e -> aabb.contains(e.getKey().getX(), e.getKey().getY(), e.getKey().getZ()))
                .flatMap(e -> e.getValue().getAll());
    }

    private ResourceLocation getKey(World world) {
        return world.getDimension().getType().getRegistryName();
    }

    private class SemiblockCollection {
        private WeakReference<ISemiBlock> center = new WeakReference<>(null);
        private final List<WeakReference<ISemiBlock>> sides = new ArrayList<>();

        SemiblockCollection(ISemiBlock e) {
            set(e);
        }

        public ISemiBlock get(Direction direction) {
            if (direction == null) return center.get();
            return sides.isEmpty() ? null : sides.get(direction.getIndex()).get();
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
                if (sides.get(dir.getIndex()).get() != null) return false;
                sides.set(dir.getIndex(), new WeakReference<>(semiBlock));
            }
            return true;
        }

        public void clear(Direction direction) {
            if (direction == null) {
                if (center != null) center.clear();
            } else {
                if (!sides.isEmpty()) sides.get(direction.getIndex()).clear();
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
