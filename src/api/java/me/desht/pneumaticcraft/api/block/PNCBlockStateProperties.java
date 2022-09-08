package me.desht.pneumaticcraft.api.block;

import net.minecraft.world.level.block.state.properties.EnumProperty;

public class PNCBlockStateProperties {
    public static class PressureTubes {
        public static final EnumProperty<PressureTubeConnection> UP = EnumProperty.create("up", PressureTubeConnection.class);
        public static final EnumProperty<PressureTubeConnection> DOWN = EnumProperty.create("down", PressureTubeConnection.class);
        public static final EnumProperty<PressureTubeConnection> NORTH = EnumProperty.create("north", PressureTubeConnection.class);
        public static final EnumProperty<PressureTubeConnection> EAST = EnumProperty.create("east", PressureTubeConnection.class);
        public static final EnumProperty<PressureTubeConnection> SOUTH = EnumProperty.create("south", PressureTubeConnection.class);
        public static final EnumProperty<PressureTubeConnection> WEST = EnumProperty.create("west", PressureTubeConnection.class);
    }
}
