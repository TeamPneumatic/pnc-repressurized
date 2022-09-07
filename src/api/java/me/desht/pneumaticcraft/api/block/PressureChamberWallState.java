package me.desht.pneumaticcraft.api.block;

import net.minecraft.util.StringRepresentable;

public enum PressureChamberWallState implements StringRepresentable {
    NONE("none"),
    CENTER("center"),
    XEDGE("xedge"),
    ZEDGE("zedge"),
    YEDGE("yedge"),
    XMIN_YMIN_ZMIN("xmin_ymin_zmin"),
    XMIN_YMIN_ZMAX("xmin_ymin_zmax"),
    XMIN_YMAX_ZMIN("xmin_ymax_zmin"),
    XMIN_YMAX_ZMAX("xmin_ymax_zmax");

    private final String name;

    PressureChamberWallState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
