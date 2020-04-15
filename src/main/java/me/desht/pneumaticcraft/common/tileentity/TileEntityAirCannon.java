package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.entity.projectile.EntityTumblingBlock;
import me.desht.pneumaticcraft.common.inventory.ContainerAirCannon;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.fakeplayer.FakeNetHandlerPlayerServer;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.*;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

public class TileEntityAirCannon extends TileEntityPneumaticBase
        implements IMinWorkingPressure, IRedstoneControl, IGUIButtonSensitive, INamedContainerProvider {

    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "gui.tab.redstoneBehaviour.airCannon.button.highSignalAndAngle",
            "gui.tab.redstoneBehaviour.button.highSignal",
            "gui.tab.redstoneBehaviour.airCannon.button.highAndSpace"
    );

    private final AirCannonStackHandler itemHandler = new AirCannonStackHandler(this);
    private final LazyOptional<IItemHandler> inventory = LazyOptional.of(() -> itemHandler);

    @DescSynced
    @LazySynced
    public float rotationAngle;
    @DescSynced
    @LazySynced
    public float heightAngle;
    @GuiSynced
    public int forceMult = 100; // percentage force multiplier
    @DescSynced
    private float targetRotationAngle;
    @DescSynced
    private float targetHeightAngle;
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
    private int redstoneMode;
    private int oldRangeUpgrades;
    private boolean externalControl;//used in the CC API, to disallow the Cannon to update its angles when things like range upgrades / GPS Tool have changed.
    private boolean entityUpgradeInserted, dispenserUpgradeInserted;
    private final List<ItemEntity> trackedItems = new ArrayList<>(); //Items that are being checked to be hoppering into inventories.
    private Set<UUID> trackedItemIds;
    private BlockPos lastInsertingInventory; // Last coordinate where the item went into the inventory (as a result of the Block Tracker upgrade).
    private Direction lastInsertingInventorySide;
    @GuiSynced
    public boolean insertingInventoryHasSpace = true;
    private boolean gpsSlotChanged = true;
    private FakePlayer fakePlayer = null;

    private static final int INVENTORY_SIZE = 2;
    private static final int CANNON_SLOT = 0;
    private static final int GPS_SLOT = 1;

    public TileEntityAirCannon() {
        super(ModTileEntities.AIR_CANNON.get(), PneumaticValues.DANGER_PRESSURE_AIR_CANNON, PneumaticValues.MAX_PRESSURE_AIR_CANNON, PneumaticValues.VOLUME_AIR_CANNON, 4);
    }

    @Override
    public void tick() {
        boolean destUpdateNeeded = false;

        if (gpsSlotChanged) {
            destUpdateNeeded = checkGPSSlot();
            gpsSlotChanged = false;
        }

        int curRangeUpgrades = Math.min(8, getUpgrades(EnumUpgrade.RANGE));
        if (curRangeUpgrades != oldRangeUpgrades) {
            oldRangeUpgrades = curRangeUpgrades;
            if (!externalControl) destUpdateNeeded = true;
        }

        boolean isDispenserUpgradeInserted = getUpgrades(EnumUpgrade.DISPENSER) > 0;
        boolean isEntityTrackerUpgradeInserted = getUpgrades(EnumUpgrade.ENTITY_TRACKER) > 0;
        if (dispenserUpgradeInserted != isDispenserUpgradeInserted || entityUpgradeInserted != isEntityTrackerUpgradeInserted) {
            dispenserUpgradeInserted = isDispenserUpgradeInserted;
            entityUpgradeInserted = isEntityTrackerUpgradeInserted;
            destUpdateNeeded = true;
        }

        if (destUpdateNeeded) updateDestination();
        updateRotationAngles();
        if (!world.isRemote) {
            updateTrackedItems();
        }

        super.tick();

        if (!getWorld().isRemote && isLeaking()) {
            airHandler.airLeak(this, getRotation());
        }
    }

    private boolean checkGPSSlot() {
        ItemStack gpsStack = itemHandler.getStackInSlot(GPS_SLOT);
        if (gpsStack.getItem() instanceof IPositionProvider && !externalControl) {
            List<BlockPos> posList = ((IPositionProvider) gpsStack.getItem()).getStoredPositions(world, gpsStack);
            if (!posList.isEmpty() && posList.get(0) != null) {
                int destinationX = posList.get(0).getX();
                int destinationY = posList.get(0).getY();
                int destinationZ = posList.get(0).getZ();
                if (destinationX != gpsX || destinationY != gpsY || destinationZ != gpsZ) {
                    gpsX = destinationX;
                    gpsY = destinationY;
                    gpsZ = destinationZ;
                    return true;
                }
            }
        }
        return false;
    }

    private void updateRotationAngles() {
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
    }

    private void updateTrackedItems() {
        if (trackedItemIds != null) {
            trackedItems.clear();
            ((ServerWorld) world).getEntities()
                    .filter(entity -> trackedItemIds.contains(entity.getUniqueID()) && entity instanceof ItemEntity)
                    .map(entity -> (ItemEntity) entity)
                    .forEach(trackedItems::add);
            trackedItemIds = null;
        }
        Iterator<ItemEntity> iterator = trackedItems.iterator();
        while (iterator.hasNext()) {
            ItemEntity item = iterator.next();
            if (item.world != getWorld() || !item.isAlive()) {
                iterator.remove();
            } else {
                Map<BlockPos, Direction> positions = new HashMap<>();
                double range = 0.2;
                for (Direction d : Direction.values()) {
                    double posX = item.getPosX() + d.getXOffset() * range;
                    double posY = item.getPosY() + d.getYOffset() * range;
                    double posZ = item.getPosZ() + d.getZOffset() * range;
                    positions.put(new BlockPos((int) Math.floor(posX), (int) Math.floor(posY), (int) Math.floor(posZ)), d.getOpposite());
                }
                for (Entry<BlockPos, Direction> entry : positions.entrySet()) {
                    BlockPos pos = entry.getKey();
                    TileEntity te = getWorld().getTileEntity(pos);
                    if (te == null) continue;
                    boolean inserted = IOHelper.getInventoryForTE(te, entry.getValue()).map(inv -> {
                        ItemStack remainder = ItemHandlerHelper.insertItem(inv, item.getItem(), false);
                        if (!remainder.isEmpty()) {
                            item.setItem(remainder);
                            insertingInventoryHasSpace = false;
                            return false;
                        } else {
                            item.remove();
                            iterator.remove();
                            lastInsertingInventory = te.getPos();
                            lastInsertingInventorySide = entry.getValue();
                            world.playSound(null, te.getPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1.0f, 1.0f);
                            return true;
                        }
                    }).orElse(false);
                    if (inserted) break;
                }
            }
        }
    }

    // ANGLE METHODS -------------------------------------------------

    @SuppressWarnings("SuspiciousNameCombination")
    private void updateDestination() {
        doneTurning = false;
        // take dispenser upgrade in account
        double payloadFrictionY = 0.98D; // this value will differ when a dispenser upgrade is inserted.
        double payloadFrictionX = 0.98D;
        double payloadGravity = 0.04D;
        if (getUpgrades(EnumUpgrade.ENTITY_TRACKER) > 0) {
            payloadFrictionX = 0.91D;
            payloadGravity = 0.08D;
        } else if (getUpgrades(EnumUpgrade.DISPENSER) > 0 && !itemHandler.getStackInSlot(CANNON_SLOT).isEmpty()) {
            Item item = itemHandler.getStackInSlot(CANNON_SLOT).getItem();
            if (item == Items.POTION || item == Items.EXPERIENCE_BOTTLE || item == Items.EGG || item == Items.SNOWBALL) {// EntityThrowable
                payloadFrictionY = 0.99D;
                payloadGravity = 0.03D;
            } else if (item == Items.ARROW) {
                payloadFrictionY = 0.99D;
                payloadGravity = 0.05D;
            } else if (item == Items.MINECART || item == Items.CHEST_MINECART || item == Items.HOPPER_MINECART || item == Items.TNT_MINECART || item == Items.FURNACE_MINECART) {
                payloadFrictionY = 0.95D;
            } else if (item == Items.FIRE_CHARGE) {
                payloadGravity = 0;
            }

            // family items (throwable) which only differ in gravity.
            if (item == Items.POTION) {
                payloadGravity = 0.05D;
            } else if (item == Items.EXPERIENCE_BOTTLE) {
                payloadGravity = 0.07D;
            }

            payloadFrictionX = payloadFrictionY;

            // items which have different frictions for each axis.
            if (item instanceof BoatItem) {
                payloadFrictionX = 0.99D;
                payloadFrictionY = 0.95D;
            }
            if (item instanceof SpawnEggItem) {
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
        // simulate the trajectory for angles from 45 to 90 degrees,
        // returning the angle which lands the projectile closest to the target distance
//        for (double i = Math.PI * 0.25D; i < Math.PI * 0.50D; i += 0.001D) {
        for (double i = Math.PI * 0.01D; i < Math.PI * 0.5D; i += 0.01D) {
            double motionX = MathHelper.cos((float) i) * force;// calculate the x component of the vector
            double motionY = MathHelper.sin((float) i) * force;// calculate the y component of the vector
            double posX = 0;
            double posY = 0;
            while (posY > deltaY || motionY > 0) { // simulate movement, until we reach the y-level required
                posX += motionX;
                posY += motionY;
                motionY -= payloadGravity;
                motionX *= payloadFrictionX;
                motionY *= payloadFrictionY;
            }
            double distanceToTarget = Math.abs(distance - posX);
            if (distanceToTarget < bestDistance) {
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
//        if (!getWorld().isRemote) scheduleDescriptionPacket();
    }

    // this function calculates with the parsed in X and Z angles and the force
    // the needed, and outputs the X, Y and Z velocities.
    private double[] getVelocityVector(float angleX, float angleZ, float force) {
        double[] velocities = new double[3];
        velocities[0] = MathHelper.sin(angleZ / 180f * (float) Math.PI);
        velocities[1] = MathHelper.cos(angleX / 180f * (float) Math.PI);
        velocities[2] = MathHelper.cos(angleZ / 180f * (float) Math.PI) * -1;
        velocities[0] *= MathHelper.sin(angleX / 180f * (float) Math.PI);
        velocities[2] *= MathHelper.sin(angleX / 180f * (float) Math.PI);

        // calculate the total velocity vector, in relation.
        double vectorTotal = velocities[0] * velocities[0] + velocities[1] * velocities[1] + velocities[2] * velocities[2];
        // calculate the relation between the forces to be shot, and the calculated vector (the scale).
        vectorTotal = force / vectorTotal;
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
    public boolean canConnectPneumatic(Direction side) {
        return getRotation() == side;
    }

    public float getForce() {
        return ((0.5F + oldRangeUpgrades / 2f) * forceMult) / 100;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        targetRotationAngle = tag.getFloat("targetRotationAngle");
        targetHeightAngle = tag.getFloat("targetHeightAngle");
        rotationAngle = tag.getFloat("rotationAngle");
        heightAngle = tag.getFloat("heightAngle");
        gpsX = tag.getInt("gpsX");
        gpsY = tag.getInt("gpsY");
        gpsZ = tag.getInt("gpsZ");
        redstoneMode = tag.getByte("redstoneMode");
        coordWithinReach = tag.getBoolean("targetWithinReach");
        itemHandler.deserializeNBT(tag.getCompound("Items"));
        forceMult = tag.getInt("forceMult");

        trackedItemIds = new HashSet<>();
        ListNBT tagList = tag.getList("trackedItems", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundNBT t = tagList.getCompound(i);
            trackedItemIds.add(new UUID(t.getLong("UUIDMost"), t.getLong("UUIDLeast")));
        }

        if (tag.contains("inventoryX")) {
            lastInsertingInventory = new BlockPos(tag.getInt("inventoryX"), tag.getInt("inventoryY"), tag.getInt("inventoryZ"));
            lastInsertingInventorySide = Direction.byIndex(tag.getByte("inventorySide"));
        } else {
            lastInsertingInventory = null;
            lastInsertingInventorySide = null;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putFloat("targetRotationAngle", targetRotationAngle);
        tag.putFloat("targetHeightAngle", targetHeightAngle);
        tag.putFloat("rotationAngle", rotationAngle);
        tag.putFloat("heightAngle", heightAngle);
        tag.putInt("gpsX", gpsX);
        tag.putInt("gpsY", gpsY);
        tag.putInt("gpsZ", gpsZ);
        tag.putByte("redstoneMode", (byte) redstoneMode);
        tag.putBoolean("targetWithinReach", coordWithinReach);
        tag.put("Items", itemHandler.serializeNBT());
        tag.putInt("forceMult", forceMult);

        ListNBT tagList = new ListNBT();
        for (ItemEntity entity : trackedItems) {
            UUID uuid = entity.getUniqueID();
            CompoundNBT t = new CompoundNBT();
            t.putLong("UUIDMost", uuid.getMostSignificantBits());
            t.putLong("UUIDLeast", uuid.getLeastSignificantBits());
            tagList.add(t);
        }
        tag.put("trackedItems", tagList);

        if (lastInsertingInventory != null) {
            tag.putInt("inventoryX", lastInsertingInventory.getX());
            tag.putInt("inventoryY", lastInsertingInventory.getY());
            tag.putInt("inventoryZ", lastInsertingInventory.getZ());
            tag.putByte("inventorySide", (byte) lastInsertingInventorySide.ordinal());
        }

        return tag;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerAirCannon(i, playerInventory, getPos());
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    private class AirCannonStackHandler extends BaseItemStackHandler {
        AirCannonStackHandler(TileEntity te) {
            super(te, INVENTORY_SIZE);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            if (slot == GPS_SLOT) {
                return itemStack.isEmpty() || itemStack.getItem() instanceof IPositionProvider;
            } else {
                return true;
            }
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (slot == GPS_SLOT) {
                gpsSlotChanged = true;
            }
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        int oldForceMult = forceMult;
        switch (tag) {
            case IGUIButtonSensitive.REDSTONE_TAG:
                if (++redstoneMode > 2) redstoneMode = 0;
                if (redstoneMode == 2 && getUpgrades(EnumUpgrade.BLOCK_TRACKER) == 0) redstoneMode = 0;
                break;
            case "--":
                forceMult = Math.max(forceMult - 10, 0);
                break;
            case "-":
                forceMult = Math.max(forceMult - 1, 0);
                break;
            case "+":
                forceMult = Math.min(forceMult + 1, 100);
                break;
            case "++":
                forceMult = Math.min(forceMult + 10, 100);
                break;
        }
        if (forceMult != oldForceMult) updateDestination();
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
        if (itemHandler.getStackInSlot(CANNON_SLOT).isEmpty())
            return true;

        TileEntity te = getWorld().getTileEntity(lastInsertingInventory);
        return IOHelper.getInventoryForTE(te, lastInsertingInventorySide).map(inv -> {
            ItemStack remainder = ItemHandlerHelper.insertItem(inv, itemHandler.getStackInSlot(CANNON_SLOT).copy(), true);
            insertingInventoryHasSpace = remainder.isEmpty();
            return insertingInventoryHasSpace;
        }).orElseGet(() -> {
            lastInsertingInventory = null;
            lastInsertingInventorySide = null;
            return true;
        });
    }

    private synchronized boolean fire() {
        Entity launchedEntity = getCloseEntityIfUpgraded();
        if (getPressure() >= PneumaticValues.MIN_PRESSURE_AIR_CANNON && (launchedEntity != null || !itemHandler.getStackInSlot(CANNON_SLOT).isEmpty())) {
            float force = getForce();
            double[] velocity = getVelocityVector(heightAngle, rotationAngle, force);
            addAir((int) (-500 * force));
            boolean shootingInventory = false;
            if (launchedEntity == null) {
                shootingInventory = true;
                launchedEntity = getPayloadEntity();
                if (launchedEntity instanceof ItemEntity) {
                    itemHandler.setStackInSlot(CANNON_SLOT, ItemStack.EMPTY);
                    if (getUpgrades(EnumUpgrade.BLOCK_TRACKER) > 0) {
                        trackedItems.add((ItemEntity) launchedEntity);
                    }
                    ((ItemEntity) launchedEntity).setPickupDelay(20);
                } else {
                    itemHandler.extractItem(CANNON_SLOT, 1, false);
                }
            } else if (launchedEntity instanceof PlayerEntity) {
                ServerPlayerEntity entityplayermp = (ServerPlayerEntity) launchedEntity;
                if (entityplayermp.connection.getNetworkManager().isChannelOpen()) {
                    // This is a nasty hack to get around "player moved wrongly!" messages, which can be caused if player movement
                    // triggers a player teleport (e.g. player moves onto pressure plate, triggers air cannon with an entity tracker).
                    // todo 1.14 reflection
                    entityplayermp.invulnerableDimensionChange = true;
                    entityplayermp.setPositionAndUpdate(getPos().getX() + 0.5D, getPos().getY() + 1.8D, getPos().getZ() + 0.5D);
                }
            }

            launchEntity(launchedEntity,
                    new Vec3d(getPos().getX() + 0.5D, getPos().getY() + 1.8D, getPos().getZ() + 0.5D),
                    new Vec3d(velocity[0], velocity[1], velocity[2]),
                    shootingInventory);
            return true;
        } else {
            return false;
        }
    }

    private Entity getPayloadEntity() {
        Entity e = getEntityToLaunch(getWorld(), itemHandler.getStackInSlot(CANNON_SLOT), getFakePlayer(),
                getUpgrades(EnumUpgrade.DISPENSER) > 0, false);
        if (e instanceof ItemEntity) {
            // 1200 ticks left to live = 60s
            ((ItemEntity) e).age = 4800; //setAgeToCreativeDespawnTime();
            // + 30s per item life upgrade, to a max of 5 mins
            ((ItemEntity) e).lifespan += Math.min(getUpgrades(EnumUpgrade.ITEM_LIFE) * 600, 4800);
        }
        return e;
    }

    private PlayerEntity getFakePlayer() {
        if (fakePlayer == null) {
            fakePlayer = FakePlayerFactory.get((ServerWorld) getWorld(), new GameProfile(null, "[Air Cannon]"));
            fakePlayer.connection = new FakeNetHandlerPlayerServer(ServerLifecycleHooks.getCurrentServer(), fakePlayer);
            fakePlayer.setPosition(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
        }
        return fakePlayer;
    }

    public static void launchEntity(Entity launchedEntity, Vec3d initialPos, Vec3d velocity, boolean doSpawn) {
        World world = launchedEntity.getEntityWorld();

        if (launchedEntity.getRidingEntity() != null) {
            launchedEntity.stopRiding();
        }

        launchedEntity.setPosition(initialPos.x, initialPos.y, initialPos.z);
        NetworkHandler.sendToAllAround(new PacketSetEntityMotion(launchedEntity, velocity),
                new PacketDistributor.TargetPoint(initialPos.x, initialPos.y, initialPos.z, 64, world.getDimension().getType()));
        if (launchedEntity instanceof FireballEntity) {
            // fireball velocity is handled a little differently...
            FireballEntity fireball = (FireballEntity) launchedEntity;
            fireball.accelerationX = velocity.x * 0.05;
            fireball.accelerationY = velocity.y * 0.05;
            fireball.accelerationZ = velocity.z * 0.05;
        } else {
            launchedEntity.setMotion(velocity);
        }
        launchedEntity.onGround = false;
        launchedEntity.collided = false;
        launchedEntity.collidedHorizontally = false;
        launchedEntity.collidedVertically = false;

        if (doSpawn && !world.isRemote) {
            world.addEntity(launchedEntity);
        }

        for (int i = 0; i < 5; i++) {
            double velX = velocity.x * 0.4D + (world.rand.nextGaussian() - 0.5D) * 0.05D;
            double velY = velocity.y * 0.4D + (world.rand.nextGaussian() - 0.5D) * 0.05D;
            double velZ = velocity.z * 0.4D + (world.rand.nextGaussian() - 0.5D) * 0.05D;
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(AirParticleData.DENSE, initialPos.x, initialPos.y, initialPos.z, velX, velY, velZ), world);
        }
        NetworkHandler.sendToAllAround(new PacketPlaySound(ModSounds.AIR_CANNON.get(), SoundCategory.BLOCKS, initialPos.x, initialPos.y, initialPos.z, 1.0F, world.rand.nextFloat() / 4F + 0.75F, true), world);
    }

    /**
     * Get the entity to launch for a given item.
     *
     * @param world the world
     * @param stack the item stack to be fired
     * @param hasDispenser true if dispenser-like behaviour should be used
     * @param fallingBlocks true if block items should be spawned as falling block entities rather than item entities
     * @return the entity to launch
     */
    public static Entity getEntityToLaunch(World world, ItemStack stack, PlayerEntity player, boolean hasDispenser, boolean fallingBlocks) {
        Item item = stack.getItem();
        if (hasDispenser) {
            if (item == Blocks.TNT.asItem()) {
                TNTEntity tnt = new TNTEntity(world, 0, 0, 0, player);
                tnt.setFuse(80);
                return tnt;
            } else if (item == Items.EXPERIENCE_BOTTLE) {
                return new ExperienceBottleEntity(world, player);
            } else if (item instanceof PotionItem) {
                PotionEntity potionEntity = new PotionEntity(world, player);
                potionEntity.setItem(stack);
                return potionEntity;
            } else if (item instanceof ArrowItem) {
                return ((ArrowItem) item).createArrow(world, stack, player);
            } else if (item == Items.EGG) {
                return new EggEntity(world, player);
            } else if (item == Items.FIRE_CHARGE) {
                return new SmallFireballEntity(world, player, 0, 0, 0);
            } else if (item == Items.SNOWBALL) {
                return new SnowballEntity(world, player);
            } else if (item instanceof SpawnEggItem) {
                EntityType<?> type = ((SpawnEggItem) item).getType(stack.getTag());
                Entity e = type.spawn(world, stack, player, player.getPosition(), SpawnReason.SPAWN_EGG, false, false);
                if (e instanceof LivingEntity && stack.hasDisplayName()) {
                    e.setCustomName(stack.getDisplayName());
                }
                return e;
            } else if (item instanceof MinecartItem) {
                return MinecartEntity.create(world, 0, 0, 0, ((MinecartItem) item).minecartType);
            }  else if (item instanceof BoatItem) {
                return new BoatEntity(world, 0, 0, 0);
            } else if (item == Items.FIREWORK_ROCKET) {
                return new FireworkRocketEntity(world, 0, 0, 0, stack);
            }
        }
        if (fallingBlocks && item instanceof BlockItem) {
            return new EntityTumblingBlock(world, player, 0, 0, 0, stack);
        } else {
            ItemEntity e = new ItemEntity(world, 0, 0, 0, stack);
            e.setPickupDelay(20);
//            e.lifespan = 1200;
            return e;
        }
    }

    private Entity getCloseEntityIfUpgraded() {
        int entityUpgrades = getUpgrades(EnumUpgrade.ENTITY_TRACKER);
        if (entityUpgrades > 0) {
            entityUpgrades = Math.min(entityUpgrades, 5);
            List<LivingEntity> entities = getWorld().getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(getPos().add(-entityUpgrades, -entityUpgrades, -entityUpgrades), getPos().add(1 + entityUpgrades, 1 + entityUpgrades, 1 + entityUpgrades)));
            if (entities.isEmpty()) return null;
            Entity closest = entities.get(0);
            Vec3d pos = PneumaticCraftUtils.getBlockCentre(getPos());
            for (Entity entity : entities) {
                double d1 = PneumaticCraftUtils.distBetweenSq(closest.getPosX(), closest.getPosY(), closest.getPosZ(), pos.x, pos.y, pos.z);
                double d2 = PneumaticCraftUtils.distBetweenSq(entity.getPosX(), entity.getPosY(), entity.getPosZ(), pos.x, pos.y, pos.z);
                if (d1 > d2) {
                    closest = entity;
                }
            }
            return closest;
        }
        return null;
    }

    /********************************
     *  ComputerCraft API
     */
//    @Override
//    public String getType() {
//        return "airCannon";
//    }

    @Override
    protected void addLuaMethods(LuaMethodRegistry registry) {
        super.addLuaMethods(registry);

        registry.registerLuaMethod(new LuaMethod("setTargetLocation") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 3, "x, y, z");
                gpsX = ((Double) args[0]).intValue();
                gpsY = ((Double) args[1]).intValue();
                gpsZ = ((Double) args[2]).intValue();
                updateDestination();
                return new Object[]{coordWithinReach};
            }
        });

        registry.registerLuaMethod(new LuaMethod("fire") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                // returns true if the fire succeeded.
                return new Object[]{fire()};
            }
        });

        registry.registerLuaMethod(new LuaMethod("isDoneTurning") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{doneTurning};
            }
        });

        registry.registerLuaMethod(new LuaMethod("setRotationAngle") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "angle (in degrees, 0 = north)");
                setTargetAngles(((Double) args[0]).floatValue(), targetHeightAngle);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setHeightAngle") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "angle (in degrees, 0 = horizontal)");
                setTargetAngles(targetRotationAngle, 90 - ((Double) args[0]).floatValue());
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("setExternalControl") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "true/false");
                externalControl = (Boolean) args[0];
                return null;
            }
        });
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return inventory;
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return itemHandler;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_AIR_CANNON;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    protected List<String> getRedstoneButtonLabels() {
        return REDSTONE_LABELS;
    }

    @Override
    public String getRedstoneTabTitle() {
        return "gui.tab.redstoneBehaviour.airCannon.fireUpon";
    }

}
