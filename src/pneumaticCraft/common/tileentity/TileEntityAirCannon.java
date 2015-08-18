package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.item.EntityMinecartFurnace;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.network.LazySynced;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketPlaySound;
import pneumaticCraft.common.network.PacketSetEntityMotion;
import pneumaticCraft.common.network.PacketSpawnParticle;
import pneumaticCraft.common.thirdparty.computercraft.LuaConstant;
import pneumaticCraft.common.thirdparty.computercraft.LuaMethod;
import pneumaticCraft.common.util.IOHelper;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Sounds;
import pneumaticCraft.lib.TileEntityConstants;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class TileEntityAirCannon extends TileEntityPneumaticBase implements ISidedInventory, IInventory,
        IMinWorkingPressure, IRedstoneControl{

    private ItemStack[] inventory;
    private final Random rand = new Random();
    @DescSynced
    @LazySynced
    public float rotationAngle;
    @DescSynced
    @LazySynced
    public float heightAngle;
    @DescSynced
    public float targetRotationAngle;
    @DescSynced
    public float targetHeightAngle;
    @GuiSynced
    public boolean doneTurning = false;
    private boolean redstonePowered = false;
    @GuiSynced
    public int gpsX;
    @GuiSynced
    public int gpsY;
    @GuiSynced
    public int gpsZ;
    @GuiSynced
    public boolean coordWithinReach;
    @GuiSynced
    public int redstoneMode;
    private int oldRangeUpgrades;
    private boolean externalControl;//used in the CC API, to disallow the Cannon to update its angles when things like range upgrades / GPS Tool have changed.
    private boolean entityUpgradeInserted, dispenserUpgradeInserted;
    private final List<EntityItem> trackedItems = new ArrayList<EntityItem>();//Items that are being checked to be hoppering into inventories.
    private Set<UUID> trackedItemIds;
    private ChunkPosition lastInsertingInventory; //Last coordinate where the item went into the inventory (as a result of the Block Tracker upgrade).
    private ForgeDirection lastInsertingInventorySide;
    @GuiSynced
    public boolean insertingInventoryHasSpace = true;

    private final int INVENTORY_SIZE = 6;
    public final int CANNON_SLOT = 0;
    public final int GPS_SLOT = 1;
    public final int UPGRADE_SLOT_1 = 2;
    public final int UPGRADE_SLOT_2 = 3;
    public final int UPGRADE_SLOT_3 = 4;
    public final int UPGRADE_SLOT_4 = 5;

    public TileEntityAirCannon(){
        super(PneumaticValues.DANGER_PRESSURE_AIR_CANNON, PneumaticValues.MAX_PRESSURE_AIR_CANNON, PneumaticValues.VOLUME_AIR_CANNON);
        inventory = new ItemStack[INVENTORY_SIZE];
        setUpgradeSlots(new int[]{UPGRADE_SLOT_1, UPGRADE_SLOT_2, UPGRADE_SLOT_3, UPGRADE_SLOT_4});
    }

    @Override
    public void updateEntity(){
        // GPS Tool read
        if(inventory[1] != null && inventory[1].getItem() == Itemss.GPSTool && !externalControl) {
            if(inventory[1].stackTagCompound != null) {

                NBTTagCompound gpsTag = inventory[1].stackTagCompound;
                int destinationX = gpsTag.getInteger("x");
                int destinationY = gpsTag.getInteger("y");
                int destinationZ = gpsTag.getInteger("z");

                if(destinationX != gpsX || destinationY != gpsY || destinationZ != gpsZ) {

                    gpsX = destinationX;
                    gpsY = destinationY;
                    gpsZ = destinationZ;
                    updateDestination();
                }
            }
        }

        int curRangeUpgrades = Math.min(8, getUpgrades(ItemMachineUpgrade.UPGRADE_RANGE, getUpgradeSlots()));
        if(curRangeUpgrades != oldRangeUpgrades) {
            oldRangeUpgrades = curRangeUpgrades;
            if(!externalControl) updateDestination();
        }

        if(worldObj.getTotalWorldTime() % 40 == 0) {
            boolean isDispenserUpgradeInserted = getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) > 0;
            boolean isEntityTrackerUpgradeInserted = getUpgrades(ItemMachineUpgrade.UPGRADE_ENTITY_TRACKER) > 0;
            if(dispenserUpgradeInserted != isDispenserUpgradeInserted || entityUpgradeInserted != isEntityTrackerUpgradeInserted) {
                dispenserUpgradeInserted = isDispenserUpgradeInserted;
                entityUpgradeInserted = isEntityTrackerUpgradeInserted;
                updateDestination();
            }
        }

        // update angles
        doneTurning = true;
        float speedMultiplier = getSpeedMultiplierFromUpgrades(getUpgradeSlots());
        if(rotationAngle < targetRotationAngle) {
            if(rotationAngle < targetRotationAngle - TileEntityConstants.CANNON_SLOW_ANGLE) {
                rotationAngle += TileEntityConstants.CANNON_TURN_HIGH_SPEED * speedMultiplier;
            } else {
                rotationAngle += TileEntityConstants.CANNON_TURN_LOW_SPEED * speedMultiplier;
            }
            if(rotationAngle > targetRotationAngle) rotationAngle = targetRotationAngle;
            doneTurning = false;
        }
        if(rotationAngle > targetRotationAngle) {
            if(rotationAngle > targetRotationAngle + TileEntityConstants.CANNON_SLOW_ANGLE) {
                rotationAngle -= TileEntityConstants.CANNON_TURN_HIGH_SPEED * speedMultiplier;
            } else {
                rotationAngle -= TileEntityConstants.CANNON_TURN_LOW_SPEED * speedMultiplier;
            }
            if(rotationAngle < targetRotationAngle) rotationAngle = targetRotationAngle;
            doneTurning = false;
        }
        if(heightAngle < targetHeightAngle) {
            if(heightAngle < targetHeightAngle - TileEntityConstants.CANNON_SLOW_ANGLE) {
                heightAngle += TileEntityConstants.CANNON_TURN_HIGH_SPEED * speedMultiplier;
            } else {
                heightAngle += TileEntityConstants.CANNON_TURN_LOW_SPEED * speedMultiplier;
            }
            if(heightAngle > targetHeightAngle) heightAngle = targetHeightAngle;
            doneTurning = false;
        }
        if(heightAngle > targetHeightAngle) {
            if(heightAngle > targetHeightAngle + TileEntityConstants.CANNON_SLOW_ANGLE) {
                heightAngle -= TileEntityConstants.CANNON_TURN_HIGH_SPEED * speedMultiplier;
            } else {
                heightAngle -= TileEntityConstants.CANNON_TURN_LOW_SPEED * speedMultiplier;
            }
            if(heightAngle < targetHeightAngle) heightAngle = targetHeightAngle;
            doneTurning = false;
        }

        updateTrackedItems();

        super.updateEntity();

    }

    private void updateTrackedItems(){
        if(trackedItemIds != null) {
            trackedItems.clear();
            for(Entity entity : (List<Entity>)worldObj.loadedEntityList) {
                if(trackedItemIds.contains(entity.getUniqueID()) && entity instanceof EntityItem) {
                    trackedItems.add((EntityItem)entity);
                }
            }
            trackedItemIds = null;
        }
        Iterator<EntityItem> iterator = trackedItems.iterator();
        while(iterator.hasNext()) {
            EntityItem item = iterator.next();
            if(item.worldObj != worldObj || item.isDead) {
                iterator.remove();
            } else {
                Map<ChunkPosition, ForgeDirection> positions = new HashMap<ChunkPosition, ForgeDirection>();
                double range = 0.2;
                for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                    double posX = item.posX + d.offsetX * range;
                    double posY = item.posY + d.offsetY * range;
                    double posZ = item.posZ + d.offsetZ * range;
                    positions.put(new ChunkPosition((int)Math.floor(posX), (int)Math.floor(posY), (int)Math.floor(posZ)), d.getOpposite());
                }
                for(Entry<ChunkPosition, ForgeDirection> entry : positions.entrySet()) {
                    ChunkPosition pos = entry.getKey();
                    TileEntity te = worldObj.getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
                    IInventory inv = IOHelper.getInventoryForTE(te);
                    ItemStack remainder = IOHelper.insert(inv, item.getEntityItem(), entry.getValue().ordinal(), false);
                    if(remainder != null) {
                        item.setEntityItemStack(remainder);
                    } else {
                        item.setDead();
                        iterator.remove();
                        lastInsertingInventory = new ChunkPosition(te.xCoord, te.yCoord, te.zCoord);
                        lastInsertingInventorySide = entry.getValue();
                        break;
                    }
                }
            }
        }
    }

    // ANGLE METHODS -------------------------------------------------

    private void updateDestination(){
        doneTurning = false;
        // take dispenser upgrade in account
        double payloadFrictionY = 0.98D;// this value will differ when a
                                        // dispenser upgrade is inserted.
        double payloadFrictionX = 0.98D;
        double payloadGravity = 0.04D;
        if(getUpgrades(ItemMachineUpgrade.UPGRADE_ENTITY_TRACKER) > 0) {
            payloadFrictionY = 0.98D;
            payloadFrictionX = 0.91D;
            payloadGravity = 0.08D;
        } else if(getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE, getUpgradeSlots()) > 0 && inventory[0] != null) {// if
            // there
            // is
            // a
            // dispenser
            // upgrade
            // inserted.
            Item item = inventory[0].getItem();
            if(item == Items.potionitem || item == Items.experience_bottle || item == Items.egg || item == Items.snowball) {// EntityThrowable
                payloadFrictionY = 0.99D;
                payloadGravity = 0.03D;
            } else if(item == Items.arrow) {
                payloadFrictionY = 0.99D;
                payloadGravity = 0.05D;
            } else if(item == Items.minecart || item == Items.chest_minecart || item == Items.hopper_minecart || item == Items.tnt_minecart || item == Items.furnace_minecart) {
                payloadFrictionY = 0.95D;
            }
            // else if(itemID == Item.fireballCharge.itemID){
            // payloadGravity = 0.0D;
            // }

            // family items (throwable) which only differ in gravity.
            if(item == Items.potionitem) payloadGravity = 0.05D;
            else if(item == Items.experience_bottle) payloadGravity = 0.07D;

            payloadFrictionX = payloadFrictionY;

            // items which have different frictions for each axis.
            if(item == Items.boat) {
                payloadFrictionX = 0.99D;
                payloadFrictionY = 0.95D;
            }
            if(item == Items.spawn_egg) {
                payloadFrictionY = 0.98D;
                payloadFrictionX = 0.91D;
                payloadGravity = 0.08D;
            }
        }

        // calculate the heading.
        double deltaX = gpsX - xCoord;
        double deltaZ = gpsZ - zCoord;
        float calculatedRotationAngle;
        if(deltaX >= 0 && deltaZ < 0) {
            calculatedRotationAngle = (float)(Math.atan(Math.abs(deltaX / deltaZ)) / Math.PI * 180D);
        } else if(deltaX >= 0 && deltaZ >= 0) {
            calculatedRotationAngle = (float)(Math.atan(Math.abs(deltaZ / deltaX)) / Math.PI * 180D) + 90;
        } else if(deltaX < 0 && deltaZ >= 0) {
            calculatedRotationAngle = (float)(Math.atan(Math.abs(deltaX / deltaZ)) / Math.PI * 180D) + 180;
        } else {
            calculatedRotationAngle = (float)(Math.atan(Math.abs(deltaZ / deltaX)) / Math.PI * 180D) + 270;
        }

        // calculate the height angle.
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double deltaY = gpsY - yCoord;
        float calculatedHeightAngle = calculateBestHeightAngle(distance, deltaY, getForce(), payloadGravity, payloadFrictionX, payloadFrictionY);

        setTargetAngles(calculatedRotationAngle, calculatedHeightAngle);
    }

    private float calculateBestHeightAngle(double distance, double deltaY, float force, double payloadGravity, double payloadFrictionX, double payloadFrictionY){
        double bestAngle = 0;
        double bestDistance = Float.MAX_VALUE;
        if(payloadGravity == 0D) {
            return 90F - (float)(Math.atan(deltaY / distance) * 180F / Math.PI);
        }
        for(double i = Math.PI * 0.25D; i < Math.PI * 0.50D; i += 0.001D) {
            double motionX = Math.cos(i) * force;// calculate the x component of
                                                 // the vector
            double motionY = Math.sin(i) * force;// calculate the y component of
                                                 // the vector
            double posX = 0;
            double posY = 0;
            while(posY > deltaY || motionY > 0) { // simulate movement, until we
                                                  // reach the y-level required
                posX += motionX;
                posY += motionY;
                motionY -= payloadGravity;// gravity
                motionX *= payloadFrictionX;// friction
                motionY *= payloadFrictionY;// friction
            }
            double distanceToTarget = Math.abs(distance - posX);// take the
                                                                // distance
            if(distanceToTarget < bestDistance) {// and return the best angle.
                bestDistance = distanceToTarget;
                bestAngle = i;
            }
        }
        coordWithinReach = bestDistance < 1.5D;
        return 90F - (float)(bestAngle * 180D / Math.PI);
    }

    public synchronized void setTargetAngles(float rotationAngle, float heightAngle){
        targetRotationAngle = rotationAngle;
        targetHeightAngle = heightAngle;
        if(!worldObj.isRemote) scheduleDescriptionPacket();
    }

    // this function calculates with the parsed in X and Z angles and the force
    // the needed, and outputs the X, Y and Z velocities.
    public double[] getVelocityVector(float angleX, float angleZ, float force){
        double[] velocities = new double[3];
        velocities[0] = Math.sin((double)angleZ / 180 * Math.PI);
        velocities[1] = Math.cos((double)angleX / 180 * Math.PI);
        velocities[2] = Math.cos((double)angleZ / 180 * Math.PI) * -1;

        velocities[0] *= Math.sin((double)angleX / 180 * Math.PI);
        velocities[2] *= Math.sin((double)angleX / 180 * Math.PI);
        // calculate the total velocity vector, in relation.
        double vectorTotal = velocities[0] * velocities[0] + velocities[1] * velocities[1] + velocities[2] * velocities[2];
        vectorTotal = force / vectorTotal; // calculate the relation between the
                                           // forces to be shot, and the
                                           // calculated vector (the scale).
        for(int i = 0; i < 3; i++) {
            velocities[i] *= vectorTotal; // scale up the velocities
            // System.out.println("velocities " + i + " = " + velocities[i]);
        }
        return velocities;
    }

    public boolean hasCoordinate(){
        return gpsX != 0 || gpsY != 0 || gpsZ != 0;
    }

    // PNEUMATIC METHODS -----------------------------------------

    @Override
    protected void disperseAir(){
        super.disperseAir();
        List<Pair<ForgeDirection, IAirHandler>> teList = getConnectedPneumatics();
        if(teList.size() == 0) airLeak(ForgeDirection.getOrientation(getBlockMetadata()));
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        return ForgeDirection.getOrientation(worldObj.getBlockMetadata(xCoord, yCoord, zCoord)) == side;
    }

    public float getForce(){
        return 2F + oldRangeUpgrades;
    }

    // INVENTORY METHODS- && NBT
    // ------------------------------------------------------------

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
        return Blockss.airCannon.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){
        return 64;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        redstonePowered = tag.getBoolean("redstonePowered");
        targetRotationAngle = tag.getFloat("targetRotationAngle");
        targetHeightAngle = tag.getFloat("targetHeightAngle");
        rotationAngle = tag.getFloat("rotationAngle");
        heightAngle = tag.getFloat("heightAngle");
        gpsX = tag.getInteger("gpsX");
        gpsY = tag.getInteger("gpsY");
        gpsZ = tag.getInteger("gpsZ");
        if(tag.hasKey("fireOnRightAngle")) {
            redstoneMode = tag.getBoolean("fireOnRightAngle") ? 0 : 1; //TODO remove legacy
        } else {
            redstoneMode = tag.getByte("redstoneMode");
        }

        coordWithinReach = tag.getBoolean("targetWithinReach");
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

        trackedItemIds = new HashSet<UUID>();
        tagList = tag.getTagList("trackedItems", 10);
        for(int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound t = tagList.getCompoundTagAt(i);
            trackedItemIds.add(new UUID(t.getLong("UUIDMost"), t.getLong("UUIDLeast")));
        }

        if(tag.hasKey("inventoryX")) {
            lastInsertingInventory = new ChunkPosition(tag.getInteger("inventoryX"), tag.getInteger("inventoryY"), tag.getInteger("inventoryZ"));
            lastInsertingInventorySide = ForgeDirection.getOrientation(tag.getByte("inventorySide"));
        } else {
            lastInsertingInventory = null;
            lastInsertingInventorySide = null;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("redstonePowered", redstonePowered);
        tag.setFloat("targetRotationAngle", targetRotationAngle);
        tag.setFloat("targetHeightAngle", targetHeightAngle);
        tag.setFloat("rotationAngle", rotationAngle);
        tag.setFloat("heightAngle", heightAngle);
        tag.setInteger("gpsX", gpsX);
        tag.setInteger("gpsY", gpsY);
        tag.setInteger("gpsZ", gpsZ);
        tag.setByte("redstoneMode", (byte)redstoneMode);
        tag.setBoolean("targetWithinReach", coordWithinReach);
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

        tagList = new NBTTagList();
        for(EntityItem entity : trackedItems) {
            UUID uuid = entity.getUniqueID();
            NBTTagCompound t = new NBTTagCompound();
            t.setLong("UUIDMost", uuid.getMostSignificantBits());
            t.setLong("UUIDLeast", uuid.getLeastSignificantBits());
            tagList.appendTag(t);
        }
        tag.setTag("trackedItems", tagList);

        if(lastInsertingInventory != null) {
            tag.setInteger("inventoryX", lastInsertingInventory.chunkPosX);
            tag.setInteger("inventoryY", lastInsertingInventory.chunkPosY);
            tag.setInteger("inventoryZ", lastInsertingInventory.chunkPosZ);
            tag.setByte("inventorySide", (byte)lastInsertingInventorySide.ordinal());
        }
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack){
        if(i == GPS_SLOT && itemstack != null && itemstack.getItem() != Itemss.GPSTool) return false;
        if(i > GPS_SLOT && i <= UPGRADE_SLOT_4 && itemstack != null && itemstack.getItem() != Itemss.machineUpgrade) return false;
        return true;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int var1){
        return new int[]{1, 2, 3, 4, 5, 0};
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemstack, int side){
        return true;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemstack, int side){
        return true;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1){
        return isGuiUseableByPlayer(var1);
    }

    // REDSTONE BEHAVIOUR
    // ------------------------------------------------------------

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            if(++redstoneMode > 2) redstoneMode = 0;
            if(redstoneMode == 2 && getUpgrades(ItemMachineUpgrade.UPGRADE_BLOCK_TRACKER) == 0) redstoneMode = 0;
        }
    }

    public void onNeighbourBlockChange(int x, int y, int z, Block block){
        if(!block.isAir(worldObj, x, y, z) && worldObj.isBlockIndirectlyGettingPowered(x, y, z) && !redstonePowered && (redstoneMode != 0 || doneTurning) && (redstoneMode != 2 || inventoryCanCarry())) {
            fire();
            redstonePowered = true;
        } else if(!worldObj.isBlockIndirectlyGettingPowered(x, y, z) && redstonePowered) {
            redstonePowered = false;
        }
    }

    private boolean inventoryCanCarry(){
        insertingInventoryHasSpace = true;
        if(lastInsertingInventory == null) return true;
        if(inventory[0] == null) return true;
        TileEntity te = worldObj.getTileEntity(lastInsertingInventory.chunkPosX, lastInsertingInventory.chunkPosY, lastInsertingInventory.chunkPosZ);
        IInventory inv = IOHelper.getInventoryForTE(te);
        if(inv != null) {
            ItemStack remainder = IOHelper.insert(inv, inventory[0].copy(), lastInsertingInventorySide.ordinal(), true);
            insertingInventoryHasSpace = remainder == null;
            return insertingInventoryHasSpace;
        } else {
            lastInsertingInventory = null;
            lastInsertingInventorySide = null;
            return true;
        }
    }

    private synchronized boolean fire(){
        Entity itemShot = getCloseEntityIfUpgraded();
        if(getPressure(ForgeDirection.UNKNOWN) >= PneumaticValues.MIN_PRESSURE_AIR_CANNON && (itemShot != null || inventory[0] != null)) {
            double[] velocity = getVelocityVector(heightAngle, rotationAngle, getForce());
            addAir((int)(-500 * getForce()), ForgeDirection.UNKNOWN);
            boolean shootingInventory = false;
            if(itemShot == null) {
                shootingInventory = true;
                itemShot = getPayloadEntity();

                if(itemShot instanceof EntityItem) {
                    inventory[0] = null;
                    if(getUpgrades(ItemMachineUpgrade.UPGRADE_BLOCK_TRACKER) > 0) {
                        trackedItems.add((EntityItem)itemShot);
                    }
                } else {
                    inventory[0].stackSize--;
                    if(inventory[0].stackSize <= 0) inventory[0] = null;
                }
            } else if(itemShot instanceof EntityPlayer) {
                EntityPlayerMP entityplayermp = (EntityPlayerMP)itemShot;
                if(entityplayermp.playerNetServerHandler.func_147362_b().isChannelOpen()) {
                    entityplayermp.setPositionAndUpdate(xCoord + 0.5D, yCoord + 1.8D, zCoord + 0.5D);
                }
            }

            if(itemShot.isRiding()) {
                itemShot.mountEntity((Entity)null);
            }

            itemShot.setPosition(xCoord + 0.5D, yCoord + 1.8D, zCoord + 0.5D);
            NetworkHandler.sendToAllAround(new PacketSetEntityMotion(itemShot, velocity[0], velocity[1], velocity[2]), new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 64));

            if(itemShot instanceof EntityFireball) {
                velocity[0] *= 0.05D;
                velocity[1] *= 0.05D;
                velocity[2] *= 0.05D;
            }

            itemShot.motionX = velocity[0];
            itemShot.motionY = velocity[1];
            itemShot.motionZ = velocity[2];

            itemShot.onGround = false;
            itemShot.isCollided = false;
            itemShot.isCollidedHorizontally = false;
            itemShot.isCollidedVertically = false;
            if(itemShot instanceof EntityLivingBase) ((EntityLivingBase)itemShot).setJumping(true);

            if(shootingInventory && !worldObj.isRemote) worldObj.spawnEntityInWorld(itemShot);

            for(int i = 0; i < 10; i++) {
                double velX = velocity[0] * 0.4D + (rand.nextGaussian() - 0.5D) * 0.05D;
                double velY = velocity[1] * 0.4D + (rand.nextGaussian() - 0.5D) * 0.05D;
                double velZ = velocity[2] * 0.4D + (rand.nextGaussian() - 0.5D) * 0.05D;
                NetworkHandler.sendToAllAround(new PacketSpawnParticle("largesmoke", xCoord + 0.5D, yCoord + 0.7D, zCoord + 0.5D, velX, velY, velZ), worldObj);
            }
            NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.CANNON_SOUND, xCoord, yCoord, zCoord, 1.0F, rand.nextFloat() / 4F + 0.75F, true), worldObj);
            return true;
        } else {
            return false;
        }
    }

    // warning: no null-check for inventory slot 0
    private Entity getPayloadEntity(){

        if(getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE, getUpgradeSlots()) > 0) {
            Item item = inventory[0].getItem();
            if(item == Item.getItemFromBlock(Blocks.tnt)) {
                EntityTNTPrimed tnt = new EntityTNTPrimed(worldObj);
                tnt.fuse = 80;
                return tnt;
            } else if(item == Items.experience_bottle) return new EntityExpBottle(worldObj);
            else if(item == Items.potionitem) {
                EntityPotion potion = new EntityPotion(worldObj);
                potion.setPotionDamage(inventory[0].getItemDamage());
                return potion;
            } else if(item == Items.arrow) return new EntityArrow(worldObj);
            else if(item == Items.egg) return new EntityEgg(worldObj);
            // else if(itemID == Item.fireballCharge) return new
            // EntitySmallFireball(worldObj);
            else if(item == Items.snowball) return new EntitySnowball(worldObj);
            else if(item == Items.spawn_egg) return ItemMonsterPlacer.spawnCreature(worldObj, inventory[0].getItemDamage(), 0, 0, 0);
            else if(item == Items.minecart) return new EntityMinecartEmpty(worldObj);
            else if(item == Items.chest_minecart) return new EntityMinecartChest(worldObj);
            else if(item == Items.furnace_minecart) return new EntityMinecartFurnace(worldObj);
            else if(item == Items.hopper_minecart) return new EntityMinecartHopper(worldObj);
            else if(item == Items.tnt_minecart) return new EntityMinecartTNT(worldObj);
            else if(item == Items.boat) return new EntityBoat(worldObj);

        }
        EntityItem item = new EntityItem(worldObj);
        item.setEntityItemStack(inventory[0].copy());
        item.age = 4800; // 1200 ticks left to live, = 60s.
        item.lifespan += Math.min(getUpgrades(ItemMachineUpgrade.UPGRADE_ITEM_LIFE, getUpgradeSlots()) * 600, 4800); // add
        // 30s
        // for
        // each
        // life
        // upgrade,
        // to
        // the
        // max
        // of
        // 5
        // min.
        return item;

    }

    private Entity getCloseEntityIfUpgraded(){
        int entityUpgrades = getUpgrades(ItemMachineUpgrade.UPGRADE_ENTITY_TRACKER);
        if(entityUpgrades > 0) {
            entityUpgrades = Math.min(entityUpgrades, 5);
            List<Entity> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(xCoord - entityUpgrades, yCoord - entityUpgrades, zCoord - entityUpgrades, xCoord + 1 + entityUpgrades, yCoord + 1 + entityUpgrades, zCoord + 1 + entityUpgrades));
            Entity closestEntity = null;
            for(Entity entity : entities) {
                if(closestEntity == null || PneumaticCraftUtils.distBetween(closestEntity.posX, closestEntity.posY, closestEntity.posZ, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) > PneumaticCraftUtils.distBetween(entity.posX, entity.posY, entity.posZ, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5)) {
                    closestEntity = entity;
                }
            }
            return closestEntity;
        }
        return null;
    }

    @Override
    public boolean hasCustomInventoryName(){
        return false;
    }

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){}

    /*
     *  COMPUTERCRAFT API
     */

    @Override
    public String getType(){
        return "airCannon";
    }

    @Override
    protected void addLuaMethods(){
        super.addLuaMethods();
        luaMethods.add(new LuaConstant("getMinWorkingPressure", PneumaticValues.MIN_PRESSURE_AIR_CANNON));

        luaMethods.add(new LuaMethod("setTargetLocation"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 3) {
                    gpsX = ((Double)args[0]).intValue();
                    gpsY = ((Double)args[1]).intValue();
                    gpsZ = ((Double)args[2]).intValue();
                    updateDestination();
                    return new Object[]{coordWithinReach};
                } else {
                    throw new IllegalArgumentException("setTargetLocation requires 3 parameters (x,y,z)");
                }
            }
        });

        luaMethods.add(new LuaMethod("fire"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    return new Object[]{fire()};//returns true if the fire succeeded.
                } else {
                    throw new IllegalArgumentException("fire doesn't take any arguments!");
                }
            }
        });
        luaMethods.add(new LuaMethod("isDoneTurning"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 0) {
                    return new Object[]{doneTurning};
                } else {
                    throw new IllegalArgumentException("isDoneTurning doesn't take any arguments!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setRotationAngle"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    setTargetAngles(((Double)args[0]).floatValue(), targetHeightAngle);
                    return null;
                } else {
                    throw new IllegalArgumentException("setRotationAngle does take one argument!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setHeightAngle"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    setTargetAngles(targetRotationAngle, 90 - ((Double)args[0]).floatValue());
                    return null;
                } else {
                    throw new IllegalArgumentException("setHeightAngle does take one argument!");
                }
            }
        });

        luaMethods.add(new LuaMethod("setExternalControl"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    externalControl = (Boolean)args[0];
                    return null;
                } else {
                    throw new IllegalArgumentException("setExternalControl does take one argument!");
                }
            }
        });
    }

    @Override
    public float getMinWorkingPressure(){
        return PneumaticValues.MIN_PRESSURE_AIR_CANNON;
    }

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }
}
