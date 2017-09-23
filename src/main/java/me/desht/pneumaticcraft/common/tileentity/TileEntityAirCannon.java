package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Sounds;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.Map.Entry;

public class TileEntityAirCannon extends TileEntityPneumaticBase implements IMinWorkingPressure, IRedstoneControl {
    private AirCannonStackHandler inventory;
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
    private final List<EntityItem> trackedItems = new ArrayList<>(); //Items that are being checked to be hoppering into inventories.
    private Set<UUID> trackedItemIds;
    private BlockPos lastInsertingInventory; // Last coordinate where the item went into the inventory (as a result of the Block Tracker upgrade).
    private EnumFacing lastInsertingInventorySide;
    @GuiSynced
    public boolean insertingInventoryHasSpace = true;

    private static final int INVENTORY_SIZE = 2;
    public static final int CANNON_SLOT = 0;
    public static final int GPS_SLOT = 1;

    public TileEntityAirCannon() {
        super(PneumaticValues.DANGER_PRESSURE_AIR_CANNON, PneumaticValues.MAX_PRESSURE_AIR_CANNON, PneumaticValues.VOLUME_AIR_CANNON, 4);
        inventory = new AirCannonStackHandler();
        addApplicableUpgrade(EnumUpgrade.RANGE, EnumUpgrade.SPEED, EnumUpgrade.DISPENSER, EnumUpgrade.ENTITY_TRACKER, EnumUpgrade.BLOCK_TRACKER, EnumUpgrade.ITEM_LIFE);
    }

    @Override
    public void update() {
        // GPS Tool read
        ItemStack gpsStack = inventory.getStackInSlot(GPS_SLOT);
        if (gpsStack.getItem() == Itemss.GPS_TOOL && !externalControl) {
            NBTTagCompound gpsTag = gpsStack.getTagCompound();
            if (gpsTag != null) {
                int destinationX = gpsTag.getInteger("x");
                int destinationY = gpsTag.getInteger("y");
                int destinationZ = gpsTag.getInteger("z");
                if (destinationX != gpsX || destinationY != gpsY || destinationZ != gpsZ) {
                    gpsX = destinationX;
                    gpsY = destinationY;
                    gpsZ = destinationZ;
                    updateDestination();
                }
            }
        }

        int curRangeUpgrades = Math.min(8, getUpgrades(EnumUpgrade.RANGE));
        if (curRangeUpgrades != oldRangeUpgrades) {
            oldRangeUpgrades = curRangeUpgrades;
            if (!externalControl) updateDestination();
        }

        if (getWorld().getTotalWorldTime() % 40 == 0) {
            boolean isDispenserUpgradeInserted = getUpgrades(EnumUpgrade.DISPENSER) > 0;
            boolean isEntityTrackerUpgradeInserted = getUpgrades(EnumUpgrade.ENTITY_TRACKER) > 0;
            if (dispenserUpgradeInserted != isDispenserUpgradeInserted || entityUpgradeInserted != isEntityTrackerUpgradeInserted) {
                dispenserUpgradeInserted = isDispenserUpgradeInserted;
                entityUpgradeInserted = isEntityTrackerUpgradeInserted;
                updateDestination();
            }
        }

        // update angles
        doneTurning = true;
        float speedMultiplier = getSpeedMultiplierFromUpgrades();
        if (rotationAngle < targetRotationAngle) {
            if (rotationAngle < targetRotationAngle - TileEntityConstants.CANNON_SLOW_ANGLE) {
                rotationAngle += TileEntityConstants.CANNON_TURN_HIGH_SPEED * speedMultiplier;
            } else {
                rotationAngle += TileEntityConstants.CANNON_TURN_LOW_SPEED * speedMultiplier;
            }
            if (rotationAngle > targetRotationAngle) rotationAngle = targetRotationAngle;
            doneTurning = false;
        }
        if (rotationAngle > targetRotationAngle) {
            if (rotationAngle > targetRotationAngle + TileEntityConstants.CANNON_SLOW_ANGLE) {
                rotationAngle -= TileEntityConstants.CANNON_TURN_HIGH_SPEED * speedMultiplier;
            } else {
                rotationAngle -= TileEntityConstants.CANNON_TURN_LOW_SPEED * speedMultiplier;
            }
            if (rotationAngle < targetRotationAngle) rotationAngle = targetRotationAngle;
            doneTurning = false;
        }
        if (heightAngle < targetHeightAngle) {
            if (heightAngle < targetHeightAngle - TileEntityConstants.CANNON_SLOW_ANGLE) {
                heightAngle += TileEntityConstants.CANNON_TURN_HIGH_SPEED * speedMultiplier;
            } else {
                heightAngle += TileEntityConstants.CANNON_TURN_LOW_SPEED * speedMultiplier;
            }
            if (heightAngle > targetHeightAngle) heightAngle = targetHeightAngle;
            doneTurning = false;
        }
        if (heightAngle > targetHeightAngle) {
            if (heightAngle > targetHeightAngle + TileEntityConstants.CANNON_SLOW_ANGLE) {
                heightAngle -= TileEntityConstants.CANNON_TURN_HIGH_SPEED * speedMultiplier;
            } else {
                heightAngle -= TileEntityConstants.CANNON_TURN_LOW_SPEED * speedMultiplier;
            }
            if (heightAngle < targetHeightAngle) heightAngle = targetHeightAngle;
            doneTurning = false;
        }

        updateTrackedItems();

        super.update();

        if (!getWorld().isRemote) {
            List<Pair<EnumFacing, IAirHandler>> teList = getAirHandler(null).getConnectedPneumatics();
            if (teList.size() == 0) getAirHandler(null).airLeak(getRotation());
        }
    }

