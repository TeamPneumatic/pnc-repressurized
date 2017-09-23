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
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TileEntityAssemblyController extends TileEntityPneumaticBase implements IAssemblyMachine, IMinWorkingPressure {
    private final AssemblyControllerHandler inventory = new AssemblyControllerHandler();
    @DescSynced
    public boolean[] sidesConnected = new boolean[6];
    public AssemblyProgram curProgram;
    private static final int INVENTORY_SIZE = 1;
    @GuiSynced
    public boolean foundAllMachines;
    @GuiSynced
    private boolean foundDuplicateMachine;
    private boolean goingToHomePosition;
    @DescSynced
    public String displayedText = "";
    public static final int PROGRAM_INVENTORY_INDEX = 0;
    @DescSynced
    public boolean hasProblem;

    private static class AssemblyControllerHandler extends FilteredItemStackHandler {
        AssemblyControllerHandler() {
            super(INVENTORY_SIZE);
        }

        @Override
        public boolean test(Integer integer, ItemStack itemStack) {
            return itemStack.isEmpty() || itemStack.getItem() == Itemss.ASSEMBLY_PROGRAM;
        }
    }

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

        if (!getWorld().isRemote && firstRun) updateConnections();

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
                    List<IAssemblyMachine> machineList = getMachines();
                    EnumMachine[] requiredMachines = curProgram != null ? curProgram.getRequiredMachines() : EnumMachine.values();
                    TileEntityAssemblyDrill drill = null;
                    TileEntityAssemblyLaser laser = null;
                    TileEntityAssemblyIOUnit ioUnitImport = null;
                    TileEntityAssemblyIOUnit ioUnitExport = null;
                    TileEntityAssemblyPlatform platform = null;
                    foundDuplicateMachine = false;
                    boolean foundMachines[] = new boolean[requiredMachines.length];
                    for (IAssemblyMachine machine : machineList) {
                        if (machine != this && machine instanceof TileEntityAssemblyController)
                            foundDuplicateMachine = true;
                        for (int i = 0; i < requiredMachines.length; i++) {
                            switch (requiredMachines[i]) {
                                case DRILL:
                                    if (machine instanceof TileEntityAssemblyDrill) {
                                        if (drill != null) foundDuplicateMachine = true;
                                        drill = (TileEntityAssemblyDrill) machine;
                                        foundMachines[i] = true;
                                    }
                                    break;
                                case LASER:
                                    if (machine instanceof TileEntityAssemblyLaser) {
                                        if (laser != null) foundDuplicateMachine = true;
                                        laser = (TileEntityAssemblyLaser) machine;
                                        foundMachines[i] = true;
                                    }
                                    break;
                                case IO_UNIT_IMPORT:
                                    if (machine instanceof TileEntityAssemblyIOUnit && ((TileEntityAssemblyIOUnit) machine).isImportUnit()) {
                                        if (ioUnitImport != null) foundDuplicateMachine = true;
                                        ioUnitImport = (TileEntityAssemblyIOUnit) machine;
                                        foundMachines[i] = true;
                                    }
                                    break;
                                case IO_UNIT_EXPORT:
                                    if (machine instanceof TileEntityAssemblyIOUnit && !((TileEntityAssemblyIOUnit) machine).isImportUnit()) {
                                        if (ioUnitExport != null) foundDuplicateMachine = true;
                                        ioUnitExport = (TileEntityAssemblyIOUnit) machine;
                                        foundMachines[i] = true;
                                    }
                                    break;
                                case PLATFORM:
                                    if (machine instanceof TileEntityAssemblyPlatform) {
                                        if (platform != null) foundDuplicateMachine = true;
                                        platform = (TileEntityAssemblyPlatform) machine;
                                        foundMachines[i] = true;
                                    }
                                    break;
                            }
                        }
                    }

                    foundAllMachines = true;
                    for (boolean foundMachine : foundMachines) {
                        if (!foundMachine) {
                            foundAllMachines = false;
                            break;
                        }
                    }

                    if ((foundAllMachines || curProgram == null) && !foundDuplicateMachine) {
                        // if(firstRun || areAllMachinesDone(machineList)) {
                        boolean useAir;
                        if (curProgram != null) {
                            useAir = curProgram.executeStep(this, platform, ioUnitImport, ioUnitExport, drill, laser);
                            if (useAir) setStatus("Running...");
                        } else {
                            useAir = true;
                            goToHomePosition(platform, ioUnitImport, ioUnitExport, drill, laser);
                            setStatus("Resetting...");
                        }
                        if (useAir)
                            addAir(-(int) (PneumaticValues.USAGE_ASSEMBLING * getSpeedUsageMultiplierFromUpgrades()));
                        float speedMultiplier = getSpeedMultiplierFromUpgrades();
                        for (IAssemblyMachine machine : machineList) {
                            machine.setSpeed(speedMultiplier);
                        }
                    }
                }
            }
            hasProblem = hasProblem();
        }
        super.update();

    }

    private void setStatus(String text) {
//        if (!text.equals(displayedText)) System.out.println("Status change! " + displayedText + " -> " + text);
        displayedText = text;
    }

    private void goToHomePosition(TileEntityAssemblyPlatform platform, TileEntityAssemblyIOUnit ioUnitImport, TileEntityAssemblyIOUnit ioUnitExport, TileEntityAssemblyDrill drill, TileEntityAssemblyLaser laser) {

        boolean resetDone = true;

        for (IResettable machine : new IResettable[]{drill, laser, ioUnitImport, platform, ioUnitExport}) {
            if (machine != null && !machine.reset()) {
                resetDone = false;

                if (machine == platform) {
                    if (ioUnitExport != null) ioUnitExport.pickupItem(null);
                }

                break;
            }
        }

        goingToHomePosition = !(foundAllMachines && resetDone);
    }

    public void addProblems(List<String> problemList) {
        if (getPressure() < PneumaticValues.MIN_PRESSURE_ASSEMBLY_CONTROLLER) {
            problemList.add(TextFormatting.GRAY + "No sufficient pressure.");
            problemList.add(TextFormatting.BLACK + "Add pressure.");
        }
        if (curProgram == null) {
            problemList.add(TextFormatting.GRAY + "There's no program to run.");
            problemList.add(TextFormatting.BLACK + "Insert an Assembly Program.");
        } else {
            if (foundDuplicateMachine) {
                problemList.addAll(PneumaticCraftUtils.convertStringIntoList(TextFormatting.GRAY + "Controller found a duplicate machine!", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
                problemList.addAll(PneumaticCraftUtils.convertStringIntoList(TextFormatting.BLACK + "Remove it so there is one machine of each type.", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
            } else if (foundAllMachines) {
                curProgram.addProgramProblem(problemList);
            } else {
                problemList.addAll(PneumaticCraftUtils.convertStringIntoList(TextFormatting.GRAY + "Not all machines required for this program are available.", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
                problemList.addAll(PneumaticCraftUtils.convertStringIntoList(TextFormatting.BLACK + "Connect up the other required machines.", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
            }
        }
    }

    private boolean hasProblem() {
        List<String> textList = new ArrayList<>();
        if (curProgram != null) {
            curProgram.addProgramProblem(textList);
        }
        return !foundAllMachines || foundDuplicateMachine || getPressure() < PneumaticValues.MIN_PRESSURE_ASSEMBLY_CONTROLLER || curProgram == null || textList.size() > 0;
    }

    public List<IAssemblyMachine> getMachines() {
        List<IAssemblyMachine> machineList = new ArrayList<IAssemblyMachine>();
        getMachines(machineList, getPos());
        return machineList;
    }

    public boolean areAllMachinesDone(List<IAssemblyMachine> machineList) {
        for (IAssemblyMachine machine : machineList) {
            if (!machine.isIdle()) return false;
        }
        return true;
    }

    private void getMachines(List<IAssemblyMachine> machineList, BlockPos pos) {
        for (EnumFacing dir : EnumFacing.HORIZONTALS) {
            TileEntity te = getWorld().getTileEntity(pos.offset(dir));
            if (te instanceof IAssemblyMachine && !machineList.contains(te)) {
                machineList.add((IAssemblyMachine) te);
                getMachines(machineList, te.getPos());
            }
        }
    }

    public void updateConnections() {
        List<Pair<EnumFacing, IAirHandler>> connections = getAirHandler(null).getConnectedPneumatics();
        Arrays.fill(sidesConnected, false);
        for (Pair<EnumFacing, IAirHandler> entry : connections) {
            sidesConnected[entry.getKey().ordinal()] = true;
        }
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        updateConnections();
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

        return Blockss.ASSEMBLY_CONTROLLER.getUnlocalizedName();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

        super.readFromNBT(tag);
        goingToHomePosition = tag.getBoolean("goingToHomePosition");
        foundAllMachines = tag.getBoolean("foundAllMachines");
        foundDuplicateMachine = tag.getBoolean("foundDuplicate");
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
        tag.setBoolean("foundAllMachines", foundAllMachines);
        tag.setBoolean("foundDuplicate", foundDuplicateMachine);
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
}
