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

import me.desht.pneumaticcraft.common.progwidgets.IItemFiltering;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

public class DroneAIVoidItem extends Goal {
    private final IDroneBase drone;
    private final IItemFiltering widget;

    public DroneAIVoidItem(IDroneBase drone, IItemFiltering widget) {
        this.drone = drone;
        this.widget = widget;
    }

    @Override
    public boolean canUse() {
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            ItemStack stack = drone.getInv().getStackInSlot(i);
            if (!stack.isEmpty() && widget.isItemValidForFilters(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        for (int i = 0; i < drone.getInv().getSlots(); i++) {
            ItemStack stack = drone.getInv().getStackInSlot(i);
            if (!stack.isEmpty() && widget.isItemValidForFilters(stack)) {
                drone.getInv().setStackInSlot(i, ItemStack.EMPTY);
                drone.addAirToDrone(-PneumaticValues.DRONE_USAGE_VOID * stack.getCount());
                if (drone.world() instanceof ServerLevel) {
                    Vec3 vec = drone.getDronePos();
                    ((ServerLevel)drone.world()).sendParticles(ParticleTypes.LAVA, vec.x, vec.y, vec.z, 5, 0, 0, 0, 0);
                }
            }
        }
    }
}