    private void updateTrackedItems() {
        if (trackedItemIds != null) {
            trackedItems.clear();
            for (Entity entity : getWorld().loadedEntityList) {
                if (trackedItemIds.contains(entity.getUniqueID()) && entity instanceof EntityItem) {
                    trackedItems.add((EntityItem) entity);
                }
            }
            trackedItemIds = null;
        }
        Iterator<EntityItem> iterator = trackedItems.iterator();
        while (iterator.hasNext()) {
            EntityItem item = iterator.next();
            if (item.world != getWorld() || item.isDead) {
                iterator.remove();
            } else {
                Map<BlockPos, EnumFacing> positions = new HashMap<>();
                double range = 0.2;
                for (EnumFacing d : EnumFacing.VALUES) {
                    double posX = item.posX + d.getFrontOffsetX() * range;
                    double posY = item.posY + d.getFrontOffsetY() * range;
                    double posZ = item.posZ + d.getFrontOffsetZ() * range;
                    positions   .put(new BlockPos((int) Math.floor(posX), (int) Math.floor(posY), (int) Math.floor(posZ)), d.getOpposite());
                }
                for (Entry<BlockPos, EnumFacing> entry : positions.entrySet()) {
                    BlockPos pos = entry.getKey();
                    TileEntity te = getWorld().getTileEntity(pos);
                    if (te == null) continue;
                    IItemHandler inv = IOHelper.getInventoryForTE(te, entry.getValue());
                    ItemStack remainder = ItemHandlerHelper.insertItem(inv, item.getItem(), false);
                    if (!remainder.isEmpty()) {
                        item.setItem(remainder);
                    } else {
                        item.setDead();
                        iterator.remove();
                        lastInsertingInventory = te.getPos();
                        lastInsertingInventorySide = entry.getValue();
                        break;
                    }
                }
            }
        }
    }

    // ANGLE METHODS -------------------------------------------------

