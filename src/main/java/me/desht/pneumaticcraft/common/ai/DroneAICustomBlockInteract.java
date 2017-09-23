package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.drone.ICustomBlockInteract;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import net.minecraft.util.math.BlockPos;

public class DroneAICustomBlockInteract extends DroneAIImExBase {
    private final ICustomBlockInteract blockInteractor;

    public DroneAICustomBlockInteract(IDroneBase drone, ProgWidgetAreaItemBase widget,
                                      ICustomBlockInteract blockInteractor) {
        super(drone, widget);
        this.blockInteractor = blockInteractor;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        return blockInteractor.doInteract(pos, drone, this, false) && super.doBlockInteraction(pos, distToBlock);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        return blockInteractor.doInteract(pos, drone, this, true);
    }
}
