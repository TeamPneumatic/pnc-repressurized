package pneumaticCraft.common.ai;

import net.minecraft.world.ChunkPosition;
import pneumaticCraft.api.drone.ICustomBlockInteract;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;

public class DroneAICustomBlockInteract extends DroneAIImExBase{
    private final ICustomBlockInteract blockInteractor;

    public DroneAICustomBlockInteract(EntityDrone drone, double speed, ProgWidgetAreaItemBase widget,
            ICustomBlockInteract blockInteractor){
        super(drone, speed, widget);
        this.blockInteractor = blockInteractor;
    }

    @Override
    protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
        return blockInteractor.doInteract(pos, drone, this, false) && super.doBlockInteraction(pos, distToBlock);
    }

    @Override
    protected boolean isValidPosition(ChunkPosition pos){
        return blockInteractor.doInteract(pos, drone, this, true);
    }
}
