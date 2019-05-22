package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.recipes.programs.AssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.programs.AssemblyProgram.EnumMachine;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TileEntityAssemblyController extends TileEntityPneumaticBase implements IAssemblyMachine, IMinWorkingPressure {
    private static final int PROGRAM_INVENTORY_INDEX = 0;
    private static final int INVENTORY_SIZE = 1;

    private final ItemStackHandler inventory = new BaseItemStackHandler(this, INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() == Itemss.ASSEMBLY_PROGRAM;
        }
    };
    @DescSynced
    public final boolean[] sidesConnected = new boolean[6];
    private AssemblyProgram curProgram;
    @GuiSynced
    private boolean isMachineMissing;
    @GuiSynced
    private boolean isMachineDuplicate;
    @GuiSynced
    private EnumMachine missingMachine;
    @GuiSynced
    private EnumMachine duplicateMachine;
    private boolean goingToHomePosition;
    @DescSynced
    public String displayedText = "";
    @DescSynced
    public boolean hasProblem;
    private AssemblySystem assemblySystem = null;

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    public TileEntityAssemblyController() {
        super(PneumaticValues.DANGER_PRESSURE_ASSEMBLY_CONTROLLER, PneumaticValues.MAX_PRESSURE_ASSEMBLY_CONTROLLER, PneumaticValues.VOLUME_ASSEMBLY_CONTROLLER, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
    }

    @Override
    public void update() {
        ItemStack programStack = inventory.getStackInSlot(PROGRAM_INVENTORY_INDEX);

        // curProgram must be available on the client, or we can't show program-problems in the GUI
        if (curProgram == null && !goingToHomePosition && programStack.getItem() == Itemss.ASSEMBLY_PROGRAM) {
            curProgram = ItemAssemblyProgram.getProgramFromItem(programStack.getMetadata());
        } else if (curProgram != null && (programStack.isEmpty() || curProgram.getClass() != ItemAssemblyProgram.getProgramFromItem(programStack.getMetadata()).getClass())) {
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
                    if (assemblySystem != null && (!isMachineMissing || curProgram == null) && !isMachineDuplicate) {
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
                    || curProgram.curProblem != AssemblyProgram.EnumTubeProblem.NO_PROBLEM;
        }
        super.update();
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
        AssemblySystem assemblySystem = new AssemblySystem(this);
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

    @SideOnly(Side.CLIENT)
    public void addProblems(List<String> problemList) {
        if (curProgram == null) {
            problemList.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.tab.problems.assembly_controller.no_program")));
        } else {
            if (isMachineDuplicate) {
                String key = I18n.format(duplicateMachine.getTranslationKey());
                problemList.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.tab.problems.assembly_controller.duplicateMachine", key)));
            } else if (!isMachineMissing) {
                curProgram.addProgramProblem(problemList);
            } else {
                String key = I18n.format(missingMachine.getTranslationKey());
                problemList.addAll(PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.tab.problems.assembly_controller.missingMachine", key)));
            }
        }
    }

    public List<IAssemblyMachine> findMachines(int max) {
        List<IAssemblyMachine> machineList = new ArrayList<>();
        findMachines(machineList, getPos(), max);
        return machineList;
    }

    private void findMachines(List<IAssemblyMachine> machineList, BlockPos pos, int max) {
        for (EnumFacing dir : EnumFacing.HORIZONTALS) {
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
        updateConnections();
        invalidateAssemblySystem();
    }

    private void updateConnections() {
        List<Pair<EnumFacing, IAirHandler>> connections = getAirHandler(null).getConnectedPneumatics();
        Arrays.fill(sidesConnected, false);
        for (Pair<EnumFacing, IAirHandler> entry : connections) {
            sidesConnected[entry.getKey().ordinal()] = true;
        }
    }

    @Override
    public boolean isConnectedTo(EnumFacing side) {
        return side != EnumFacing.UP;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
    }

    @Override
    public String getName() {
        return Blockss.ASSEMBLY_CONTROLLER.getTranslationKey();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        goingToHomePosition = tag.getBoolean("goingToHomePosition");
        displayedText = tag.getString("displayedText");
        for (int i = 0; i < 6; i++) {
            sidesConnected[i] = tag.getBoolean("sideConnected" + i);
        }
        inventory.deserializeNBT(tag.getCompoundTag("Items"));
        if (!inventory.getStackInSlot(PROGRAM_INVENTORY_INDEX).isEmpty()) {
            curProgram = ItemAssemblyProgram.getProgramFromItem(inventory.getStackInSlot(PROGRAM_INVENTORY_INDEX).getMetadata());
            if (curProgram != null) curProgram.readFromNBT(tag);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("goingToHomePosition", goingToHomePosition);
        tag.setString("displayedText", displayedText);
        if (curProgram != null) curProgram.writeToNBT(tag);
        for (int i = 0; i < 6; i++) {
            tag.setBoolean("sideConnected" + i, sidesConnected[i]);
        }
        tag.setTag("Items", inventory.serializeNBT());
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

    public class AssemblySystem {
        final IAssemblyMachine[] machines = new IAssemblyMachine[EnumMachine.values().length];

        AssemblySystem(TileEntityAssemblyController controller) {
        }

        private IAssemblyMachine get(EnumMachine machine) {
            return machines[machine.ordinal()];
        }

        boolean addMachine(IAssemblyMachine machine) {
            if (machines[machine.getAssemblyType().ordinal()] != null) {
                return false;  // already present
            }
            machines[machine.getAssemblyType().ordinal()] = machine;
            machine.setControllerPos(getPos());
            return true;
        }

        boolean reset() {
            boolean resetDone = true;
            for (IAssemblyMachine machine : machines) {
                if (machine instanceof IResettable) {
                    if (!((IResettable) machine).reset()) {
                        resetDone = false;
                        if (machine instanceof TileEntityAssemblyPlatform) {
                            getExportUnit().pickupItem(null);
                        }
                        break;
                    }
                }
            }
            return resetDone;
        }

        void setSpeed(float speedMult) {
            for (IAssemblyMachine te : machines) {
                if (te != null) te.setSpeed(speedMult);
            }
        }

        TileEntityAssemblyController getController() {
            return (TileEntityAssemblyController) get(EnumMachine.CONTROLLER);
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
            for (EnumMachine e : requiredMachines) {
                if (get(e) == null) {
                    return e;
                }
            }
            return null;
        }
    }

}
