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

package me.desht.pneumaticcraft.common.thirdparty.computer_common;

import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Set;

class DroneAICC extends Goal {
    private final DroneEntity drone;
    private final ProgWidgetCC widget;
    private Goal curAction;
    private boolean curActionActive;
    private final DroneInterfaceBlockEntity droneInterface;
    private boolean newAction;

    DroneAICC(DroneEntity drone, ProgWidgetCC widget, boolean targetAI) {
        this.drone = drone;
        this.widget = widget;
        Set<BlockPos> area = widget.getInterfaceArea();
        for (BlockPos pos : area) {
            BlockEntity te = drone.level().getBlockEntity(pos);
            if (te instanceof DroneInterfaceBlockEntity interfaceTE) {
                if (targetAI) {
                    if (interfaceTE.getDrone() == drone) {
                        droneInterface = interfaceTE;
                        return;
                    }
                } else {
                    if (interfaceTE.getDrone() == null) {
                        droneInterface = interfaceTE;
                        droneInterface.setDrone(drone);
                        return;
                    }
                }
            }
        }
        droneInterface = null;
    }

    public ProgWidgetCC getWidget() {
        return widget;
    }

    @Override
    public synchronized boolean canUse() {
        newAction = false;
        if (curAction != null) {
            curActionActive = curAction.canUse();
            if (curActionActive) curAction.start();
        }
        return droneInterface != null && !droneInterface.isRemoved() && droneInterface.getDrone() == drone;
    }

    @Override
    public synchronized boolean canContinueToUse() {
        if (!newAction && curActionActive && curAction != null) {
            boolean shouldContinue = curAction.canContinueToUse();
            if (!shouldContinue) curAction.stop();
            return shouldContinue;
        } else {
            return false;
        }
    }

    @Override
    public synchronized void tick() {
        if (curActionActive && curAction != null) curAction.tick();
    }

    synchronized void setAction(IProgWidget widget, Goal ai) throws IllegalArgumentException {
        curAction = ai;
        newAction = true;
        curActionActive = true;
    }

    synchronized void abortAction() {
        curAction = null;
    }

    synchronized boolean isActionDone() {
        if (curAction == null) throw new IllegalStateException("There's no action active!");
        return !curActionActive;
    }
}
