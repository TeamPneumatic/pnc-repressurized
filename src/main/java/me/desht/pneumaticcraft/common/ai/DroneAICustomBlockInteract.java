package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.drone.ICustomBlockInteract;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetInventoryBase;
import net.minecraft.util.math.BlockPos;

public class DroneAICustomBlockInteract extends DroneAIImExBase<ProgWidgetInventoryBase> {
    private final ICustomBlockInteract blockInteractor;

    public DroneAICustomBlockInteract(IDroneBase drone, ProgWidgetInventoryBase widget,
                                      ICustomBlockInteract blockInteractor) {
        super(drone, widget);
        this.blockInteractor = blockInteractor;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return blockInteractor.doInteract(pos, drone, this, false) && super.doBlockInteraction(pos, squareDistToBlock);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return blockInteractor.doInteract(pos, drone, this, true);
    }
}
