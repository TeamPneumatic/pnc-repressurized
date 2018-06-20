package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

/**
 * Prevent riding entity AI to execute, to prevent setting a Path in its entity PathNavigator, so the Drone's path does not get overriden
 * by the riding entity ({@link net.minecraft.entity.EntityLiving#updateEntityActionState()}).
 * @author Maarten
 *
 */
public class EntityAINoAIWhenRidingDrone extends EntityAIBase{

    private final EntityLiving entity;
    
    public EntityAINoAIWhenRidingDrone(EntityLiving entity){
        this.entity = entity;
        setMutexBits(Integer.MAX_VALUE); //All bits to block all other AI's
    }
    
    @Override
    public boolean shouldExecute(){
        return entity.getRidingEntity() instanceof EntityDrone;
    }
}