    private void updateDestination() {
        doneTurning = false;
        // take dispenser upgrade in account
        double payloadFrictionY = 0.98D; // this value will differ when a dispenser upgrade is inserted.
        double payloadFrictionX = 0.98D;
        double payloadGravity = 0.04D;
        if (getUpgrades(EnumUpgrade.ENTITY_TRACKER) > 0) {
            payloadFrictionY = 0.98D;
            payloadFrictionX = 0.91D;
            payloadGravity = 0.08D;
        } else if (getUpgrades(EnumUpgrade.DISPENSER) > 0 && !inventory.getStackInSlot(CANNON_SLOT).isEmpty()) {
            Item item = inventory.getStackInSlot(CANNON_SLOT).getItem();
            if (item == Items.POTIONITEM || item == Items.EXPERIENCE_BOTTLE || item == Items.EGG || item == Items.SNOWBALL) {// EntityThrowable
                payloadFrictionY = 0.99D;
                payloadGravity = 0.03D;
            } else if (item == Items.ARROW) {
                payloadFrictionY = 0.99D;
                payloadGravity = 0.05D;
            } else if (item == Items.MINECART || item == Items.CHEST_MINECART || item == Items.HOPPER_MINECART || item == Items.TNT_MINECART || item == Items.FURNACE_MINECART) {
                payloadFrictionY = 0.95D;
            }
            // else if(itemID == Item.fireballCharge.itemID){
            // payloadGravity = 0.0D;
            // }

            // family items (throwable) which only differ in gravity.
            if (item == Items.POTIONITEM) {
                payloadGravity = 0.05D;
            } else if (item == Items.EXPERIENCE_BOTTLE) {
                payloadGravity = 0.07D;
            }

            payloadFrictionX = payloadFrictionY;

            // items which have different frictions for each axis.
            if (item == Items.BOAT) {
                payloadFrictionX = 0.99D;
                payloadFrictionY = 0.95D;
            }
            if (item == Items.SPAWN_EGG) {
                payloadFrictionY = 0.98D;
                payloadFrictionX = 0.91D;
                payloadGravity = 0.08D;
            }
        }

        // calculate the heading.
        double deltaX = gpsX - getPos().getX();
        double deltaZ = gpsZ - getPos().getZ();
        float calculatedRotationAngle;
        if (deltaX >= 0 && deltaZ < 0) {
            calculatedRotationAngle = (float) (Math.atan(Math.abs(deltaX / deltaZ)) / Math.PI * 180D);
        } else if (deltaX >= 0 && deltaZ >= 0) {
            calculatedRotationAngle = (float) (Math.atan(Math.abs(deltaZ / deltaX)) / Math.PI * 180D) + 90;
        } else if (deltaX < 0 && deltaZ >= 0) {
            calculatedRotationAngle = (float) (Math.atan(Math.abs(deltaX / deltaZ)) / Math.PI * 180D) + 180;
        } else {
            calculatedRotationAngle = (float) (Math.atan(Math.abs(deltaZ / deltaX)) / Math.PI * 180D) + 270;
        }

        // calculate the height angle.
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double deltaY = gpsY - getPos().getY();
        float calculatedHeightAngle = calculateBestHeightAngle(distance, deltaY, getForce(), payloadGravity, payloadFrictionX, payloadFrictionY);

        setTargetAngles(calculatedRotationAngle, calculatedHeightAngle);
    }

    private float calculateBestHeightAngle(double distance, double deltaY, float force, double payloadGravity, double payloadFrictionX, double payloadFrictionY) {
        double bestAngle = 0;
        double bestDistance = Float.MAX_VALUE;
        if (payloadGravity == 0D) {
            return 90F - (float) (Math.atan(deltaY / distance) * 180F / Math.PI);
        }
        for (double i = Math.PI * 0.25D; i < Math.PI * 0.50D; i += 0.001D) {
            double motionX = Math.cos(i) * force;// calculate the x component of
            // the vector
            double motionY = Math.sin(i) * force;// calculate the y component of
            // the vector
            double posX = 0;
            double posY = 0;
            while (posY > deltaY || motionY > 0) { // simulate movement, until we
                // reach the y-level required
                posX += motionX;
                posY += motionY;
                motionY -= payloadGravity;// gravity
                motionX *= payloadFrictionX;// friction
                motionY *= payloadFrictionY;// friction
            }
            double distanceToTarget = Math.abs(distance - posX);// take the
            // distance
            if (distanceToTarget < bestDistance) {// and return the best angle.
                bestDistance = distanceToTarget;
                bestAngle = i;
            }
        }
        coordWithinReach = bestDistance < 1.5D;
        return 90F - (float) (bestAngle * 180D / Math.PI);
    }

