package pneumaticCraft.common.tileentity;

import ic2.api.item.IC2Items;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.inventory.SyncedField;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.DescPacketHandler;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.IDescSynced;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.NetworkUtils;
import pneumaticCraft.common.network.PacketDescription;
import pneumaticCraft.common.thirdparty.computercraft.ILuaMethod;
import pneumaticCraft.common.thirdparty.computercraft.LuaMethod;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.common.util.TileEntityCache;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Optional.InterfaceList({@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = ModIds.COMPUTERCRAFT)})
public class TileEntityBase extends TileEntity implements IGUIButtonSensitive, IDescSynced, IPeripheral{
    /**
     * True only the first time updateEntity invokes in a session.
     */
    protected boolean firstRun = true;
    private int[] upgradeSlots;
    private boolean descriptionPacketScheduled;
    private List<SyncedField> descriptionFields;
    protected int poweredRedstone; //The redstone strength currently applied to the block.
    private TileEntityCache[] tileCache;
    protected List<ILuaMethod> luaMethods = new ArrayList<ILuaMethod>();

    public TileEntityBase(){
        addLuaMethods();
    }

    public TileEntityBase(int... upgradeSlots){
        this();
        this.upgradeSlots = upgradeSlots;
    }

    @Override
    public Packet getDescriptionPacket(){
        return DescPacketHandler.getPacket(new PacketDescription(this));
    }

    protected double getPacketDistance(){
        return 64;
    }

    @Override
    public List<SyncedField> getDescriptionFields(){
        if(descriptionFields == null) {
            descriptionFields = NetworkUtils.getSyncedFields(this, DescSynced.class);
            for(SyncedField field : descriptionFields) {
                field.update();
            }
        }
        return descriptionFields;
    }

    /**
     * Sends the description packet to every client within PACKET_UPDATE_DISTANCE blocks, and in the same dimension.
     */
    public void sendDescriptionPacket(){
        //PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, TileEntityConstants.PACKET_UPDATE_DISTANCE, worldObj.provider.dimensionId, getDescriptionPacket());
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    /**
     * A way to safely mark a block for an update from another thread (like the CC Lua thread).
     */
    protected void scheduleDescriptionPacket(){
        descriptionPacketScheduled = true;
    }

    public void sendDescPacket(double maxPacketDistance){
        NetworkHandler.sendToAllAround(new PacketDescription(this), new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, maxPacketDistance));
    }

    @Override
    public void updateEntity(){
        if(firstRun && !worldObj.isRemote) {
            //firstRun = false;
            onFirstServerUpdate();
            onNeighborTileUpdate();
            onNeighborBlockUpdate();
        }
        firstRun = false;

        if(!worldObj.isRemote) {
            if(this instanceof IHeatExchanger) {
                ((IHeatExchanger)this).getHeatExchangerLogic(ForgeDirection.UNKNOWN).update();
            }

            if(descriptionFields == null) descriptionPacketScheduled = true;
            for(SyncedField field : getDescriptionFields()) {
                if(field.update()) {
                    descriptionPacketScheduled = true;
                }
            }

            if(descriptionPacketScheduled) {
                descriptionPacketScheduled = false;
                sendDescriptionPacket();
            }
        }
    }

    protected void onFirstServerUpdate(){
        initializeIfHeatExchanger();
    }

    protected void updateNeighbours(){
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
    }

    public void onBlockRotated(){}

    protected void rerenderChunk(){
        worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
    }

    protected boolean shouldRerenderChunkOnDescUpdate(){
        return false;
    }

    /**
     * Encoded into the description packet. Also is included in the world save.
     * Used as last resort, using @DescSynced is preferred.
     * @param tag
     */
    @Override
    public void writeToPacket(NBTTagCompound tag){}

    /**
     * Encoded into the description packet. Also is included in the world save.
     * Used as last resort, using @DescSynced is preferred.
     * @param tag
     */
    @Override
    public void readFromPacket(NBTTagCompound tag){}

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        writeToPacket(tag);
        if(this instanceof IHeatExchanger) {
            ((IHeatExchanger)this).getHeatExchangerLogic(ForgeDirection.UNKNOWN).writeToNBT(tag);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        readFromPacket(tag);
        if(this instanceof IHeatExchanger) {
            ((IHeatExchanger)this).getHeatExchangerLogic(ForgeDirection.UNKNOWN).readFromNBT(tag);
        }
    }

    @Override
    public void validate(){
        super.validate();
        scheduleDescriptionPacket();
    }

    @Override
    public void onDescUpdate(){
        if(shouldRerenderChunkOnDescUpdate()) rerenderChunk();
    }

    /**
     * Called when a key is synced in the container.
     */
    public void onGuiUpdate(){}

