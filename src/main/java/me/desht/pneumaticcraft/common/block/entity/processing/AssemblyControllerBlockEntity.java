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

package me.desht.pneumaticcraft.common.block.entity.processing;

import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.block.entity.AbstractAirHandlingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IMinWorkingPressure;
import me.desht.pneumaticcraft.common.block.entity.IResettable;
import me.desht.pneumaticcraft.common.inventory.AssemblyControllerMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.AssemblyProgramItem;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram.EnumMachine;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.*;

public class AssemblyControllerBlockEntity extends AbstractAirHandlingBlockEntity
        implements IAssemblyMachine, IMinWorkingPressure, MenuProvider {
    private static final int PROGRAM_SLOT = 0;
    private static final int INVENTORY_SIZE = 1;

    private final ItemStackHandler itemHandler = new BaseItemStackHandler(this, INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() instanceof AssemblyProgramItem;
        }
    };

    public AssemblyProgram curProgram;
    @GuiSynced
    public boolean isMachineMissing;
    @GuiSynced
    public boolean isMachineDuplicate;
    @GuiSynced
    public EnumMachine missingMachine;
    @GuiSynced
    public EnumMachine duplicateMachine;
    private boolean goingToHomePosition;
    @DescSynced
    public String displayedText = "";
    @DescSynced
    public boolean hasProblem;
    private AssemblySystem assemblySystem = null;

    public AssemblyControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.ASSEMBLY_CONTROLLER.get(),  pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_ASSEMBLY_CONTROLLER, 4);
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return itemHandler;
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        ItemStack programStack = itemHandler.getStackInSlot(PROGRAM_SLOT);

        // curProgram must be available on the client, or we can't show program-problems in the GUI
        AssemblyProgram newProgram = AssemblyProgramItem.getProgram(programStack);
        if (curProgram == null && !goingToHomePosition && newProgram != null) {
            curProgram = newProgram;
        } else if (curProgram != null && newProgram == null) {
            curProgram = null;
            if (!nonNullLevel().isClientSide) goingToHomePosition = true;
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        setStatus("Standby");
        if (getPressure() >= PneumaticValues.MIN_PRESSURE_ASSEMBLY_CONTROLLER) {
            if (curProgram != null || goingToHomePosition) {
                if (assemblySystem == null) {
                    assemblySystem = findAssemblySystem();
                }
                if ((!isMachineMissing || curProgram == null) && !isMachineDuplicate) {
                    boolean useAir;
                    if (curProgram != null) {
                        if (!curProgram.validateBlockEntity(assemblySystem)) {
                            // just in case a machine we think should be there, isn't
                            invalidateAssemblySystem();
                            return;
                        }
                        useAir = curProgram.executeStep(assemblySystem);
                        if (useAir) {
                            setStatus("Running...");
                        }
                    } else {
                        useAir = true;
                        boolean resetDone = assemblySystem.reset();
                        goingToHomePosition = isMachineMissing || !resetDone;
                        setStatus("Resetting...");
                    }
                    if (useAir) {
                        addAir(-(int) (PneumaticValues.USAGE_ASSEMBLING * getSpeedUsageMultiplierFromUpgrades()));
                    }
                    assemblySystem.setSpeed(getSpeedMultiplierFromUpgrades());
                }
            }
        }
        hasProblem = isMachineMissing
                || isMachineDuplicate
                || getPressure() < PneumaticValues.MIN_PRESSURE_ASSEMBLY_CONTROLLER
                || curProgram == null
                || curProgram.curProblem != AssemblyProgram.EnumAssemblyProblem.NO_PROBLEM;
    }

    /**
     * Force the controller to rediscover its machines on the next tick.
     */
    void invalidateAssemblySystem() {
        assemblySystem = null;
    }

    private AssemblySystem findAssemblySystem() {
        EnumMachine[] requiredMachines = curProgram != null ? curProgram.getRequiredMachines() : EnumMachine.values();

        duplicateMachine = null;
        AssemblySystem assemblySystem = new AssemblySystem(getBlockPos());
        for (IAssemblyMachine machine : findMachines(requiredMachines.length * 2)) {  // *2 ensures duplicates are noticed
            if (!assemblySystem.addMachine(machine)) {
                duplicateMachine = machine.getAssemblyType();
            }
        }
        missingMachine = assemblySystem.checkForMissingMachine(requiredMachines);

        isMachineDuplicate = duplicateMachine != null;
        isMachineMissing = missingMachine != null;

        return assemblySystem;
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    private void setStatus(String text) {
        displayedText = text;
    }

    public List<IAssemblyMachine> findMachines(int max) {
        List<IAssemblyMachine> machineList = new ArrayList<>();
        findMachines(machineList, getBlockPos(), max);
        return machineList;
    }

    private void findMachines(List<IAssemblyMachine> machineList, BlockPos pos, int max) {
        for (Direction dir : DirectionUtil.HORIZONTALS) {
            BlockEntity te = nonNullLevel().getBlockEntity(pos.relative(dir));
            if (te instanceof IAssemblyMachine && !machineList.contains(te) && machineList.size() < max) {
                machineList.add((IAssemblyMachine) te);
                findMachines(machineList, te.getBlockPos(), max);
            }
        }
    }

    @Override
    public void onNeighborBlockUpdate(BlockPos fromPos) {
        super.onNeighborBlockUpdate(fromPos);

        invalidateAssemblySystem();
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side != Direction.UP;
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        goingToHomePosition = tag.getBoolean("goingToHomePosition");
        displayedText = tag.getString("displayedText");
        itemHandler.deserializeNBT(provider, tag.getCompound("Items"));
        if (!itemHandler.getStackInSlot(PROGRAM_SLOT).isEmpty()) {
            curProgram = AssemblyProgramItem.getProgram(itemHandler.getStackInSlot(PROGRAM_SLOT));
            if (curProgram != null) curProgram.readFromNBT(tag);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("goingToHomePosition", goingToHomePosition);
        tag.putString("displayedText", displayedText);
        if (curProgram != null) curProgram.writeToNBT(tag);
        tag.put("Items", itemHandler.serializeNBT(provider));
    }

    @Override
    public boolean isIdle() {
        return true;
    }

    @Override
    public void setSpeed(float speed) {
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_ASSEMBLY_CONTROLLER;
    }

    @Override
    public EnumMachine getAssemblyType() {
        return EnumMachine.CONTROLLER;
    }

    @Override
    public void setControllerPos(BlockPos controllerPos) {
        // no-op - we *are* the controller!
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new AssemblyControllerMenu(i, playerInventory, getBlockPos());
    }

    public static class AssemblySystem {
        private final EnumMap<EnumMachine,IAssemblyMachine> machines = new EnumMap<>(EnumMachine.class);
        private final BlockPos controllerPos;

        public AssemblySystem(BlockPos controllerPos) {
            this.controllerPos = controllerPos;
        }

        private IAssemblyMachine get(EnumMachine machine) {
            return machines.get(machine);
        }

        boolean addMachine(IAssemblyMachine machine) {
            if (machines.containsKey(machine.getAssemblyType())) {
                return false;  // already present
            }
            machines.put(machine.getAssemblyType(), machine);
            machine.setControllerPos(controllerPos);
            return true;
        }

        boolean reset() {
            boolean resetDone = true;
            for (IAssemblyMachine machine : machines.values()) {
                if (machine instanceof IResettable && !((IResettable) machine).reset()) {
                    resetDone = false;
                    if (machine instanceof AssemblyPlatformBlockEntity) {
                        getExportUnit().pickupItem(null);
                    }
                    break;
                }
            }
            return resetDone;
        }

        void setSpeed(float speedMult) {
            machines.values().stream()
                    .filter(Objects::nonNull)
                    .forEach(te -> te.setSpeed(speedMult));
        }

        public AssemblyIOUnitBlockEntity getImportUnit() {
            return (AssemblyIOUnitBlockEntity) get(EnumMachine.IO_UNIT_IMPORT);
        }

        public AssemblyIOUnitBlockEntity getExportUnit() {
            return (AssemblyIOUnitBlockEntity) get(EnumMachine.IO_UNIT_EXPORT);
        }

        public AssemblyPlatformBlockEntity getPlatform() {
            return (AssemblyPlatformBlockEntity) get(EnumMachine.PLATFORM);
        }

        public AssemblyLaserBlockEntity getLaser() {
            return (AssemblyLaserBlockEntity) get(EnumMachine.LASER);
        }

        public AssemblyDrillBlockEntity getDrill() {
            return (AssemblyDrillBlockEntity) get(EnumMachine.DRILL);
        }

        EnumMachine checkForMissingMachine(EnumMachine[] requiredMachines) {
            return Arrays.stream(requiredMachines)
                    .filter(e -> !machines.containsKey(e))
                    .findFirst()
                    .orElse(null);
        }
    }

}