    private synchronized void setTargetAngles(float rotationAngle, float heightAngle) {
        targetRotationAngle = rotationAngle;
        targetHeightAngle = heightAngle;
        if (!getWorld().isRemote) scheduleDescriptionPacket();
    }

    // this function calculates with the parsed in X and Z angles and the force
    // the needed, and outputs the X, Y and Z velocities.
    private double[] getVelocityVector(float angleX, float angleZ, float force) {
        double[] velocities = new double[3];
        velocities[0] = Math.sin((double) angleZ / 180 * Math.PI);
        velocities[1] = Math.cos((double) angleX / 180 * Math.PI);
        velocities[2] = Math.cos((double) angleZ / 180 * Math.PI) * -1;

        velocities[0] *= Math.sin((double) angleX / 180 * Math.PI);
        velocities[2] *= Math.sin((double) angleX / 180 * Math.PI);
        // calculate the total velocity vector, in relation.
        double vectorTotal = velocities[0] * velocities[0] + velocities[1] * velocities[1] + velocities[2] * velocities[2];
        vectorTotal = force / vectorTotal; // calculate the relation between the
        // forces to be shot, and the
        // calculated vector (the scale).
        for (int i = 0; i < 3; i++) {
            velocities[i] *= vectorTotal; // scale up the velocities
            // System.out.println("velocities " + i + " = " + velocities[i]);
        }
        return velocities;
    }

    public boolean hasCoordinate() {
        return gpsX != 0 || gpsY != 0 || gpsZ != 0;
    }

    // PNEUMATIC METHODS -----------------------------------------

    @Override
    public boolean isConnectedTo(EnumFacing side) {
        return getRotation() == side;
    }

    public float getForce() {
        return 2F + oldRangeUpgrades;
    }

    @Override
    public String getName() {
        return Blockss.AIR_CANNON.getUnlocalizedName();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        targetRotationAngle = tag.getFloat("targetRotationAngle");
        targetHeightAngle = tag.getFloat("targetHeightAngle");
        rotationAngle = tag.getFloat("rotationAngle");
        heightAngle = tag.getFloat("heightAngle");
        gpsX = tag.getInteger("gpsX");
        gpsY = tag.getInteger("gpsY");
        gpsZ = tag.getInteger("gpsZ");
        redstoneMode = tag.getByte("redstoneMode");
        coordWithinReach = tag.getBoolean("targetWithinReach");
        inventory = new AirCannonStackHandler();
        inventory.deserializeNBT(tag.getCompoundTag("Items"));

        trackedItemIds = new HashSet<>();
        NBTTagList tagList = tag.getTagList("trackedItems", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound t = tagList.getCompoundTagAt(i);
            trackedItemIds.add(new UUID(t.getLong("UUIDMost"), t.getLong("UUIDLeast")));
        }

        if (tag.hasKey("inventoryX")) {
            lastInsertingInventory = new BlockPos(tag.getInteger("inventoryX"), tag.getInteger("inventoryY"), tag.getInteger("inventoryZ"));
            lastInsertingInventorySide = EnumFacing.getFront(tag.getByte("inventorySide"));
        } else {
            lastInsertingInventory = null;
            lastInsertingInventorySide = null;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setFloat("targetRotationAngle", targetRotationAngle);
        tag.setFloat("targetHeightAngle", targetHeightAngle);
        tag.setFloat("rotationAngle", rotationAngle);
        tag.setFloat("heightAngle", heightAngle);
        tag.setInteger("gpsX", gpsX);
        tag.setInteger("gpsY", gpsY);
        tag.setInteger("gpsZ", gpsZ);
        tag.setByte("redstoneMode", (byte) redstoneMode);
        tag.setBoolean("targetWithinReach", coordWithinReach);
        tag.setTag("Items", inventory.serializeNBT());

        NBTTagList tagList = new NBTTagList();
        for (EntityItem entity : trackedItems) {
            UUID uuid = entity.getUniqueID();
            NBTTagCompound t = new NBTTagCompound();
            t.setLong("UUIDMost", uuid.getMostSignificantBits());
            t.setLong("UUIDLeast", uuid.getLeastSignificantBits());
            tagList.appendTag(t);
        }
        tag.setTag("trackedItems", tagList);

        if (lastInsertingInventory != null) {
            tag.setInteger("inventoryX", lastInsertingInventory.getX());
            tag.setInteger("inventoryY", lastInsertingInventory.getY());
            tag.setInteger("inventoryZ", lastInsertingInventory.getZ());
            tag.setByte("inventorySide", (byte) lastInsertingInventorySide.ordinal());
        }

        return tag;
    }

    private static class AirCannonStackHandler extends FilteredItemStackHandler {
        AirCannonStackHandler() {
            super(INVENTORY_SIZE);
        }

        @Override
        public boolean test(Integer slot, ItemStack itemStack) {
            return !(slot == GPS_SLOT && !itemStack.isEmpty() && itemStack.getItem() != Itemss.GPS_TOOL);
        }
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            if (++redstoneMode > 2) redstoneMode = 0;
            if (redstoneMode == 2 && getUpgrades(EnumUpgrade.BLOCK_TRACKER) == 0) redstoneMode = 0;
        }
    }