    public ForgeDirection getRotation(){
        return ForgeDirection.getOrientation(getBlockMetadata());
    }

    public int getUpgrades(int upgradeDamage){
        return getUpgrades(upgradeDamage, this instanceof IPneumaticMachine ? ((IPneumaticMachine)this).getAirHandler().getUpgradeSlots() : getUpgradeSlots());
    }

    protected int getUpgrades(int upgradeDamage, int... upgradeSlots){
        int upgrades = 0;
        IInventory inv = null;
        if(this instanceof IInventory) inv = (IInventory)this;
        if(inv == null && this instanceof TileEntityPneumaticBase && ((TileEntityPneumaticBase)this).parentTile instanceof IInventory) inv = (IInventory)((TileEntityPneumaticBase)this).parentTile;
        if(inv != null) {
            for(int i : upgradeSlots) {
                if(inv.getStackInSlot(i) != null && inv.getStackInSlot(i).getItem() == Itemss.machineUpgrade && inv.getStackInSlot(i).getItemDamage() == upgradeDamage) {
                    upgrades += inv.getStackInSlot(i).stackSize;
                }
            }
        }
        return upgrades;
    }

    protected float getSpeedMultiplierFromUpgrades(){
        return getSpeedMultiplierFromUpgrades(getUpgradeSlots());
    }

    protected float getSpeedUsageMultiplierFromUpgrades(){
        return getSpeedUsageMultiplierFromUpgrades(getUpgradeSlots());
    }

