package me.desht.pneumaticcraft.api.event;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

public class SemiblockEvent extends Event {
    private final World world;
    private final BlockPos pos;
    private final Direction dir;

    SemiblockEvent(World world, BlockPos pos, Direction dir) {
        this.world = world;
        this.pos = pos;
        this.dir = dir;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public static class PlaceEvent extends SemiblockEvent {
        public PlaceEvent(World world, BlockPos pos, Direction dir) {
            super(world, pos, dir);
        }
    }

    public static class BreakEvent extends SemiblockEvent {
        public BreakEvent(World world, BlockPos pos, Direction dir) {
            super(world, pos, dir);
        }
    }
}
