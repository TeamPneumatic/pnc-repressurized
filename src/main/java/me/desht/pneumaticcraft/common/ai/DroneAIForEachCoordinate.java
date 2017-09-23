package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetForEachCoordinate;
import net.minecraft.util.math.BlockPos;

public class DroneAIForEachCoordinate extends DroneAIBlockInteraction<ProgWidgetForEachCoordinate> {

    private BlockPos curCoord;

    public DroneAIForEachCoordinate(IDroneBase drone, ProgWidgetForEachCoordinate widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        if (widget.isValidPosition(pos)) {
            curCoord = pos;
            abort();
        }
        return false;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        return false;
    }

    public BlockPos getCurCoord() {
        return curCoord;
    }

    @Override
    protected void addEndingDebugEntry() {

    }
}
