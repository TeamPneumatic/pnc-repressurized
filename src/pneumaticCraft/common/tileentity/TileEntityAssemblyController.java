package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemAssemblyProgram;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.recipes.programs.AssemblyProgram;
import pneumaticCraft.common.recipes.programs.AssemblyProgram.EnumMachine;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityAssemblyController extends TileEntityPneumaticBase implements ISidedInventory, IAssemblyMachine,
        IMinWorkingPressure{
    private ItemStack[] inventory;
    @DescSynced
    public boolean[] sidesConnected = new boolean[6];
    public AssemblyProgram curProgram;
    private final int INVENTORY_SIZE = 5;
    @GuiSynced
    public boolean foundAllMachines;
    @GuiSynced
    private boolean foundDuplicateMachine;
    private boolean goingToHomePosition;
    @DescSynced
    public String displayedText = "";
    public static final int PROGRAM_INVENTORY_INDEX = 0;
    public static final int UPGRADE_SLOT_START = 1;
    public static final int UPGRADE_SLOT_END = 4;
    @DescSynced
    public boolean hasProblem;

    public TileEntityAssemblyController(){
        super(PneumaticValues.DANGER_PRESSURE_ASSEMBLY_CONTROLLER, PneumaticValues.MAX_PRESSURE_ASSEMBLY_CONTROLLER, PneumaticValues.VOLUME_ASSEMBLY_CONTROLLER);
        inventory = new ItemStack[INVENTORY_SIZE];
        setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 2, 3, UPGRADE_SLOT_END});
    }

    @Override
    public void updateEntity(){

        if(!worldObj.isRemote && firstRun) updateConnections();

        // curProgram must be available on the client, or we can't show program-problems in the GUI
        if(curProgram == null && !goingToHomePosition && inventory[PROGRAM_INVENTORY_INDEX] != null && inventory[PROGRAM_INVENTORY_INDEX].getItem() == Itemss.assemblyProgram) {
            AssemblyProgram program = ItemAssemblyProgram.getProgramFromItem(inventory[PROGRAM_INVENTORY_INDEX].getItemDamage());
            curProgram = program;
        } else if(curProgram != null && (inventory[PROGRAM_INVENTORY_INDEX] == null || curProgram.getClass() != ItemAssemblyProgram.getProgramFromItem(inventory[PROGRAM_INVENTORY_INDEX].getItemDamage()).getClass())) {
            curProgram = null;
            if(!worldObj.isRemote) goingToHomePosition = true;
        }

        if(!worldObj.isRemote) {
            displayedText = "Standby";
            if(getPressure(ForgeDirection.UNKNOWN) >= PneumaticValues.MIN_PRESSURE_ASSEMBLY_CONTROLLER) {
                if(curProgram != null || goingToHomePosition) {
                    List<IAssemblyMachine> machineList = getMachines();
                    EnumMachine[] requiredMachines = curProgram != null ? curProgram.getRequiredMachines() : EnumMachine.values();
                    TileEntityAssemblyDrill drill = null;
                    TileEntityAssemblyLaser laser = null;
                    TileEntityAssemblyIOUnit ioUnitImport = null;
                    TileEntityAssemblyIOUnit ioUnitExport = null;
                    TileEntityAssemblyPlatform platform = null;
                    foundDuplicateMachine = false;
                    boolean foundMachines[] = new boolean[requiredMachines.length];
                    for(IAssemblyMachine machine : machineList) {
                        if(machine != this && machine instanceof TileEntityAssemblyController) foundDuplicateMachine = true;
                        for(int i = 0; i < requiredMachines.length; i++) {
                            switch(requiredMachines[i]){
                                case DRILL:
                                    if(machine instanceof TileEntityAssemblyDrill) {
                                        if(drill != null) foundDuplicateMachine = true;
                                        drill = (TileEntityAssemblyDrill)machine;
                                        foundMachines[i] = true;
                                    }
                                    break;
                                case LASER:
                                    if(machine instanceof TileEntityAssemblyLaser) {
                                        if(laser != null) foundDuplicateMachine = true;
                                        laser = (TileEntityAssemblyLaser)machine;
                                        foundMachines[i] = true;
                                    }
                                    break;
                                case IO_UNIT_IMPORT:
                                    if(machine instanceof TileEntityAssemblyIOUnit && ((TileEntityAssemblyIOUnit)machine).getBlockMetadata() == 0) {
                                        if(ioUnitImport != null) foundDuplicateMachine = true;
                                        ioUnitImport = (TileEntityAssemblyIOUnit)machine;
                                        foundMachines[i] = true;
                                    }
                                    break;
                                case IO_UNIT_EXPORT:
                                    if(machine instanceof TileEntityAssemblyIOUnit && ((TileEntityAssemblyIOUnit)machine).getBlockMetadata() == 1) {
                                        if(ioUnitExport != null) foundDuplicateMachine = true;
                                        ioUnitExport = (TileEntityAssemblyIOUnit)machine;
                                        foundMachines[i] = true;
                                    }
                                    break;
                                case PLATFORM:
                                    if(machine instanceof TileEntityAssemblyPlatform) {
                                        if(platform != null) foundDuplicateMachine = true;
                                        platform = (TileEntityAssemblyPlatform)machine;
                                        foundMachines[i] = true;
                                    }
                                    break;
                            }
                        }
                    }

                    foundAllMachines = true;
                    for(boolean foundMachine : foundMachines) {
                        if(!foundMachine) {
                            foundAllMachines = false;
                            break;
                        }
                    }

                    if((foundAllMachines || curProgram == null) && !foundDuplicateMachine) {
                        // if(firstRun || areAllMachinesDone(machineList)) {
                        boolean useAir;
                        if(curProgram != null) {
                            useAir = curProgram.executeStep(this, platform, ioUnitImport, ioUnitExport, drill, laser);
                            if(useAir) displayedText = "Running...";
                        } else {
                            useAir = true;
                            goToHomePosition(platform, ioUnitImport, ioUnitExport, drill, laser);
                            displayedText = "Resetting...";
                        }
                        if(useAir) addAir(-(int)(PneumaticValues.USAGE_ASSEMBLING * getSpeedUsageMultiplierFromUpgrades(getUpgradeSlots())), ForgeDirection.UNKNOWN);
                        float speedMultiplier = getSpeedMultiplierFromUpgrades(getUpgradeSlots());
                        for(IAssemblyMachine machine : machineList) {
                            machine.setSpeed(speedMultiplier);
                        }
                    }
                }
            }
            hasProblem = hasProblem();
        }
        super.updateEntity();

    }

    private void goToHomePosition(TileEntityAssemblyPlatform platform, TileEntityAssemblyIOUnit ioUnitImport, TileEntityAssemblyIOUnit ioUnitExport, TileEntityAssemblyDrill drill, TileEntityAssemblyLaser laser){

        boolean resetDone = true;

        for(IResettable machine : new IResettable[]{drill, laser, ioUnitImport, platform, ioUnitExport}) {
            if(machine != null && !machine.reset()) {
                resetDone = false;

                if(machine == platform) {
                    if(ioUnitExport != null) ioUnitExport.pickupItem(null);
                }

                break;
            }
        }

        goingToHomePosition = !(foundAllMachines && resetDone);
    }

    public void addProblems(List<String> problemList){
        if(getPressure(ForgeDirection.UNKNOWN) < PneumaticValues.MIN_PRESSURE_ASSEMBLY_CONTROLLER) {
            problemList.add(EnumChatFormatting.GRAY + "No sufficient pressure.");
            problemList.add(EnumChatFormatting.BLACK + "Add pressure.");
        }
        if(curProgram == null) {
            problemList.add(EnumChatFormatting.GRAY + "There's no program to run.");
            problemList.add(EnumChatFormatting.BLACK + "Insert an Assembly Program.");
        } else {
            if(foundDuplicateMachine) {
                problemList.addAll(PneumaticCraftUtils.convertStringIntoList(EnumChatFormatting.GRAY + "Controller found a duplicate machine!", GuiConstants.maxCharPerLineLeft));
                problemList.addAll(PneumaticCraftUtils.convertStringIntoList(EnumChatFormatting.BLACK + "Remove it so there is one machine of each type.", GuiConstants.maxCharPerLineLeft));
            } else if(foundAllMachines) {
                curProgram.addProgramProblem(problemList);
            } else {
                problemList.addAll(PneumaticCraftUtils.convertStringIntoList(EnumChatFormatting.GRAY + "Not all machines required for this program are available.", GuiConstants.maxCharPerLineLeft));
                problemList.addAll(PneumaticCraftUtils.convertStringIntoList(EnumChatFormatting.BLACK + "Connect up the other required machines.", GuiConstants.maxCharPerLineLeft));
            }
        }
    }

    public boolean hasProblem(){
        List<String> textList = null;
        if(curProgram != null) {
            textList = new ArrayList<String>();
            curProgram.addProgramProblem(textList);
        }
        return !foundAllMachines || foundDuplicateMachine || getPressure(ForgeDirection.UNKNOWN) < PneumaticValues.MIN_PRESSURE_ASSEMBLY_CONTROLLER || curProgram == null || textList.size() > 0;
    }

    public List<IAssemblyMachine> getMachines(){
        List<IAssemblyMachine> machineList = new ArrayList<IAssemblyMachine>();
        getMachines(machineList, xCoord, yCoord, zCoord);
        return machineList;
    }

    public boolean areAllMachinesDone(List<IAssemblyMachine> machineList){
        for(IAssemblyMachine machine : machineList) {
            if(!machine.isIdle()) return false;
        }
        return true;
    }

    private void getMachines(List<IAssemblyMachine> machineList, int x, int y, int z){
        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if(dir == ForgeDirection.UP || dir == ForgeDirection.DOWN) continue;
            TileEntity te = worldObj.getTileEntity(x + dir.offsetX, y, z + dir.offsetZ);
            if(te instanceof IAssemblyMachine && !machineList.contains(te)) {
                machineList.add((IAssemblyMachine)te);
                getMachines(machineList, te.xCoord, te.yCoord, te.zCoord);
            }
        }
    }

    public void updateConnections(){
        for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity te = worldObj.getTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
            if(te instanceof IPneumaticMachine) {
                sidesConnected[direction.ordinal()] = ((IPneumaticMachine)te).isConnectedTo(direction.getOpposite());
            } else {
                sidesConnected[direction.ordinal()] = false;
            }
        }
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        return side != ForgeDirection.UP;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox(){
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory(){

        return inventory.length;
    }

    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int slot){

        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount){

        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            if(itemStack.stackSize <= amount) {
                setInventorySlotContents(slot, null);
            } else {
                itemStack = itemStack.splitStack(amount);
                if(itemStack.stackSize == 0) {
                    setInventorySlotContents(slot, null);
                }
            }
        }

        return itemStack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot){

        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            setInventorySlotContents(slot, null);
        }
        return itemStack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack){

        inventory[slot] = itemStack;
        if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public String getInventoryName(){

        return Blockss.assemblyController.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){

        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1){
        return isGuiUseableByPlayer(var1);
    }

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){}

    @Override
    public void readFromNBT(NBTTagCompound tag){

        super.readFromNBT(tag);
        goingToHomePosition = tag.getBoolean("goingToHomePosition");
        foundAllMachines = tag.getBoolean("foundAllMachines");
        foundDuplicateMachine = tag.getBoolean("foundDuplicate");
        displayedText = tag.getString("displayedText");
        for(int i = 0; i < 6; i++) {
            sidesConnected[i] = tag.getBoolean("sideConnected" + i);
        }
        // Read in the ItemStacks in the inventory from NBT
        NBTTagList tagList = tag.getTagList("Items", 10);
        inventory = new ItemStack[getSizeInventory()];
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
        if(inventory[PROGRAM_INVENTORY_INDEX] != null) {
            curProgram = ItemAssemblyProgram.getProgramFromItem(inventory[PROGRAM_INVENTORY_INDEX].getItemDamage());
            if(curProgram != null) curProgram.readFromNBT(tag);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){

        super.writeToNBT(tag);
        tag.setBoolean("goingToHomePosition", goingToHomePosition);
        tag.setBoolean("foundAllMachines", foundAllMachines);
        tag.setBoolean("foundDuplicate", foundDuplicateMachine);
        tag.setString("displayedText", displayedText);
        if(curProgram != null) curProgram.writeToNBT(tag);
        for(int i = 0; i < 6; i++) {
            tag.setBoolean("sideConnected" + i, sidesConnected[i]);
        }
        // Write the ItemStacks in the inventory to NBT
        NBTTagList tagList = new NBTTagList();
        for(int currentIndex = 0; currentIndex < inventory.length; ++currentIndex) {
            if(inventory[currentIndex] != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)currentIndex);
                inventory[currentIndex].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        tag.setTag("Items", tagList);
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack){
        if(i > 0) {
            return itemstack != null && itemstack.getItem() == Itemss.machineUpgrade;
        } else {
            return itemstack != null && itemstack.getItem() == Itemss.assemblyProgram;
        }
    }

    @Override
    // upgrades in bottom, fuel in the rest.
    public int[] getAccessibleSlotsFromSide(int var1){
        if(var1 == 0) return new int[]{1, 2, 3, 4};
        return new int[]{0};
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, int j){
        return true;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, int j){
        return true;
    }

    @Override
    public boolean isIdle(){
        return true;
    }

    @Override
    public void setSpeed(float speed){}

    @Override
    public boolean hasCustomInventoryName(){
        return false;
    }

    @Override
    public float getMinWorkingPressure(){
        return PneumaticValues.MIN_PRESSURE_ASSEMBLY_CONTROLLER;
    }
}
