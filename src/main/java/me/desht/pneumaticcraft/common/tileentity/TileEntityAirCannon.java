package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerAirCannon;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.ReceivingRedstoneMode;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.util.EntityDistanceComparator;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.ItemLaunching;
import me.desht.pneumaticcraft.common.util.fakeplayer.FakeNetHandlerPlayerServer;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class TileEntityAirCannon extends TileEntityPneumaticBase
        implements IMinWorkingPressure, IRedstoneControl<TileEntityAirCannon>, IGUIButtonSensitive, INamedContainerProvider {

    private static final String FP_NAME = "[Air Cannon]";
    private static final UUID FP_UUID = UUID.nameUUIDFromBytes(FP_NAME.getBytes());

    private static final List<RedstoneMode<TileEntityAirCannon>> REDSTONE_MODES = ImmutableList.of(
            new ReceivingRedstoneMode<>("airCannon.highSignalAndAngle", Textures.GUI_HIGH_SIGNAL_ANGLE,
                    te -> te.getCurrentRedstonePower() > 0 && te.doneTurning),
            new ReceivingRedstoneMode<>("standard.high_signal", new ItemStack(Items.REDSTONE_TORCH),
                    te -> te.getCurrentRedstonePower() > 0),
            new ReceivingRedstoneMode<>("airCannon.highAndSpace", Textures.GUI_HIGH_SIGNAL_SPACE,
                    te -> te.getCurrentRedstonePower() > 0 && te.inventoryCanCarry())
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
    private final RedstoneController<TileEntityAirCannon> rsController = new RedstoneController<>(this, REDSTONE_MODES);

    private int oldRangeUpgrades;
    private boolean externalControl;//used in the CC API, to disallow the Cannon to update its angles when things like range upgrades / GPS Tool have changed.
    private boolean entityUpgradeInserted, dispenserUpgradeInserted;
    private final List<ItemEntity> trackedItems = new ArrayList<>(); //Items that are being checked to be hoppering into inventories.
    private Set<UUID> trackedItemIds;
    private final Set<TNTEntity> trackedTNT = new HashSet<>();
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
        if (!level.isClientSide) {
            updateTrackedItems();
            updateTrackedTNT();
        }

        super.tick();

        if (!getLevel().isClientSide) {
            airHandler.setSideLeaking(hasNoConnectedAirHandlers() ? getRotation() : null);
        }
    }

    private void updateTrackedTNT() {
        Iterator<TNTEntity> iter = trackedTNT.iterator();
        while (iter.hasNext()) {
            TNTEntity e = iter.next();
            if (!e.isAlive()) {
                iter.remove();
            } else {
                if (e.tickCount > 5 && e.getDeltaMovement().lengthSqr() < 0.01) {
                    e.setFuse(0);
                }
            }
        }
    }

    private boolean checkGPSSlot() {
        ItemStack gpsStack = itemHandler.getStackInSlot(GPS_SLOT);
        if (gpsStack.getItem() instanceof IPositionProvider && !externalControl) {
            List<BlockPos> posList = ((IPositionProvider) gpsStack.getItem()).getStoredPositions(level, gpsStack);
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
            ((ServerWorld) level).getEntities()
                    .filter(entity -> trackedItemIds.contains(entity.getUUID()) && entity instanceof ItemEntity)
                    .map(entity -> (ItemEntity) entity)
                    .forEach(trackedItems::add);
            trackedItemIds = null;
        }
        Iterator<ItemEntity> iterator = trackedItems.iterator();
        while (iterator.hasNext()) {
            ItemEntity item = iterator.next();
            if (item.level != getLevel() || !item.isAlive()) {
                iterator.remove();
            } else {
                Map<BlockPos, Direction> positions = new HashMap<>();
                double range = 0.2;
                for (Direction d : Direction.values()) {
                    double posX = item.getX() + d.getStepX() * range;
                    double posY = item.getY() + d.getStepY() * range;
                    double posZ = item.getZ() + d.getStepZ() * range;
                    positions.put(new BlockPos((int) Math.floor(posX), (int) Math.floor(posY), (int) Math.floor(posZ)), d.getOpposite());
                }
                for (Entry<BlockPos, Direction> entry : positions.entrySet()) {
                    BlockPos pos = entry.getKey();
                    TileEntity te = getLevel().getBlockEntity(pos);
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
                            lastInsertingInventory = te.getBlockPos();
                            lastInsertingInventorySide = entry.getValue();
                            level.playSound(null, te.getBlockPos(), SoundEvents.ITEM_PICKUP, SoundCategory.BLOCKS, 1.0f, 1.0f);
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
        double deltaX = gpsX - getBlockPos().getX();
        double deltaZ = gpsZ - getBlockPos().getZ();
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
        double deltaY = gpsY - getBlockPos().getY();
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
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        targetRotationAngle = tag.getFloat("targetRotationAngle");
        targetHeightAngle = tag.getFloat("targetHeightAngle");
        rotationAngle = tag.getFloat("rotationAngle");
        heightAngle = tag.getFloat("heightAngle");
        gpsX = tag.getInt("gpsX");
        gpsY = tag.getInt("gpsY");
        gpsZ = tag.getInt("gpsZ");
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
            lastInsertingInventorySide = Direction.from3DDataValue(tag.getByte("inventorySide"));
        } else {
            lastInsertingInventory = null;
            lastInsertingInventorySide = null;
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.putFloat("targetRotationAngle", targetRotationAngle);
        tag.putFloat("targetHeightAngle", targetHeightAngle);
        tag.putFloat("rotationAngle", rotationAngle);
        tag.putFloat("heightAngle", heightAngle);
        tag.putInt("gpsX", gpsX);
        tag.putInt("gpsY", gpsY);
        tag.putInt("gpsZ", gpsZ);
        tag.putBoolean("targetWithinReach", coordWithinReach);
        tag.put("Items", itemHandler.serializeNBT());
        tag.putInt("forceMult", forceMult);

        ListNBT tagList = new ListNBT();
        for (ItemEntity entity : trackedItems) {
            UUID uuid = entity.getUUID();
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
            tag.putByte("inventorySide", (byte) lastInsertingInventorySide.get3DDataValue());
        }

        return tag;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerAirCannon(i, playerInventory, getBlockPos());
    }

    @Override
    public RedstoneController<TileEntityAirCannon> getRedstoneController() {
        return rsController;
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
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        if (rsController.parseRedstoneMode(tag)) {
            if (rsController.getCurrentMode() == 2 && getUpgrades(EnumUpgrade.BLOCK_TRACKER) == 0) {
                rsController.setCurrentMode(0);
            }
            return;
        }

        int oldForceMult = forceMult;
        switch (tag) {
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
    public void onNeighborBlockUpdate(BlockPos fromPos) {
        boolean wasPowered = rsController.getCurrentRedstonePower() > 0;
        super.onNeighborBlockUpdate(fromPos);
        boolean isPowered = rsController.getCurrentRedstonePower() > 0;
        if (isPowered && !wasPowered && rsController.shouldRun()) {
            fire();
        }
    }

    private boolean inventoryCanCarry() {
        insertingInventoryHasSpace = true;
        if (lastInsertingInventory == null)
            return true;
        if (itemHandler.getStackInSlot(CANNON_SLOT).isEmpty())
            return true;

        TileEntity te = getLevel().getBlockEntity(lastInsertingInventory);
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
            //noinspection SuspiciousNameCombination
            double[] velocity = getVelocityVector(heightAngle, rotationAngle, force);
            addAir((int) (-500 * force));
            boolean shootingInventory = false;
            if (launchedEntity == null) {
                shootingInventory = true;
                launchedEntity = getPayloadEntity();
                if (launchedEntity instanceof TNTEntity) {
                    ((TNTEntity) launchedEntity).setFuse(400); // long fuse, but will explode on contact
                    trackedTNT.add((TNTEntity) launchedEntity);
                }
                if (launchedEntity instanceof ItemEntity) {
                    itemHandler.setStackInSlot(CANNON_SLOT, ItemStack.EMPTY);
                    if (getUpgrades(EnumUpgrade.BLOCK_TRACKER) > 0) {
                        trackedItems.add((ItemEntity) launchedEntity);
                    }
                    ((ItemEntity) launchedEntity).setPickUpDelay(20);
                } else {
                    itemHandler.extractItem(CANNON_SLOT, 1, false);
                }
            } else if (launchedEntity instanceof PlayerEntity) {
                ServerPlayerEntity entityplayermp = (ServerPlayerEntity) launchedEntity;
                if (entityplayermp.connection.getConnection().isConnected()) {
                    // This is a nasty hack to get around "player moved wrongly!" messages, which can be caused if player movement
                    // triggers a player teleport (e.g. player moves onto pressure plate, triggers air cannon with an entity tracker).
                    // todo 1.14 reflection
                    entityplayermp.isChangingDimension = true;
                    entityplayermp.teleportTo(getBlockPos().getX() + 0.5D, getBlockPos().getY() + 1.8D, getBlockPos().getZ() + 0.5D);
                }
            }

            ItemLaunching.launchEntity(launchedEntity,
                    new Vector3d(getBlockPos().getX() + 0.5D, getBlockPos().getY() + 1.8D, getBlockPos().getZ() + 0.5D),
                    new Vector3d(velocity[0], velocity[1], velocity[2]),
                    shootingInventory);
            return true;
        } else {
            return false;
        }
    }

    private Entity getPayloadEntity() {
        Entity e = ItemLaunching.getEntityToLaunch(getLevel(), itemHandler.getStackInSlot(CANNON_SLOT), getFakePlayer(),
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
            fakePlayer = FakePlayerFactory.get((ServerWorld) getLevel(), new GameProfile(FP_UUID, FP_NAME));
            fakePlayer.connection = new FakeNetHandlerPlayerServer(ServerLifecycleHooks.getCurrentServer(), fakePlayer);
            fakePlayer.setPos(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5);
        }
        return fakePlayer;
    }



    private Entity getCloseEntityIfUpgraded() {
        int entityUpgrades = Math.min(5, getUpgrades(EnumUpgrade.ENTITY_TRACKER));
        if (entityUpgrades > 0) {
            List<LivingEntity> entities = getLevel().getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(getBlockPos()).inflate(entityUpgrades));
            if (!entities.isEmpty()) {
                entities.sort(new EntityDistanceComparator(getBlockPos()));
                return entities.get(0);
            }
        }
        return null;
    }

    @Override
    public void addLuaMethods(LuaMethodRegistry registry) {
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
    public IFormattableTextComponent getRedstoneTabTitle() {
        return xlate("pneumaticcraft.gui.tab.redstoneBehaviour.airCannon.fireUpon");
    }

}
