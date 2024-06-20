/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.block.entity.utility;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.block.entity.*;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController.ReceivingRedstoneMode;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.entity.projectile.MicromissileEntity;
import me.desht.pneumaticcraft.common.inventory.AirCannonMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.EntityDistanceComparator;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.ItemLaunching;
import me.desht.pneumaticcraft.lib.BlockEntityConstants;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import me.desht.pneumaticcraft.mixin.accessors.ItemEntityAccess;
import me.desht.pneumaticcraft.mixin.accessors.ServerPlayerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AirCannonBlockEntity extends AbstractAirHandlingBlockEntity
        implements IMinWorkingPressure, IRedstoneControl<AirCannonBlockEntity>, IGUIButtonSensitive, MenuProvider {

    private static final String FP_NAME = "[Air Cannon]";
    private static final GameProfile FAKE_PROFILE = UUIDUtil.createOfflineProfile(FP_NAME);

    private static final List<RedstoneMode<AirCannonBlockEntity>> REDSTONE_MODES = ImmutableList.of(
            new ReceivingRedstoneMode<>("airCannon.highSignalAndAngle", Textures.GUI_HIGH_SIGNAL_ANGLE,
                    te -> te.getCurrentRedstonePower() > 0 && te.doneTurning),
            new ReceivingRedstoneMode<>("standard.high_signal", new ItemStack(Items.REDSTONE_TORCH),
                    te -> te.getCurrentRedstonePower() > 0),
            new ReceivingRedstoneMode<>("airCannon.highAndSpace", Textures.GUI_HIGH_SIGNAL_SPACE,
                    te -> te.getCurrentRedstonePower() > 0 && te.inventoryCanCarry())
    );

    private final AirCannonStackHandler itemHandler = new AirCannonStackHandler(this);

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
    private final RedstoneController<AirCannonBlockEntity> rsController = new RedstoneController<>(this, REDSTONE_MODES);

    private int oldRangeUpgrades;
    private boolean externalControl;//used in the CC API, to disallow the Cannon to update its angles when things like range upgrades / GPS Tool have changed.
    private boolean entityUpgradeInserted, dispenserUpgradeInserted;
    private final List<ItemEntity> trackedItems = new ArrayList<>(); //Items that are being checked to be hoppering into inventories.
    private Set<UUID> trackedItemIds;
    private final Set<PrimedTnt> trackedTNT = new HashSet<>();
    private BlockPos lastInsertingInventory; // Last coordinate where the item went into the inventory (as a result of the Block Tracker upgrade).
    private Direction lastInsertingInventorySide;
    @GuiSynced
    public boolean insertingInventoryHasSpace = true;
    private boolean gpsSlotChanged = true;
    private FakePlayer fakePlayer = null;

    private static final int INVENTORY_SIZE = 2;
    private static final int CANNON_SLOT = 0;
    private static final int GPS_SLOT = 1;

    public AirCannonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.AIR_CANNON.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_AIR_CANNON, 4);
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        boolean destUpdateNeeded = false;
        if (gpsSlotChanged) {
            destUpdateNeeded = checkGPSSlot();
            gpsSlotChanged = false;
        }

        int curRangeUpgrades = Math.min(8, getUpgrades(ModUpgrades.RANGE.get()));
        if (curRangeUpgrades != oldRangeUpgrades) {
            oldRangeUpgrades = curRangeUpgrades;
            if (!externalControl) destUpdateNeeded = true;
        }

        boolean isDispenserUpgradeInserted = getUpgrades(ModUpgrades.DISPENSER.get()) > 0;
        boolean isEntityTrackerUpgradeInserted = getUpgrades(ModUpgrades.ENTITY_TRACKER.get()) > 0;
        if (dispenserUpgradeInserted != isDispenserUpgradeInserted || entityUpgradeInserted != isEntityTrackerUpgradeInserted) {
            dispenserUpgradeInserted = isDispenserUpgradeInserted;
            entityUpgradeInserted = isEntityTrackerUpgradeInserted;
            destUpdateNeeded = true;
        }

        if (destUpdateNeeded) updateDestination();
        updateRotationAngles();
    }

    @Override
    public void tickServer() {
        super.tickServer();

        updateTrackedItems();
        updateTrackedTNT();

        airHandler.setSideLeaking(hasNoConnectedAirHandlers() ? getRotation() : null);
    }

    private void updateTrackedTNT() {
        Iterator<PrimedTnt> iter = trackedTNT.iterator();
        while (iter.hasNext()) {
            PrimedTnt e = iter.next();
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
            List<BlockPos> posList = ((IPositionProvider) gpsStack.getItem()).getStoredPositions(null, gpsStack);
            if (!posList.isEmpty() && posList.getFirst() != null) {
                int destinationX = posList.getFirst().getX();
                int destinationY = posList.getFirst().getY();
                int destinationZ = posList.getFirst().getZ();
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
            if (rotationAngle < targetRotationAngle - BlockEntityConstants.CANNON_SLOW_ANGLE) {
                rotationAngle += BlockEntityConstants.CANNON_TURN_HIGH_SPEED * speedMultiplier;
            } else {
                rotationAngle += BlockEntityConstants.CANNON_TURN_LOW_SPEED * speedMultiplier;
            }
            if (rotationAngle > targetRotationAngle) rotationAngle = targetRotationAngle;
            doneTurning = false;
        }
        if (rotationAngle > targetRotationAngle) {
            if (rotationAngle > targetRotationAngle + BlockEntityConstants.CANNON_SLOW_ANGLE) {
                rotationAngle -= BlockEntityConstants.CANNON_TURN_HIGH_SPEED * speedMultiplier;
            } else {
                rotationAngle -= BlockEntityConstants.CANNON_TURN_LOW_SPEED * speedMultiplier;
            }
            if (rotationAngle < targetRotationAngle) rotationAngle = targetRotationAngle;
            doneTurning = false;
        }
        if (heightAngle < targetHeightAngle) {
            if (heightAngle < targetHeightAngle - BlockEntityConstants.CANNON_SLOW_ANGLE) {
                heightAngle += BlockEntityConstants.CANNON_TURN_HIGH_SPEED * speedMultiplier;
            } else {
                heightAngle += BlockEntityConstants.CANNON_TURN_LOW_SPEED * speedMultiplier;
            }
            if (heightAngle > targetHeightAngle) heightAngle = targetHeightAngle;
            doneTurning = false;
        }
        if (heightAngle > targetHeightAngle) {
            if (heightAngle > targetHeightAngle + BlockEntityConstants.CANNON_SLOW_ANGLE) {
                heightAngle -= BlockEntityConstants.CANNON_TURN_HIGH_SPEED * speedMultiplier;
            } else {
                heightAngle -= BlockEntityConstants.CANNON_TURN_LOW_SPEED * speedMultiplier;
            }
            if (heightAngle < targetHeightAngle) heightAngle = targetHeightAngle;
            doneTurning = false;
        }
    }

    private void updateTrackedItems() {
        if (trackedItemIds != null && level instanceof ServerLevel serverLevel) {
            trackedItems.clear();
            serverLevel.getEntities().get(EntityTypeTest.forClass(ItemEntity.class), itemEntity -> {
                if (trackedItemIds.contains(itemEntity.getUUID())) {
                    trackedItems.add(itemEntity);
                }
                return AbortableIterationConsumer.Continuation.CONTINUE;
            });
            trackedItemIds = null;
        }
        Iterator<ItemEntity> iterator = trackedItems.iterator();
        while (iterator.hasNext()) {
            ItemEntity item = iterator.next();
            if (item.level() != getLevel() || !item.isAlive()) {
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
                    BlockEntity te = getLevel().getBlockEntity(pos);
                    if (te == null) continue;
                    boolean inserted = IOHelper.getInventoryForBlock(te, entry.getValue()).map(inv -> {
                        ItemStack remainder = ItemHandlerHelper.insertItem(inv, item.getItem(), false);
                        if (!remainder.isEmpty()) {
                            item.setItem(remainder);
                            insertingInventoryHasSpace = false;
                            return false;
                        } else {
                            item.discard();
                            iterator.remove();
                            lastInsertingInventory = te.getBlockPos();
                            lastInsertingInventorySide = entry.getValue();
                            nonNullLevel().playSound(null, te.getBlockPos(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);
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
        if (getUpgrades(ModUpgrades.ENTITY_TRACKER.get()) > 0) {
            payloadFrictionX = 0.91D;
            payloadGravity = 0.08D;
        } else if (getUpgrades(ModUpgrades.DISPENSER.get()) > 0 && !itemHandler.getStackInSlot(CANNON_SLOT).isEmpty()) {
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
        float calculatedRotationAngle = calculateRotationAngle(deltaX, deltaZ);

        // calculate the height angle.
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double deltaY = gpsY - getBlockPos().getY();
        float calculatedHeightAngle = calculateBestHeightAngle(distance, deltaY, getForce(), payloadGravity, payloadFrictionX, payloadFrictionY);

        setTargetAngles(calculatedRotationAngle, calculatedHeightAngle);
    }

    private static float calculateRotationAngle(double deltaX, double deltaZ) {
        float calculatedRotationAngle;
        double angleXZ = Math.atan(Math.abs(deltaX / deltaZ)) / Math.PI * 180D;
        if (deltaX >= 0 && deltaZ < 0) {
            calculatedRotationAngle = (float) angleXZ;
        } else {
            double angleZX = Math.atan(Math.abs(deltaZ / deltaX)) / Math.PI * 180D;
            if (deltaX >= 0) {
                calculatedRotationAngle = (float) angleZX + 90;
            } else if (deltaZ >= 0) {
                calculatedRotationAngle = (float) angleXZ + 180;
            } else {
                calculatedRotationAngle = (float) angleZX + 270;
            }
        }
        return calculatedRotationAngle;
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
            double motionX = Mth.cos((float) i) * force;// calculate the x component of the vector
            double motionY = Mth.sin((float) i) * force;// calculate the y component of the vector
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
        velocities[0] = Mth.sin(angleZ / 180f * (float) Math.PI);
        velocities[1] = Mth.cos(angleX / 180f * (float) Math.PI);
        velocities[2] = Mth.cos(angleZ / 180f * (float) Math.PI) * -1;
        velocities[0] *= Mth.sin(angleX / 180f * (float) Math.PI);
        velocities[2] *= Mth.sin(angleX / 180f * (float) Math.PI);

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
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        targetRotationAngle = tag.getFloat("targetRotationAngle");
        targetHeightAngle = tag.getFloat("targetHeightAngle");
        rotationAngle = tag.getFloat("rotationAngle");
        heightAngle = tag.getFloat("heightAngle");
        gpsX = tag.getInt("gpsX");
        gpsY = tag.getInt("gpsY");
        gpsZ = tag.getInt("gpsZ");
        coordWithinReach = tag.getBoolean("targetWithinReach");
        itemHandler.deserializeNBT(provider, tag.getCompound("Items"));
        forceMult = tag.getInt("forceMult");

        trackedItemIds = new HashSet<>();
        ListTag tagList = tag.getList("trackedItems", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag t = tagList.getCompound(i);
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
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putFloat("targetRotationAngle", targetRotationAngle);
        tag.putFloat("targetHeightAngle", targetHeightAngle);
        tag.putFloat("rotationAngle", rotationAngle);
        tag.putFloat("heightAngle", heightAngle);
        tag.putInt("gpsX", gpsX);
        tag.putInt("gpsY", gpsY);
        tag.putInt("gpsZ", gpsZ);
        tag.putBoolean("targetWithinReach", coordWithinReach);
        tag.put("Items", itemHandler.serializeNBT(provider));
        tag.putInt("forceMult", forceMult);

        ListTag tagList = new ListTag();
        for (ItemEntity entity : trackedItems) {
            UUID uuid = entity.getUUID();
            CompoundTag t = new CompoundTag();
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
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new AirCannonMenu(i, playerInventory, getBlockPos());
    }

    @Override
    public RedstoneController<AirCannonBlockEntity> getRedstoneController() {
        return rsController;
    }

    private class AirCannonStackHandler extends BaseItemStackHandler {
        AirCannonStackHandler(BlockEntity te) {
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
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (rsController.parseRedstoneMode(tag)) {
            if (rsController.getCurrentMode() == 2 && getUpgrades(ModUpgrades.BLOCK_TRACKER.get()) == 0) {
                rsController.setCurrentMode(0);
            }
            return;
        }

        int oldForceMult = forceMult;
        switch (tag) {
            case "--" -> forceMult = Math.max(forceMult - 10, 0);
            case "-" -> forceMult = Math.max(forceMult - 1, 0);
            case "+" -> forceMult = Math.min(forceMult + 1, 100);
            case "++" -> forceMult = Math.min(forceMult + 10, 100);
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

        BlockEntity te = nonNullLevel().getBlockEntity(lastInsertingInventory);
        return IOHelper.getInventoryForBlock(te, lastInsertingInventorySide).map(inv -> {
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
                if (launchedEntity instanceof PrimedTnt tnt) {
                    tnt.setFuse(400); // long fuse, but will explode on contact
                    trackedTNT.add(tnt);
                }
                if (launchedEntity instanceof ItemEntity itemEntity) {
                    itemHandler.setStackInSlot(CANNON_SLOT, ItemStack.EMPTY);
                    if (getUpgrades(ModUpgrades.BLOCK_TRACKER.get()) > 0) {
                        trackedItems.add(itemEntity);
                    }
                    itemEntity.setPickUpDelay(20);
                }

                // Split stack only for items that are consumed when dispensed (not micromissiles)
                else if (!(launchedEntity instanceof MicromissileEntity)) {
                    itemHandler.extractItem(CANNON_SLOT, 1, false);
                }
            } else if (launchedEntity instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.connection.isAcceptingMessages()) {
                    // This is a nasty hack to get around "player moved wrongly!" messages, which can be caused if player movement
                    // triggers a player teleport (e.g. player moves onto pressure plate, triggers air cannon with an entity tracker).
                    ((ServerPlayerAccess) serverPlayer).setIsChangingDimension(true);
                    serverPlayer.teleportTo(getBlockPos().getX() + 0.5D, getBlockPos().getY() + 1.8D, getBlockPos().getZ() + 0.5D);
                }
            }

            ItemLaunching.launchEntity(launchedEntity,
                    new Vec3(getBlockPos().getX() + 0.5D, getBlockPos().getY() + 1.8D, getBlockPos().getZ() + 0.5D),
                    new Vec3(velocity[0], velocity[1], velocity[2]),
                    shootingInventory);
            return true;
        } else {
            return false;
        }
    }

    private Entity getPayloadEntity() {
        Entity e = ItemLaunching.getEntityToLaunch(getLevel(), itemHandler.getStackInSlot(CANNON_SLOT), getFakePlayer(),
                getUpgrades(ModUpgrades.DISPENSER.get()) > 0, false);
        if (e instanceof ItemEntity itemEntity) {
            // 1200 ticks left to live = 60s
            ((ItemEntityAccess) itemEntity).setAge(4800);
            // + 30s per item life upgrade, to a max of 5 mins
            itemEntity.lifespan += Math.min(getUpgrades(ModUpgrades.ITEM_LIFE.get()) * 600, 4800);
        }
        return e;
    }

    private FakePlayer getFakePlayer() {
        if (fakePlayer == null) {
            fakePlayer = FakePlayerFactory.get((ServerLevel) getLevel(), FAKE_PROFILE);
            fakePlayer.setPos(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5);
        }
        return fakePlayer;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        fakePlayer = null;  // fake player holds a reference to the level; this is called when the level is unloaded
    }

    private Entity getCloseEntityIfUpgraded() {
        int entityUpgrades = Math.min(5, getUpgrades(ModUpgrades.ENTITY_TRACKER.get()));
        if (entityUpgrades > 0) {
            List<LivingEntity> entities = nonNullLevel().getEntitiesOfClass(LivingEntity.class, new AABB(getBlockPos()).inflate(entityUpgrades));
            if (!entities.isEmpty()) {
                entities.sort(new EntityDistanceComparator(getBlockPos()));
                return entities.getFirst();
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
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return itemHandler;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_AIR_CANNON;
    }

    @Override
    public MutableComponent getRedstoneTabTitle() {
        return xlate("pneumaticcraft.gui.tab.redstoneBehaviour.airCannon.fireUpon");
    }

}
