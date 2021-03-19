package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerAssemblyController;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram.EnumMachine;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityAssemblyController extends TileEntityPneumaticBase
        implements IAssemblyMachine, IMinWorkingPressure, INamedContainerProvider {
    private static final int PROGRAM_SLOT = 0;
    private static final int INVENTORY_SIZE = 1;

    private final ItemStackHandler itemHandler = new BaseItemStackHandler(this, INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() instanceof ItemAssemblyProgram;
        }
    };
    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> itemHandler);

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

    public TileEntityAssemblyController() {
        super(ModTileEntities.ASSEMBLY_CONTROLLER.get(),  PneumaticValues.DANGER_PRESSURE_ASSEMBLY_CONTROLLER, PneumaticValues.MAX_PRESSURE_ASSEMBLY_CONTROLLER, PneumaticValues.VOLUME_ASSEMBLY_CONTROLLER, 4);
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return inventoryCap;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return itemHandler;
    }

    @Override
    public void tick() {
        ItemStack programStack = itemHandler.getStackInSlot(PROGRAM_SLOT);

        // curProgram must be available on the client, or we can't show program-problems in the GUI
        if (curProgram == null && !goingToHomePosition && programStack.getItem() instanceof ItemAssemblyProgram) {
            curProgram = ItemAssemblyProgram.getProgram(programStack);
        } else if (curProgram != null &&
                (programStack.isEmpty() || curProgram.getType() != ItemAssemblyProgram.getProgram(programStack).getType())) {
            curProgram = null;
            if (!getWorld().isRemote) goingToHomePosition = true;
        }

        if (!getWorld().isRemote) {
            setStatus("Standby");
            if (getPressure() >= PneumaticValues.MIN_PRESSURE_ASSEMBLY_CONTROLLER) {
                if (curProgram != null || goingToHomePosition) {
                    if (assemblySystem == null) {
                        assemblySystem = findAssemblySystem();
                    }
                    if ((!isMachineMissing || curProgram == null) && !isMachineDuplicate) {
                        boolean useAir;
                        if (curProgram != null) {
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
        super.tick();
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
        AssemblySystem assemblySystem = new AssemblySystem(getPos());
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
        findMachines(machineList, getPos(), max);
        return machineList;
    }

    private void findMachines(List<IAssemblyMachine> machineList, BlockPos pos, int max) {
        for (Direction dir : DirectionUtil.HORIZONTALS) {
            TileEntity te = getWorld().getTileEntity(pos.offset(dir));
            if (te instanceof IAssemblyMachine && !machineList.contains(te) && machineList.size() < max) {
                machineList.add((IAssemblyMachine) te);
                findMachines(machineList, te.getPos(), max);
            }
        }
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();

        invalidateAssemblySystem();
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side != Direction.UP;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        goingToHomePosition = tag.getBoolean("goingToHomePosition");
        displayedText = tag.getString("displayedText");
        itemHandler.deserializeNBT(tag.getCompound("Items"));
        if (!itemHandler.getStackInSlot(PROGRAM_SLOT).isEmpty()) {
            curProgram = ItemAssemblyProgram.getProgram(itemHandler.getStackInSlot(PROGRAM_SLOT));
            if (curProgram != null) curProgram.readFromNBT(tag);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putBoolean("goingToHomePosition", goingToHomePosition);
        tag.putString("displayedText", displayedText);
        if (curProgram != null) curProgram.writeToNBT(tag);
        tag.put("Items", itemHandler.serializeNBT());
        return tag;
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
        // nop - we *are the controller!
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerAssemblyController(i, playerInventory, getPos());
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
                    if (machine instanceof TileEntityAssemblyPlatform) {
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

        public TileEntityAssemblyIOUnit getImportUnit() {
            return (TileEntityAssemblyIOUnit) get(EnumMachine.IO_UNIT_IMPORT);
        }

        public TileEntityAssemblyIOUnit getExportUnit() {
            return (TileEntityAssemblyIOUnit) get(EnumMachine.IO_UNIT_EXPORT);
        }

        public TileEntityAssemblyPlatform getPlatform() {
            return (TileEntityAssemblyPlatform) get(EnumMachine.PLATFORM);
        }

        public TileEntityAssemblyLaser getLaser() {
            return (TileEntityAssemblyLaser) get(EnumMachine.LASER);
        }

        public TileEntityAssemblyDrill getDrill() {
            return (TileEntityAssemblyDrill) get(EnumMachine.DRILL);
        }

        EnumMachine checkForMissingMachine(EnumMachine[] requiredMachines) {
            return Arrays.stream(requiredMachines)
                    .filter(e -> !machines.containsKey(e))
                    .findFirst()
                    .orElse(null);
        }
    }

}
