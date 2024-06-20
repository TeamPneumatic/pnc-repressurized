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

package me.desht.pneumaticcraft.common.drone.ai;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.common.block.entity.drone.ProgrammerBlockEntity;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetExternalProgram;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncDroneProgWidgets;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DroneAIExternalProgram extends DroneAIBlockInteraction<ProgWidgetExternalProgram> {
    private final DroneAIManager subAI, mainAI;
    private final Set<BlockPos> traversedPositions = new HashSet<>();
    private int curSlot;
    private int curProgramHash; //Used to see if changes have been made to the program while running it.

    public DroneAIExternalProgram(IDrone drone, DroneAIManager mainAI, ProgWidgetExternalProgram widget) {
        super(drone, widget);
        this.mainAI = mainAI;
        subAI = new DroneAIManager(drone, new ArrayList<>());
    }

    @Override
    public boolean canUse() {
        if (super.canUse()) {
            traversedPositions.clear();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean moveToPositions() {
        return false;
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        if (traversedPositions.add(pos)) {
            curSlot = 0;
            BlockEntity te = drone.getDroneLevel().getBlockEntity(pos);
            return te != null && IOHelper.getInventoryForBlock(te).isPresent();
        }
        return false;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double squareDistToBlock) {
        return IOHelper.getInventoryForBlock(drone.getDroneLevel().getBlockEntity(pos)).map(this::handleInv).orElse(false);
    }

    private boolean handleInv(IItemHandler inv) {
        if (curProgramHash != 0) {
            if (curSlot < inv.getSlots()) {
                ItemStack stack = inv.getStackInSlot(curSlot);
                if (curProgramHash == ItemStack.hashItemAndComponents(stack)) {
                    subAI.onUpdateTasks();
                    if (subAI.isIdling() || isRunningSameProgram(subAI.getCurrentGoal())) {
                        curProgramHash = 0;
                        curSlot++;
                    }
                } else {
                    curProgramHash = 0;
                    subAI.setWidgets(new ArrayList<>());
                    if (drone instanceof IDroneBase base) {  // should always be the case...
                        drone.getDebugger().getDebuggingPlayers().forEach(p ->
                                NetworkHandler.sendToPlayer(PacketSyncDroneProgWidgets.create(base), p));
                    }
                }
            }
            return true;
        } else {
            while (curSlot < inv.getSlots()) {
                ItemStack stack = inv.getStackInSlot(curSlot);
                if (stack.getItem() instanceof IProgrammable programmable) {
                    if (programmable.canProgram(stack) && programmable.usesPieces(stack)) {
                        List<IProgWidget> widgets = ProgrammerBlockEntity.getProgWidgets(stack);
                        ProgrammerBlockEntity.updatePuzzleConnections(widgets);
                        boolean areWidgetsValid = widgets.stream().allMatch(widget -> drone.isProgramApplicable(widget.getType()));
                        if (areWidgetsValid) {
                            if (progWidget.shareVariables) mainAI.connectVariables(subAI);
                            IDroneBase droneBase = IDroneBase.asDroneBase(subAI.getDrone());
                            droneBase.getAIManager().setLabel("Main");
                            subAI.setWidgets(widgets);
                            drone.getDebugger().getDebuggingPlayers().forEach(p -> NetworkHandler.sendToPlayer(PacketSyncDroneProgWidgets.create(droneBase), p));
                            curProgramHash = ItemStack.hashItemAndComponents(stack);
                            if (!subAI.isIdling()) {
                                return true;
                            }
                        }
                    }
                }
                curSlot++;
            }
            abort();
            return false;
        }
    }

    // Prevent a memory leak, as a result of the same External program recursively calling itself.
    private boolean isRunningSameProgram(Goal ai) {
        return ai instanceof DroneAIExternalProgram ext && this.curProgramHash == ext.curProgramHash;
    }

    public DroneAIManager getRunningAI() {
        return subAI;
    }
}
