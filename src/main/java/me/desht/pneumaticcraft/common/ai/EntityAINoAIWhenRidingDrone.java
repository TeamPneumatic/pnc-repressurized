/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

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
