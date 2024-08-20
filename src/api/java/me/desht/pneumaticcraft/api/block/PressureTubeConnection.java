package me.desht.pneumaticcraft.api.block;

import net.minecraft.util.StringRepresentable;

/**
 * Tri-state representing the 3 possible states for a tube connection.
 */
public enum PressureTubeConnection implements StringRepresentable {
    /**
     * Unconnected, but available
     */
    OPEN(0, "open"),
    /**
     * Connected to a neighbor
     */
    CONNECTED(1, "connected"),
    /**
     * Wrenched closed, and unavailable
     */
    CLOSED(2, "closed");

    private final int index;
    private final String name;

    PressureTubeConnection(int index, String name) {
        this.index = index;
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public static short packConnections(PressureTubeConnection[] connections) {
        assert connections.length == 6;

        short res = 0;
        for (int i = 0; i < connections.length; i++) {
            res |= (short) ((connections[i] == CONNECTED ? 1 : 0) << i * 2);
            res |= (short) ((connections[i] == CLOSED ? 1 : 0) << i * 2 + 1);
        }
        return res;
    }
}
