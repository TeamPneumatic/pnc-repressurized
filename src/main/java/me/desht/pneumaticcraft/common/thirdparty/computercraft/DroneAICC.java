package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

class DroneAICC extends EntityAIBase {
    private final EntityDrone drone;
    private final ProgWidgetCC widget;
    private EntityAIBase curAction;
    private boolean curActionActive;
    private final TileEntityDroneInterface droneInterface;
    private boolean newAction;

    public DroneAICC(EntityDrone drone, ProgWidgetCC widget, boolean targetAI) {
        this.drone = drone;
        this.widget = widget;
        Set<BlockPos> area = widget.getInterfaceArea();
        for (BlockPos pos : area) {
            TileEntity te = drone.world.getTileEntity(pos);
            if (te instanceof TileEntityDroneInterface) {
                TileEntityDroneInterface inter = (TileEntityDroneInterface) te;
                if (targetAI) {
                    if (inter.getDrone() == drone) {
                        droneInterface = inter;
                        return;
                    }
                } else {
                    if (inter.getDrone() == null) {
                        droneInterface = inter;
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
        return droneInterface != null && !droneInterface.isInvalid() && droneInterface.getDrone() == drone;
    }

    @Override
    public synchronized boolean shouldContinueExecuting() {
        if (!newAction && curActionActive && curAction != null) {
            boolean contin = curAction.shouldContinueExecuting();
            if (!contin) curAction.resetTask();
            return contin;
        } else {
            return false;
        }
    }

    @Override
    public synchronized void updateTask() {
        if (curActionActive && curAction != null) curAction.updateTask();
    }

    public synchronized void setAction(IProgWidget widget, EntityAIBase ai) throws IllegalArgumentException {
        curAction = ai;
        newAction = true;
        curActionActive = true;
    }

    public synchronized void abortAction() {
        curAction = null;
    }

    public synchronized boolean isActionDone() throws Exception {
        if (curAction == null) throw new IllegalStateException("There's no action active!");
        return !curActionActive;
    }
}
