package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Prevent riding entity AI to execute, to prevent setting a Path in its entity PathNavigator, so the Drone's path does
 * not get overriden by the riding entity - see MobEntity#updateEntityActionState().
 *
 * @author Maarten
 */
public class EntityAINoAIWhenRidingDrone extends Goal {

    private final MobEntity entity;
    
    public EntityAINoAIWhenRidingDrone(MobEntity entity){
        this.entity = entity;
        setFlags(EnumSet.allOf(Flag.class)); //All bits to block all other AI's
    }
    
    @Override
    public boolean canUse(){
        return entity.getVehicle() instanceof EntityDrone;
    }
}
