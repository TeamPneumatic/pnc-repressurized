package me.desht.pneumaticcraft.api.event;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

public class SemiblockEvent extends Event {
    private final World world;
    private final BlockPos pos;

    SemiblockEvent(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public static class PlaceEvent extends SemiblockEvent {
        public PlaceEvent(World world, BlockPos pos) {
            super(world, pos);
        }
    }

    public static class BreakEvent extends SemiblockEvent {
        public BreakEvent(World world, BlockPos pos) {
            super(world, pos);
        }
    }
}
