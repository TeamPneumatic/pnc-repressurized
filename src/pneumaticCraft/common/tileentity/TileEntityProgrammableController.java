package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;
import pneumaticCraft.api.drone.DroneConstructingEvent;
import pneumaticCraft.api.drone.IPathNavigator;
import pneumaticCraft.api.item.IProgrammable;
import pneumaticCraft.common.ai.DroneAIManager;
import pneumaticCraft.common.ai.FakePlayerItemInWorldManager;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.entity.EntityProgrammableController;
import pneumaticCraft.common.entity.living.EntityDrone.DroneFakePlayer;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.LazySynced;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSpawnParticle;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetDroneConditionEntity;
import pneumaticCraft.common.progwidgets.ProgWidgetEntityAttack;
import pneumaticCraft.common.progwidgets.ProgWidgetEntityExport;
import pneumaticCraft.common.progwidgets.ProgWidgetEntityImport;
import pneumaticCraft.common.progwidgets.ProgWidgetStandby;
import pneumaticCraft.common.progwidgets.ProgWidgetSuicide;
import pneumaticCraft.common.progwidgets.ProgWidgetTeleport;
import pneumaticCraft.common.thirdparty.computercraft.ProgWidgetCC;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Log;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityProgrammableController extends TileEntityPneumaticBase implements ISidedInventory,
        IFluidHandler, IMinWorkingPressure, IDroneBase{
    private ItemStack[] inventory;

    private final int INVENTORY_SIZE = 5;

    private final FluidTank tank = new FluidTank(16000);
    private DroneAIManager aiManager;
    private DroneFakePlayer fakePlayer;
    private ItemStack[] droneItems;
    private final Map<String, IExtendedEntityProperties> properties = new HashMap<String, IExtendedEntityProperties>();
    private List<IProgWidget> progWidgets = new ArrayList<IProgWidget>();
    private final int[] redstoneLevels = new int[6];
    private String droneName = "";
    @DescSynced
    private double targetX, targetY, targetZ;
    @DescSynced
    @LazySynced
    private double curX, curY, curZ;
    public double oldCurX, oldCurY, oldCurZ;
    private EntityProgrammableController drone;
    @DescSynced
    private int diggingX, diggingY, diggingZ;
    private int dispenserUpgrades, speedUpgrades;

    private static final Set<Class<? extends IProgWidget>> WIDGET_BLACKLIST = new HashSet<Class<? extends IProgWidget>>();

    static {
        WIDGET_BLACKLIST.add(ProgWidgetCC.class);
        WIDGET_BLACKLIST.add(ProgWidgetEntityAttack.class);
        WIDGET_BLACKLIST.add(ProgWidgetDroneConditionEntity.class);
        WIDGET_BLACKLIST.add(ProgWidgetStandby.class);
        WIDGET_BLACKLIST.add(ProgWidgetSuicide.class);
        WIDGET_BLACKLIST.add(ProgWidgetTeleport.class);
        WIDGET_BLACKLIST.add(ProgWidgetEntityExport.class);
        WIDGET_BLACKLIST.add(ProgWidgetEntityImport.class);
    }

    public TileEntityProgrammableController(){
        super(5, 7, 5000);
        inventory = new ItemStack[INVENTORY_SIZE];
        //    setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 1, 2, UPGRADE_SLOT_END});
        setUpgradeSlots(new int[]{1, 2, 3, 4});
        MinecraftForge.EVENT_BUS.post(new DroneConstructingEvent(this));
    }

    @Override
    public void updateEntity(){

        super.updateEntity();

        oldCurX = curX;
        oldCurY = curY;
        oldCurZ = curZ;
        if(PneumaticCraftUtils.distBetween(getPosition(), targetX, targetY, targetZ) <= getSpeed()) {
            curX = targetX;
            curY = targetY;
            curZ = targetZ;
        } else {
            Vec3 vec = Vec3.createVectorHelper(targetX - curX, targetY - curY, targetZ - curZ).normalize();
            curX += vec.xCoord * getSpeed();
            curY += vec.yCoord * getSpeed();
            curZ += vec.zCoord * getSpeed();
        }

        if(!worldObj.isRemote) {
            getAIManager();
            if(worldObj.getTotalWorldTime() % 40 == 0) {
                dispenserUpgrades = getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE);
                speedUpgrades = getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE);

                for(int i = getDroneSlots(); i < 36; i++) {
                    ItemStack stack = getFakePlayer().inventory.getStackInSlot(i);
                    if(stack != null) {
                        worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord + 0.5, yCoord + 1.5, zCoord + 0.5, stack));
                        getFakePlayer().inventory.setInventorySlotContents(i, null);
                    }
                }

                tank.setCapacity((dispenserUpgrades + 1) * 16000);
                if(tank.getFluidAmount() > tank.getCapacity()) {
                    tank.getFluid().amount = tank.getCapacity();
                }
            }
            for(int i = 0; i < 4; i++) {
                getFakePlayer().theItemInWorldManager.updateBlockRemoving();
            }
            if(getPressure(ForgeDirection.UNKNOWN) >= getMinWorkingPressure()) {
                if(!aiManager.isIdling()) addAir(-10, ForgeDirection.UNKNOWN);
                aiManager.onUpdateTasks();
            }
        } else {
            if(drone == null || drone.isDead) {
                drone = new EntityProgrammableController(worldObj, this);
                drone.posX = curX;
                drone.posY = curY;
                drone.posZ = curZ;
                worldObj.spawnEntityInWorld(drone);
            }
            drone.setPosition(curX, curY, curZ);
            // drone.getMoveHelper().setMoveTo(curX, curY, curZ, 0);
            /*   drone.prevPosX = oldCurX;
            drone.prevPosY = oldCurY;
            drone.prevPosZ = oldCurZ;*/
            //drone.getMoveHelper().setMoveTo(curX, curY, curZ, getSpeed());
        }
    }

    @Override
    public void onDescUpdate(){
        super.onDescUpdate();
        if(drone != null) {
            drone.setDead();
        }
    }

    private double getSpeed(){
        return Math.min(10, speedUpgrades) * 0.1 + 0.1;
    }

    private void initializeFakePlayer(){
        String playerUUID = null;
        String playerName = "Drone";
        fakePlayer = new DroneFakePlayer((WorldServer)worldObj, new GameProfile(playerUUID != null ? UUID.fromString(playerUUID) : null, playerName), new FakePlayerItemInWorldManager(worldObj, fakePlayer, this), this);
        fakePlayer.playerNetServerHandler = new NetHandlerPlayServer(MinecraftServer.getServer(), new NetworkManager(false), fakePlayer);
        fakePlayer.inventory = new InventoryPlayer(fakePlayer){
            private ItemStack oldStack;

            @Override
            public int getSizeInventory(){
                return getDroneSlots();
            }

            @Override
            public void setInventorySlotContents(int slot, ItemStack stack){
                super.setInventorySlotContents(slot, stack);
                if(slot == 0) {
                    if(oldStack != null) {
                        getFakePlayer().getAttributeMap().removeAttributeModifiers(oldStack.getAttributeModifiers());
                    }

                    if(stack != null) {
                        getFakePlayer().getAttributeMap().applyAttributeModifiers(stack.getAttributeModifiers());
                    }
                    oldStack = stack;
                }
            }
        };
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){

    }

    @Override
    public boolean hasCustomInventoryName(){
        return false;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory(){
        return inventory.length + getDroneSlots();
    }

    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int slot){
        return slot < 5 ? inventory[slot] : getFakePlayer().inventory.getStackInSlot(slot - 5);
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

        if(slot < 5) {
            inventory[slot] = itemStack;
            if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
                itemStack.stackSize = getInventoryStackLimit();
            }

            if(slot == 0) {
                if(itemStack != null && isProgrammableAndValidForDrone(this, itemStack)) {
                    progWidgets = TileEntityProgrammer.getProgWidgets(itemStack);
                    if(!worldObj.isRemote) getAIManager().setWidgets(progWidgets);
                } else {
                    progWidgets.clear();
                    targetX = xCoord + 0.5;
                    targetY = yCoord + 0.6;
                    targetZ = zCoord + 0.5;
                    boolean updateNeighbours = false;
                    for(int i = 0; i < redstoneLevels.length; i++) {
                        if(redstoneLevels[i] > 0) {
                            redstoneLevels[i] = 0;
                            updateNeighbours = true;
                        }
                    }
                    if(updateNeighbours) updateNeighbours();
                }
                getAIManager();
            }
        } else {
            getFakePlayer().inventory.setInventorySlotContents(slot - 5, itemStack);
        }
    }

    @Override
    public String getInventoryName(){

        return Blockss.programmableController.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){

        return 64;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){

        super.readFromNBT(tag);

        NBTTagList tagList = tag.getTagList("Items", 10);
        inventory = new ItemStack[INVENTORY_SIZE];
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }

        tank.readFromNBT(tag.getCompoundTag("tank"));

        NBTTagList droneItemTag = tag.getTagList("droneItems", 10);
        droneItems = new ItemStack[getDroneSlots()];
        for(int i = 0; i < droneItemTag.tagCount(); ++i) {
            NBTTagCompound tagCompound = droneItemTag.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < droneItems.length) {
                droneItems[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }

        NBTTagList extendedList = tag.getTagList("extendedProperties", 10);
        for(int i = 0; i < extendedList.tagCount(); ++i) {
            NBTTagCompound propertyTag = extendedList.getCompoundTagAt(i);
            String key = propertyTag.getString("key");
            IExtendedEntityProperties property = properties.get(key);
            if(property != null) {
                property.loadNBTData(propertyTag);
            } else {
                Log.warning("Extended entity property \"" + key + "\" doesn't exist in a Programmable Controller");
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){

        super.writeToNBT(tag);

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

        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag);
        tag.setTag("tank", tankTag);

        NBTTagList droneItems = new NBTTagList();
        for(int currentIndex = 0; currentIndex < getDroneSlots(); ++currentIndex) {
            if(getFakePlayer().inventory.getStackInSlot(currentIndex) != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)currentIndex);
                getFakePlayer().inventory.getStackInSlot(currentIndex).writeToNBT(tagCompound);
                droneItems.appendTag(tagCompound);
            }
        }
        tag.setTag("droneItems", droneItems);

        NBTTagList extendedList = new NBTTagList();
        for(Map.Entry<String, IExtendedEntityProperties> entry : properties.entrySet()) {
            NBTTagCompound propertyTag = new NBTTagCompound();
            propertyTag.setString("key", entry.getKey());
            entry.getValue().saveNBTData(propertyTag);
            extendedList.appendTag(propertyTag);
        }
        tag.setTag("extendedProperties", extendedList);
    }

    @Override
    protected void onFirstServerUpdate(){
        super.onFirstServerUpdate();
        setInventorySlotContents(0, getStackInSlot(0));
    }

    private int getDroneSlots(){
        return worldObj != null && worldObj.isRemote ? 0 : Math.min(36, 1 + dispenserUpgrades);
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack){
        return i != 0 || isProgrammableAndValidForDrone(this, itemstack);
    }

    public static boolean isProgrammableAndValidForDrone(IDroneBase drone, ItemStack programmable){
        if(programmable != null && programmable.getItem() instanceof IProgrammable && ((IProgrammable)programmable.getItem()).canProgram(programmable) && ((IProgrammable)programmable.getItem()).usesPieces(programmable)) {
            List<IProgWidget> widgets = TileEntityProgrammer.getProgWidgets(programmable);
            for(IProgWidget widget : widgets) {
                if(!drone.isProgramApplicable(widget)) return false;
            }
            return true;
        }
        return false;
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
    public int[] getAccessibleSlotsFromSide(int var1){
        if(ForgeDirection.getOrientation(var1) == ForgeDirection.UP) {
            return new int[]{0};
        } else {
            if(worldObj.isRemote) return new int[0];
            int[] mainInv = new int[getSizeInventory()];
            for(int i = 0; i < getSizeInventory(); i++) {
                mainInv[i] = i + 5;
            }
            return mainInv;
        }
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int p_102007_3_){
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_){
        return true;
    }

    @Override
    public float getMinWorkingPressure(){
        return 3;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox(){
        return INFINITE_EXTENT_AABB;
    }

    /*
     *                IDrone
     */

    @Override
    public float getPressure(ItemStack iStack){
        return getPressure(ForgeDirection.UNKNOWN);
    }

    @Override
    public void addAir(ItemStack iStack, int amount){
        addAir(amount, ForgeDirection.UNKNOWN);
    }

    @Override
    public float maxPressure(ItemStack iStack){
        return 7;
    }

    @Override
    public World getWorld(){
        return worldObj;
    }

    @Override
    public IFluidTank getTank(){
        return tank;
    }

    @Override
    public IInventory getInventory(){
        return getFakePlayer().inventory;
    }

    @Override
    public Vec3 getPosition(){
        if(curX == 0 && curY == 0 && curZ == 0) {
            curX = xCoord + 0.5;
            curY = yCoord + 0.6;
            curZ = zCoord + 0.5;
            targetX = curX;
            targetY = curY;
            targetZ = curZ;
        }
        return Vec3.createVectorHelper(curX, curY, curZ);
    }

    @Override
    public IPathNavigator getPathNavigator(){
        return new IPathNavigator(){

            @Override
            public boolean moveToXYZ(double x, double y, double z){
                int blockX = (int)Math.floor(x);
                int blockY = (int)Math.floor(y);
                int blockZ = (int)Math.floor(z);
                if(isBlockValidPathfindBlock(blockX, blockY, blockZ)) {
                    targetX = x + 0.5;
                    targetY = y - 0.3;
                    targetZ = z + 0.5;
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean moveToEntity(Entity entity){
                return moveToXYZ(entity.posX, entity.posY + 0.3, entity.posZ);
            }

            @Override
            public boolean hasNoPath(){
                return targetX == curX && targetY == curY && targetZ == curZ;
            }

            @Override
            public boolean isGoingToTeleport(){
                return false;
            }

        };
    }

    @Override
    public void sendWireframeToClient(int x, int y, int z){}

    @Override
    public EntityPlayerMP getFakePlayer(){
        if(fakePlayer == null) initializeFakePlayer();
        if(droneItems != null) {
            ItemStack[] copyDroneItems = droneItems;
            droneItems = null;
            for(int i = 0; i < copyDroneItems.length; i++) {
                fakePlayer.inventory.setInventorySlotContents(i, copyDroneItems[i]);
            }
        }
        return fakePlayer;
    }

    @Override
    public boolean isBlockValidPathfindBlock(int x, int y, int z){
        return worldObj.isAirBlock(x, y, z);
    }

    @Override
    public void dropItem(ItemStack stack){
        Vec3 pos = getPosition();
        worldObj.spawnEntityInWorld(new EntityItem(worldObj, pos.xCoord, pos.yCoord, pos.zCoord, stack));
    }

    @Override
    public void setDugBlock(int x, int y, int z){
        diggingX = x;
        diggingY = y;
        diggingZ = z;
    }

    public ChunkPosition getDugPosition(){
        return diggingX != 0 || diggingY != 0 || diggingZ != 0 ? new ChunkPosition(diggingX, diggingY, diggingZ) : null;
    }

    @Override
    public List<IProgWidget> getProgWidgets(){
        return progWidgets;
    }

    @Override
    public void setActiveProgram(IProgWidget widget){}

    @Override
    public boolean isProgramApplicable(IProgWidget widget){
        return !WIDGET_BLACKLIST.contains(widget.getClass());
    }

    @Override
    public EntityAITasks getTargetAI(){
        return null;
    }

    @Override
    public IExtendedEntityProperties getProperty(String key){
        return properties.get(key);
    }

    @Override
    public void setProperty(String key, IExtendedEntityProperties property){
        properties.put(key, property);
    }

    @Override
    public void setEmittingRedstone(ForgeDirection orientation, int emittingRedstone){
        redstoneLevels[orientation.ordinal()] = emittingRedstone;
        updateNeighbours();
    }

    public int getEmittingRedstone(int direction){
        return redstoneLevels[direction];
    }

    @Override
    public void setName(String string){
        droneName = string;
        if(drone != null) drone.setCustomNameTag(droneName);
        if(inventory[0] != null) inventory[0].setStackDisplayName(string);
    }

    @Override
    public void setCarryingEntity(Entity entity){
        Log.warning("Drone AI setting carrying entity. However a Programmable Controller can't carry entities!");
        new Throwable().printStackTrace();
    }

    @Override
    public Entity getCarryingEntity(){
        return null;
    }

    @Override
    public boolean isAIOverriden(){
        return false;
    }

    @Override
    public void onItemPickupEvent(EntityItem curPickingUpEntity, int stackSize){

    }

    /*
     * Liquid handling
     */

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill){
        return tank.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain){
        return tank.getFluid() != null && tank.getFluid().isFluidEqual(resource) ? tank.drain(resource.amount, doDrain) : null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain){
        return tank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid){
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid){
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from){
        return new FluidTankInfo[]{new FluidTankInfo(tank)};
    }

    @Override
    public void overload(){
        for(int i = 0; i < 10; i++) {
            NetworkHandler.sendToAllAround(new PacketSpawnParticle("largesmoke", xCoord + worldObj.rand.nextDouble(), yCoord + 1, zCoord + worldObj.rand.nextDouble(), 0, 0, 0), worldObj);
        }
    }

    @Override
    public DroneAIManager getAIManager(){
        if(aiManager == null && worldObj != null && !worldObj.isRemote) {
            aiManager = new DroneAIManager(this, new ArrayList<IProgWidget>());
            aiManager.setWidgets(getProgWidgets());
            aiManager.dontStopWhenEndReached();
        }
        return aiManager;
    }

    @Override
    public void updateLabel(){}

    @Override
    public void addDebugEntry(String message){

    }

    @Override
    public void addDebugEntry(String message, ChunkPosition pos){

    }
}
