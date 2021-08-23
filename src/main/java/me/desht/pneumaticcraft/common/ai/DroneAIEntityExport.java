package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class DroneAIEntityExport<W extends ProgWidgetAreaItemBase> extends DroneAIBlockInteraction<W> {
    public DroneAIEntityExport(IDroneBase drone, W progWidget) {
        super(drone, progWidget);
    }

    @Override
    public boolean canUse() {
        if (drone.getCarryingEntities().isEmpty()) return false;
        for (Entity e : drone.getCarryingEntities()) {
            if (!progWidget.isEntityValid(e)) return false;
        }
        return super.canUse();
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return true;
    }

    @Override
    protected boolean moveIntoBlock() {
        return true;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        drone.setCarryingEntity(null);
        return false;
    }
}