    @Override
    public void onNeighborBlockUpdate() {
        boolean wasPowered = poweredRedstone > 0;
        super.onNeighborBlockUpdate();
        boolean isPowered = poweredRedstone > 0;
        if (isPowered && !wasPowered && (redstoneMode != 0 || doneTurning) && (redstoneMode != 2 || inventoryCanCarry())) {
            fire();
        }
    }

    private boolean inventoryCanCarry() {
        insertingInventoryHasSpace = true;
        if (lastInsertingInventory == null)
            return true;
        if (inventory.getStackInSlot(CANNON_SLOT).isEmpty())
            return true;
        TileEntity te = getWorld().getTileEntity(lastInsertingInventory);
        IItemHandler inv = IOHelper.getInventoryForTE(te, lastInsertingInventorySide);
        if (inv != null) {
            ItemStack remainder = ItemHandlerHelper.insertItem(inv, inventory.getStackInSlot(CANNON_SLOT).copy(), true);
            insertingInventoryHasSpace = remainder.isEmpty();
            return insertingInventoryHasSpace;
        } else {
            lastInsertingInventory = null;
            lastInsertingInventorySide = null;
            return true;
        }
    }

    private synchronized boolean fire() {
        Entity itemShot = getCloseEntityIfUpgraded();
        if (getPressure() >= PneumaticValues.MIN_PRESSURE_AIR_CANNON && (itemShot != null || !inventory.getStackInSlot(CANNON_SLOT).isEmpty())) {
            double[] velocity = getVelocityVector(heightAngle, rotationAngle, getForce());
            addAir((int) (-500 * getForce()));
            boolean shootingInventory = false;
            if (itemShot == null) {
                shootingInventory = true;
                itemShot = getPayloadEntity();
                if (itemShot instanceof EntityItem) {
                    inventory.setStackInSlot(CANNON_SLOT, ItemStack.EMPTY);
                    if (getUpgrades(EnumUpgrade.BLOCK_TRACKER) > 0) {
                        trackedItems.add((EntityItem) itemShot);
                    }
                } else {
                    inventory.extractItem(CANNON_SLOT, 1, false);
                }
            } else if (itemShot instanceof EntityPlayer) {
                EntityPlayerMP entityplayermp = (EntityPlayerMP) itemShot;
                if (entityplayermp.connection.getNetworkManager().isChannelOpen()) {
                    entityplayermp.setPositionAndUpdate(getPos().getX() + 0.5D, getPos().getY() + 1.8D, getPos().getZ() + 0.5D);
                }
            }

            if (itemShot.isRiding()) {
                itemShot.dismountRidingEntity();
            }

            itemShot.setPosition(getPos().getX() + 0.5D, getPos().getY() + 1.8D, getPos().getZ() + 0.5D);
            NetworkHandler.sendToAllAround(new PacketSetEntityMotion(itemShot, velocity[0], velocity[1], velocity[2]),
                    new TargetPoint(getWorld().provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 64));

            if (itemShot instanceof EntityFireball) {
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
            if (itemShot instanceof EntityLivingBase) ((EntityLivingBase) itemShot).setJumping(true);

            if (shootingInventory && !getWorld().isRemote) getWorld().spawnEntity(itemShot);

            for (int i = 0; i < 10; i++) {
                double velX = velocity[0] * 0.4D + (rand.nextGaussian() - 0.5D) * 0.05D;
                double velY = velocity[1] * 0.4D + (rand.nextGaussian() - 0.5D) * 0.05D;
                double velZ = velocity[2] * 0.4D + (rand.nextGaussian() - 0.5D) * 0.05D;
                NetworkHandler.sendToAllAround(new PacketSpawnParticle(EnumParticleTypes.SMOKE_LARGE, getPos().getX() + 0.5D, getPos().getY() + 0.7D, getPos().getZ() + 0.5D, velX, velY, velZ), getWorld());
            }
            NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.CANNON_SOUND, SoundCategory.BLOCKS, getPos().getX(), getPos().getY(), getPos().getZ(), 1.0F, rand.nextFloat() / 4F + 0.75F, true), getWorld());
            return true;
        } else {
            return false;
        }
    }

    // warning: no null-check for inventory slot 0
    private Entity getPayloadEntity() {
        if (getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            ItemStack stack = inventory.getStackInSlot(CANNON_SLOT);
            Item item = stack.getItem();
            if (item == Item.getItemFromBlock(Blocks.TNT)) {
                EntityTNTPrimed tnt = new EntityTNTPrimed(getWorld());
                tnt.setFuse(80);
                return tnt;
            } else if (item == Items.EXPERIENCE_BOTTLE) {
                return new EntityExpBottle(getWorld());
            } else if (item == Items.POTIONITEM) {
                EntityPotion potion = new EntityPotion(getWorld());
                potion.setItem(stack);
                return potion;
            } else if (item == Items.ARROW) {
                return new EntityTippedArrow(getWorld());
            } else if (item == Items.EGG) {
                return new EntityEgg(getWorld());
            } else if(item == Items.FIRE_CHARGE)
                return new EntitySmallFireball(getWorld());
            else if (item == Items.SNOWBALL)
                return new EntitySnowball(getWorld());
            else if (item == Items.SPAWN_EGG) {
                Entity e = ItemMonsterPlacer.spawnCreature(getWorld(), ItemMonsterPlacer.getNamedIdFrom(stack), 0, 0, 0);
                if (e instanceof EntityLivingBase && stack.hasDisplayName()) {
                    e.setCustomNameTag(stack.getDisplayName());
                }
                return e;
            } else if (item == Items.MINECART)
                return new EntityMinecartEmpty(getWorld());
            else if (item == Items.CHEST_MINECART)
                return new EntityMinecartChest(getWorld());
            else if (item == Items.FURNACE_MINECART)
                return new EntityMinecartFurnace(getWorld());
            else if (item == Items.HOPPER_MINECART)
                return new EntityMinecartHopper(getWorld());
            else if (item == Items.TNT_MINECART)
                return new EntityMinecartTNT(getWorld());
            else if (item == Items.BOAT)
                return new EntityBoat(getWorld());

        }
        EntityItem item = new EntityItem(getWorld());
        item.setItem(inventory.getStackInSlot(CANNON_SLOT).copy());
        item.setAgeToCreativeDespawnTime(); // 1200 ticks left to live = 60s.
        // add 30s for life upgrade, to a max of 5 mins
        item.lifespan += Math.min(getUpgrades(EnumUpgrade.ITEM_LIFE) * 600, 4800);
        return item;

    }

    private Entity getCloseEntityIfUpgraded() {
        int entityUpgrades = getUpgrades(EnumUpgrade.ENTITY_TRACKER);
        if (entityUpgrades > 0) {
            entityUpgrades = Math.min(entityUpgrades, 5);
            List<EntityLivingBase> entities = getWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(getPos().add(-entityUpgrades, -entityUpgrades, -entityUpgrades), getPos().add(1 + entityUpgrades, 1 + entityUpgrades, 1 + entityUpgrades)));
            Entity closestEntity = null;
            for (Entity entity : entities) {
                if (closestEntity == null || PneumaticCraftUtils.distBetween(closestEntity.posX, closestEntity.posY, closestEntity.posZ, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5) > PneumaticCraftUtils.distBetween(entity.posX, entity.posY, entity.posZ, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5)) {
                    closestEntity = entity;
                }
            }
            return closestEntity;
        }
        return null;
    }

    /*
     *  COMPUTERCRAFT API
     */