    public float getSpeedMultiplierFromUpgrades(int[] upgradeSlots){
        return (float)Math.pow(PneumaticValues.SPEED_UPGRADE_MULTIPLIER, Math.min(10, getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE, upgradeSlots)));
    }

    protected float getSpeedUsageMultiplierFromUpgrades(int[] upgradeSlots){
        return (float)Math.pow(PneumaticValues.SPEED_UPGRADE_USAGE_MULTIPLIER, Math.min(10, getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE, upgradeSlots)));
    }

    @Optional.Method(modid = ModIds.INDUSTRIALCRAFT)
    protected int getIC2Upgrades(String ic2ItemKey, int[] upgradeSlots){
        ItemStack itemStack = IC2Items.getItem(ic2ItemKey);
        if(itemStack == null) return 0;
        int upgrades = 0;
        if(this instanceof IInventory) {// this always should be true.
            IInventory inv = (IInventory)this;
            for(int i : upgradeSlots) {
                if(inv.getStackInSlot(i) != null && inv.getStackInSlot(i).getItem() == itemStack.getItem()) {
                    upgrades += inv.getStackInSlot(i).stackSize;
                }
            }
        }
        return upgrades;
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player){}

    public boolean isGuiUseableByPlayer(EntityPlayer par1EntityPlayer){
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) != this ? false : par1EntityPlayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }

    public void setUpgradeSlots(int... upgradeSlots){
        this.upgradeSlots = upgradeSlots;
    }

    public int[] getUpgradeSlots(){
        return upgradeSlots;
    }

    public static void writeInventoryToNBT(NBTTagCompound tag, ItemStack[] stacks){
        writeInventoryToNBT(tag, stacks, "Items");
    }

    public static void writeInventoryToNBT(NBTTagCompound tag, IInventory inventory, String tagName){
        ItemStack[] stacks = new ItemStack[inventory.getSizeInventory()];
        for(int i = 0; i < stacks.length; i++) {
            stacks[i] = inventory.getStackInSlot(i);
        }
        writeInventoryToNBT(tag, stacks, tagName);
    }

    public static void writeInventoryToNBT(NBTTagCompound tag, ItemStack[] stacks, String tagName){
        NBTTagList tagList = new NBTTagList();
        for(int i = 0; i < stacks.length; i++) {
            if(stacks[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                stacks[i].writeToNBT(itemTag);
                itemTag.setByte("Slot", (byte)i);
                tagList.appendTag(itemTag);
            }
        }
        tag.setTag(tagName, tagList);
    }

    public static void readInventoryFromNBT(NBTTagCompound tag, ItemStack[] stacks){
        readInventoryFromNBT(tag, stacks, "Items");
    }

    public static void readInventoryFromNBT(NBTTagCompound tag, IInventory inventory, String tagName){
        ItemStack[] stacks = new ItemStack[inventory.getSizeInventory()];
        readInventoryFromNBT(tag, stacks, tagName);
        for(int i = 0; i < stacks.length; i++) {
            inventory.setInventorySlotContents(i, stacks[i]);
        }
    }

    public static void readInventoryFromNBT(NBTTagCompound tag, ItemStack[] stacks, String tagName){
        for(int i = 0; i < stacks.length; i++) {
            stacks[i] = null;
        }
        NBTTagList tagList = tag.getTagList(tagName, 10);
        for(int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound itemTag = tagList.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot");
            if(slot >= 0 && slot < stacks.length) {
                stacks[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
    }

    public void onNeighborTileUpdate(){
        initializeIfHeatExchanger();
        for(TileEntityCache cache : getTileCache()) {
            cache.update();
        }
    }

    public TileEntityCache[] getTileCache(){
        if(tileCache == null) tileCache = TileEntityCache.getDefaultCache(worldObj, xCoord, yCoord, zCoord);
        return tileCache;
    }

    public void onNeighborBlockUpdate(){
        poweredRedstone = PneumaticCraftUtils.getRedstoneLevel(worldObj, xCoord, yCoord, zCoord);
        initializeIfHeatExchanger();
    }

    public boolean redstoneAllows(){
        if(worldObj.isRemote) onNeighborBlockUpdate();
        switch(((IRedstoneControl)this).getRedstoneMode()){
            case 0:
                return true;
            case 1:
                return poweredRedstone > 0;
            case 2:
                return poweredRedstone == 0;
        }
        return false;
    }

    protected void initializeIfHeatExchanger(){
        if(this instanceof IHeatExchanger) {
            initializeHeatExchanger(((IHeatExchanger)this).getHeatExchangerLogic(ForgeDirection.UNKNOWN), getConnectedHeatExchangerSides());
        }
    }

    protected void initializeHeatExchanger(IHeatExchangerLogic heatExchanger, ForgeDirection... connectedSides){
        heatExchanger.initializeAsHull(worldObj, xCoord, yCoord, zCoord, connectedSides);
    }

    /**
     * Gets the valid sides for heat exchanging to be allowed. returning an empty array will allow any side.
     * @return
     */
    protected ForgeDirection[] getConnectedHeatExchangerSides(){
        return new ForgeDirection[0];
    }

    public void autoExportLiquid(){
        FluidStack extractedStack = ((IFluidHandler)this).drain(ForgeDirection.UNKNOWN, Integer.MAX_VALUE, false);
        if(extractedStack != null) {
            for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                TileEntity te = getTileCache()[d.ordinal()].getTileEntity();
                if(te instanceof IFluidHandler) {
                    if(((IFluidHandler)te).canFill(d.getOpposite(), extractedStack.getFluid())) {
                        int filledAmount = ((IFluidHandler)te).fill(d.getOpposite(), extractedStack, true);
                        ((IFluidHandler)this).drain(ForgeDirection.UNKNOWN, filledAmount, true);
                        extractedStack.amount -= filledAmount;
                        if(extractedStack.amount <= 0) break;
                    }
                }
            }
        }
    }

    @Override
    public Type getSyncType(){
        return Type.TILE_ENTITY;
    }

    @Override
    public int getX(){
        return xCoord;
    }

    @Override
    public int getY(){
        return yCoord;
    }

    @Override
    public int getZ(){
        return zCoord;
    }

    protected void processFluidItem(int inputSlot, int outputSlot){
        IInventory inv = (IInventory)this;
        IFluidHandler fluidHandler = (IFluidHandler)this;
        FluidTankInfo tankInfo = fluidHandler.getTankInfo(ForgeDirection.UNKNOWN)[0];
        if(inv.getStackInSlot(inputSlot) != null) {
            ItemStack fluidContainer = inv.getStackInSlot(inputSlot);
            if(tankInfo.fluid == null || tankInfo.fluid.isFluidEqual(fluidContainer)) {
                FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(fluidContainer);
                int amount = FluidContainerRegistry.BUCKET_VOLUME;
                if(fluid == null) {
                    if(fluidContainer.getItem() instanceof IFluidContainerItem) {
                        IFluidContainerItem containerItem = (IFluidContainerItem)fluidContainer.getItem();
                        fluid = containerItem.getFluid(fluidContainer);
                        if(fluid != null && fluidHandler.canFill(ForgeDirection.UNKNOWN, fluid.getFluid())) {
                            amount = fluid != null ? fluid.amount : 0;
                            int availableSpace = tankInfo.capacity - (tankInfo.fluid != null ? tankInfo.fluid.amount : 0);
                            if(availableSpace >= amount) {
                                ItemStack singleFuelItem = fluidContainer.copy();
                                singleFuelItem.stackSize = 1;
                                FluidStack drainedStack = containerItem.drain(singleFuelItem, availableSpace, true);
                                if(fluidContainer.stackSize == 1 || inv.getStackInSlot(outputSlot) == null || canStack(singleFuelItem, inv.getStackInSlot(outputSlot))) {
                                    fluidHandler.fill(ForgeDirection.UNKNOWN, drainedStack, true);
                                    if(fluidContainer.stackSize == 1) {
                                        inv.setInventorySlotContents(inputSlot, singleFuelItem);
                                    } else {
                                        inv.getStackInSlot(inputSlot).stackSize--;
                                        if(inv.getStackInSlot(outputSlot) == null) {
                                            inv.setInventorySlotContents(outputSlot, singleFuelItem);
                                        } else {
                                            inv.getStackInSlot(outputSlot).stackSize++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if(fluidHandler.canFill(ForgeDirection.UNKNOWN, fluid.getFluid())) {
                    if(tankInfo.capacity - (tankInfo.fluid != null ? tankInfo.fluid.amount : 0) >= amount) {
                        ItemStack returnedItem = null;
                        FluidContainerData[] allFluidData = FluidContainerRegistry.getRegisteredFluidContainerData();
                        for(FluidContainerData fluidData : allFluidData) {
                            if(fluidData.filledContainer.isItemEqual(fluidContainer)) {
                                returnedItem = fluidData.emptyContainer;
                                break;
                            }
                        }
                        if(returnedItem == null || inv.getStackInSlot(outputSlot) == null || canStack(returnedItem, inv.getStackInSlot(outputSlot))) {
                            if(returnedItem != null) {
                                if(inv.getStackInSlot(outputSlot) == null) {
                                    inv.setInventorySlotContents(outputSlot, returnedItem.copy());
                                } else {
                                    inv.getStackInSlot(outputSlot).stackSize += returnedItem.stackSize;
                                }
                            }
                            fluidHandler.fill(ForgeDirection.UNKNOWN, new FluidStack(fluid.getFluid(), amount, fluid.tag), true);
                            inv.getStackInSlot(inputSlot).stackSize--;
                            if(inv.getStackInSlot(inputSlot).stackSize <= 0) inv.setInventorySlotContents(inputSlot, null);
                        }
                    }
                }
            }
            if(fluidContainer.getItem() instanceof IFluidContainerItem) {
                if(((IFluidContainerItem)fluidContainer.getItem()).getFluid(fluidContainer) == null && (inv.getStackInSlot(outputSlot) == null || canStack(fluidContainer, inv.getStackInSlot(outputSlot)))) {
                    if(inv.getStackInSlot(outputSlot) == null) {
                        inv.setInventorySlotContents(outputSlot, fluidContainer);
                    } else {
                        inv.getStackInSlot(outputSlot).stackSize += fluidContainer.stackSize;
                    }
                    inv.setInventorySlotContents(inputSlot, null);
                }
            }
        }
    }

    private boolean canStack(ItemStack stack1, ItemStack stack2){
        return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2) && stack1.stackSize + stack2.stackSize <= stack1.getMaxStackSize();
    }

    /*
     * COMPUTERCRAFT API 
     */

    protected void addLuaMethods(){
        if(this instanceof IHeatExchanger) {
            final IHeatExchanger exchanger = (IHeatExchanger)this;
            luaMethods.add(new LuaMethod("getTemperature"){
                @Override
                public Object[] call(Object[] args) throws Exception{
                    if(args.length == 0) {
                        return new Object[]{exchanger.getHeatExchangerLogic(ForgeDirection.UNKNOWN).getTemperature()};
                    } else if(args.length == 1) {
                        IHeatExchangerLogic logic = exchanger.getHeatExchangerLogic(getDirForString((String)args[0]));
                        return new Object[]{logic != null ? logic.getTemperature() : 0};
                    } else {
                        throw new IllegalArgumentException("getTemperature method requires 0 or 1 argument (direction: up, down, east, west, north, south!");
                    }
                }
            });
        }
    }

    @Override
    public String getType(){
        return getBlockType().getUnlocalizedName().substring(5);
    }

    @Override
    public String[] getMethodNames(){
        String[] methodNames = new String[luaMethods.size()];
        for(int i = 0; i < methodNames.length; i++) {
            methodNames[i] = luaMethods.get(i).getMethodName();
        }
        return methodNames;
    }

    public List<ILuaMethod> getLuaMethods(){
        return luaMethods;
    }

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException{
        try {
            return luaMethods.get(method).call(arguments);
        } catch(Exception e) {
            throw new LuaException(e.getMessage());
        }
    }

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public void attach(IComputerAccess computer){}

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public void detach(IComputerAccess computer){}

    @Override
    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
    public boolean equals(IPeripheral other){
        if(other == null) {
            return false;
        }
        if(this == other) {
            return true;
        }
        if(other instanceof TileEntity) {
            TileEntity tother = (TileEntity)other;
            return tother.getWorldObj().equals(worldObj) && tother.xCoord == xCoord && tother.yCoord == yCoord && tother.zCoord == zCoord;
        }

        return false;
    }
}
