package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import pneumaticCraft.api.drone.IDrone;
import pneumaticCraft.api.item.IProgrammable;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetDroneConditionEntity;
import pneumaticCraft.common.progwidgets.ProgWidgetEntityAttack;
import pneumaticCraft.common.progwidgets.ProgWidgetStandby;
import pneumaticCraft.common.progwidgets.ProgWidgetSuicide;
import pneumaticCraft.common.progwidgets.ProgWidgetTeleport;
import pneumaticCraft.common.thirdparty.computercraft.ProgWidgetCC;

public class TileEntityProgrammableController extends TileEntityPneumaticBase implements IInventory,
        IMinWorkingPressure, IDrone{
    private ItemStack[] inventory;

    private final int INVENTORY_SIZE = 1;

    private static final int UPGRADE_SLOT_START = 0;
    private static final int UPGRADE_SLOT_END = 3;

    private final FluidTank tank = new FluidTank(16000);
    private Entity carryingEntity;
    private List<IProgWidget> progWidgets = new ArrayList<IProgWidget>();

    private static final Set<Class<? extends IProgWidget>> WIDGET_BLACKLIST = new HashSet<Class<? extends IProgWidget>>();

    static {
        WIDGET_BLACKLIST.add(ProgWidgetCC.class);
        WIDGET_BLACKLIST.add(ProgWidgetEntityAttack.class);
        WIDGET_BLACKLIST.add(ProgWidgetDroneConditionEntity.class);
        WIDGET_BLACKLIST.add(ProgWidgetStandby.class);
        WIDGET_BLACKLIST.add(ProgWidgetSuicide.class);
        WIDGET_BLACKLIST.add(ProgWidgetTeleport.class);
    }

    public TileEntityProgrammableController(){
        super(5, 7, 5000);
        inventory = new ItemStack[INVENTORY_SIZE];
        //    setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 1, 2, UPGRADE_SLOT_END});
    }

    @Override
    public void updateEntity(){

        super.updateEntity();

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

        if(itemStack != null) {
            progWidgets = TileEntityProgrammer.getProgWidgets(itemStack);
        } else {
            progWidgets.clear();
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
    public void readFromNBT(NBTTagCompound nbtTagCompound){

        super.readFromNBT(nbtTagCompound);

        NBTTagList tagList = nbtTagCompound.getTagList("Items", 10);
        inventory = new ItemStack[4];
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound){

        super.writeToNBT(nbtTagCompound);

        NBTTagList tagList = new NBTTagList();
        for(int currentIndex = 0; currentIndex < inventory.length; ++currentIndex) {
            if(inventory[currentIndex] != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)currentIndex);
                inventory[currentIndex].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        nbtTagCompound.setTag("Items", tagList);
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack){
        return itemstack != null && itemstack.getItem() instanceof IProgrammable && ((IProgrammable)itemstack.getItem()).canProgram(itemstack) && ((IProgrammable)itemstack.getItem()).usesPieces(itemstack);
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
    public float getMinWorkingPressure(){
        return 3;
    }

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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Vec3 getPosition(){
        return Vec3.createVectorHelper(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
    }

    @Override
    public PathNavigate getNavigator(){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendWireframeToClient(int x, int y, int z){
        // TODO Auto-generated method stub

    }

    @Override
    public EntityPlayerMP getFakePlayer(){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isBlockValidPathfindBlock(int x, int y, int z){
        return true;
    }

    @Override
    public void dropItem(ItemStack stack){
        // TODO Auto-generated method stub

    }

    @Override
    public void setDugBlock(int x, int y, int z){
        // TODO Auto-generated method stub

    }

    @Override
    public List<IProgWidget> getProgWidgets(){
        return progWidgets;
    }

    @Override
    public void setActiveProgram(IProgWidget widget){
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isProgramApplicable(IProgWidget widget){
        return !WIDGET_BLACKLIST.contains(widget.getClass());
    }

    @Override
    public EntityAITasks getTargetAI(){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IExtendedEntityProperties getProperty(String key){
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setProperty(String key, IExtendedEntityProperties property){
        // TODO Auto-generated method stub

    }

    @Override
    public void setEmittingRedstone(ForgeDirection orientation, int emittingRedstone){
        // TODO Auto-generated method stub
    }

    @Override
    public double getSpeed(){
        return 0;
    }

    @Override
    public void setName(String string){
        // TODO Auto-generated method stub

    }

    @Override
    public void setCarryingEntity(Entity entity){
        carryingEntity = entity;
    }

    @Override
    public Entity getCarryingEntity(){
        return carryingEntity;
    }

    @Override
    public boolean isAIOverriden(){
        return false;
    }

    @Override
    public void onItemPickupEvent(EntityItem curPickingUpEntity, int stackSize){
        // TODO Auto-generated method stub

    }

}
