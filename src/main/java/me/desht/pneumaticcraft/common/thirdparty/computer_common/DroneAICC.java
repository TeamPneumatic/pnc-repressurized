package me.desht.pneumaticcraft.common.thirdparty.computer_common;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

class DroneAICC extends Goal {
    private final EntityDrone drone;
    private final ProgWidgetCC widget;
    private Goal curAction;
    private boolean curActionActive;
    private final TileEntityDroneInterface droneInterface;
    private boolean newAction;

    DroneAICC(EntityDrone drone, ProgWidgetCC widget, boolean targetAI) {
        this.drone = drone;
        this.widget = widget;
        Set<BlockPos> area = widget.getInterfaceArea();
        for (BlockPos pos : area) {
            TileEntity te = drone.world.getTileEntity(pos);
            if (te instanceof TileEntityDroneInterface) {
                TileEntityDroneInterface interfaceTE = (TileEntityDroneInterface) te;
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
    public synchronized boolean shouldExecute() {
        newAction = false;
        if (curAction != null) {
            curActionActive = curAction.shouldExecute();
            if (curActionActive) curAction.startExecuting();
        }
        return droneInterface != null && !droneInterface.isRemoved() && droneInterface.getDrone() == drone;
    }

    @Override
    public synchronized boolean shouldContinueExecuting() {
        if (!newAction && curActionActive && curAction != null) {
            boolean shouldContinue = curAction.shouldContinueExecuting();
            if (!shouldContinue) curAction.resetTask();
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
