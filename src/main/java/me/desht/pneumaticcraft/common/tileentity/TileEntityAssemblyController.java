package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerAssemblyController;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.pressure.AirHandler;
import me.desht.pneumaticcraft.common.recipes.programs.AssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.programs.AssemblyProgram.EnumMachine;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityAssemblyController extends TileEntityPneumaticBase implements IAssemblyMachine, IMinWorkingPressure, INamedContainerProvider {
    private static final int PROGRAM_SLOT = 0;
    private static final int INVENTORY_SIZE = 1;

    private final ItemStackHandler itemHandler = new BaseItemStackHandler(this, INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() instanceof ItemAssemblyProgram;
        }
    };
    private final LazyOptional<IItemHandlerModifiable> inventoryCap = LazyOptional.of(() -> itemHandler);

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

    public TileEntityAssemblyController() {
        super(ModTileEntityTypes.ASSEMBLY_CONTROLLER,  PneumaticValues.DANGER_PRESSURE_ASSEMBLY_CONTROLLER, PneumaticValues.MAX_PRESSURE_ASSEMBLY_CONTROLLER, PneumaticValues.VOLUME_ASSEMBLY_CONTROLLER, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
    }

    @Override
    public LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return inventoryCap;
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return itemHandler;
    }

    @Override
    public void tick() {
        ItemStack programStack = itemHandler.getStackInSlot(PROGRAM_SLOT);

        // curProgram must be available on the client, or we can't show program-problems in the GUI
        if (curProgram == null && !goingToHomePosition && programStack.getItem() instanceof ItemAssemblyProgram) {
            curProgram = ((ItemAssemblyProgram) programStack.getItem()).getProgram();
        } else if (curProgram != null && (programStack.isEmpty() || curProgram != ItemAssemblyProgram.getProgram(programStack))) {
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

    @OnlyIn(Dist.CLIENT)
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
        for (Direction dir : PneumaticCraftUtils.HORIZONTALS) {
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
        BlockState newState = AirHandler.getBlockConnectionState(getBlockState(), getAirHandler(null));
        newState = newState.with(BlockPneumaticCraft.UP, false);  // never connects from above
        world.setBlockState(pos, newState);
    }

    @Override
    public boolean canConnectTo(Direction side) {
        return side != Direction.UP;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
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

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
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
