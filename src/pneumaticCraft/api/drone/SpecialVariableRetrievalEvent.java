package pneumaticCraft.api.drone;

import net.minecraft.entity.EntityCreature;
import net.minecraft.world.ChunkPosition;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * Fired when a Drones is trying to get a special coordinate, by accessing a variable with '$' prefix.
 * This event is posted on the MinecraftForge.EVENT_BUS.
 */
public class SpecialVariableRetrievalEvent extends Event{
    public final EntityCreature drone;
    /**
     * The special variable name, with the '$' stripped away.
     */
    public final String specialVarName;
    /**
     * The returning coordinate
     */
    public ChunkPosition coordinate;

    public SpecialVariableRetrievalEvent(EntityCreature drone, String specialVarName){
        this.drone = drone;
        this.specialVarName = specialVarName;
    }

}