//    @Override
//    public String getType() {
//        return "airCannon";
//    }
//
//    @Override
//    protected void addLuaMethods() {
//        super.addLuaMethods();
//        luaMethods.add(new LuaConstant("getMinWorkingPressure", PneumaticValues.MIN_PRESSURE_AIR_CANNON));
//
//        luaMethods.add(new LuaMethod("setTargetLocation") {
//            @Override
//            public Object[] call(Object[] args) throws Exception {
//                if (args.length == 3) {
//                    gpsX = ((Double) args[0]).intValue();
//                    gpsY = ((Double) args[1]).intValue();
//                    gpsZ = ((Double) args[2]).intValue();
//                    updateDestination();
//                    return new Object[]{coordWithinReach};
//                } else {
//                    throw new IllegalArgumentException("setTargetLocation requires 3 parameters (x,y,z)");
//                }
//            }
//        });
//
//        luaMethods.add(new LuaMethod("fire") {
//            @Override
//            public Object[] call(Object[] args) throws Exception {
//                if (args.length == 0) {
//                    return new Object[]{fire()};//returns true if the fire succeeded.
//                } else {
//                    throw new IllegalArgumentException("fire doesn't take any arguments!");
//                }
//            }
//        });
//        luaMethods.add(new LuaMethod("isDoneTurning") {
//            @Override
//            public Object[] call(Object[] args) throws Exception {
//                if (args.length == 0) {
//                    return new Object[]{doneTurning};
//                } else {
//                    throw new IllegalArgumentException("isDoneTurning doesn't take any arguments!");
//                }
//            }
//        });
//
//        luaMethods.add(new LuaMethod("setRotationAngle") {
//            @Override
//            public Object[] call(Object[] args) throws Exception {
//                if (args.length == 1) {
//                    setTargetAngles(((Double) args[0]).floatValue(), targetHeightAngle);
//                    return null;
//                } else {
//                    throw new IllegalArgumentException("setRotationAngle does take one argument!");
//                }
//            }
//        });
//
//        luaMethods.add(new LuaMethod("setHeightAngle") {
//            @Override
//            public Object[] call(Object[] args) throws Exception {
//                if (args.length == 1) {
//                    setTargetAngles(targetRotationAngle, 90 - ((Double) args[0]).floatValue());
//                    return null;
//                } else {
//                    throw new IllegalArgumentException("setHeightAngle does take one argument!");
//                }
//            }
//        });
//
//        luaMethods.add(new LuaMethod("setExternalControl") {
//            @Override
//            public Object[] call(Object[] args) throws Exception {
//                if (args.length == 1) {
//                    externalControl = (Boolean) args[0];
//                    return null;
//                } else {
//                    throw new IllegalArgumentException("setExternalControl does take one argument!");
//                }
//            }
//        });
//    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_AIR_CANNON;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }
}
