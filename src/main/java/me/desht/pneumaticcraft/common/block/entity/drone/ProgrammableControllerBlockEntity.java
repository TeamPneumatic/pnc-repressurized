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

package me.desht.pneumaticcraft.common.block.entity.drone;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.ForcedChunks;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.drone.DroneConstructingEvent;
import me.desht.pneumaticcraft.api.drone.IPathNavigator;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.api.semiblock.SemiblockEvent;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.entity.*;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.debug.DroneDebugger;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.LogisticsManager;
import me.desht.pneumaticcraft.common.drone.ProgWidgetUtils;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.drone.progwidgets.SavedDroneProgram;
import me.desht.pneumaticcraft.common.entity.drone.ProgrammableControllerEntity;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractLogisticsFrameEntity;
import me.desht.pneumaticcraft.common.inventory.ProgrammableControllerMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.registry.*;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.fakeplayer.DroneFakePlayer;
import me.desht.pneumaticcraft.common.util.fakeplayer.DroneItemHandler;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ProgrammableControllerBlockEntity extends AbstractAirHandlingBlockEntity
        implements IMinWorkingPressure, IDroneBase, ISideConfigurable, MenuProvider {
    private static final int INVENTORY_SIZE = 1;
    private static final String FALLBACK_NAME = "[ProgController]";
    private static final UUID FALLBACK_UUID = UUID.nameUUIDFromBytes(FALLBACK_NAME.getBytes());
    private static final int MAX_ENERGY = 100000;

    public static final Set<ResourceLocation> BLACKLISTED_WIDGETS = ImmutableSet.of(
            RL("computer_control"),
            RL("entity_attack"),
            RL("drone_condition_entity"),
            RL("standby"),
            RL("teleport"),
            RL("entity_export"),
            RL("entity_import")
    );

    private static final double SPEED_PER_UPGRADE = 0.05;
    private static final double BASE_SPEED = 0.15;

    private final ProgrammableItemStackHandler inventory = new ProgrammableItemStackHandler(this);

    private final FluidTank tank = new FluidTank(16000);

    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(MAX_ENERGY);

    private final DroneItemHandler droneItemHandler = new DroneItemHandler(this, 1);

    private final ControllerNavigator controllerNavigator = new ControllerNavigator();

    private ProgrammableControllerEntity drone;
    private DroneAIManager aiManager;
    private DroneFakePlayer fakePlayer;
    private final List<IProgWidget> progWidgets = new ArrayList<>();
    private final int[] redstoneLevels = new int[6];
    private final SideConfigurator<IItemHandler> itemHandlerSideConfigurator;
    private CompoundTag variablesNBT = null;  // pending variable data to add to ai manager

    @DescSynced
    private double targetX, targetY, targetZ;
    @DescSynced
    @LazySynced
    private double curX, curY, curZ;
    @DescSynced
    private int diggingX, diggingY, diggingZ;
    @DescSynced
    private int speedUpgrades;
    @DescSynced
    public boolean isIdle;
    @DescSynced
    public ItemStack heldItem = ItemStack.EMPTY;
    @GuiSynced
    public boolean shouldChargeHeldItem;
    @DescSynced
    public String label = "";
    @DescSynced
    public String ownerNameClient = "";
    @GuiSynced
    private boolean chunkloadSelf = false;
    @GuiSynced
    private boolean chunkloadWorkingChunk = false;
    @GuiSynced
    private boolean chunkloadWorkingChunk3x3 = false;
    private ChunkPos prevChunkPos = null;
    private final Set<ChunkPos> loadedChunks = new HashSet<>();

    private UUID ownerID;
    private Component ownerName;
    private boolean shouldUpdateNeighbours;
    // Although this is only used by DroneAILogistics, it is here rather than there so it can persist,
    // for performance reasons; DroneAILogistics is a short-lived object and LogisticsManager is expensive to create
    private LogisticsManager logisticsManager;
    private final DroneDebugger debugger = new DroneDebugger(this);
    @DescSynced
    private int activeWidgetIndex;

    public ProgrammableControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.PROGRAMMABLE_CONTROLLER.get(), pos, state, PressureTier.TIER_TWO, 10000, 4);

        NeoForge.EVENT_BUS.post(new DroneConstructingEvent(this));

        NeoForge.EVENT_BUS.register(this);

        itemHandlerSideConfigurator = new SideConfigurator<>("items", this);
        itemHandlerSideConfigurator.registerHandler("droneInv", new ItemStack(ModItems.DRONE.get()),
                Capabilities.ItemHandler.BLOCK, () -> droneItemHandler,
                SideConfigurator.RelativeFace.TOP, SideConfigurator.RelativeFace.FRONT, SideConfigurator.RelativeFace.BACK, SideConfigurator.RelativeFace.LEFT, SideConfigurator.RelativeFace.RIGHT);
        itemHandlerSideConfigurator.registerHandler("programmableInv", new ItemStack(ModItems.NETWORK_API.get()),
                Capabilities.ItemHandler.BLOCK, () -> inventory,
                SideConfigurator.RelativeFace.BOTTOM);
        itemHandlerSideConfigurator.setNullFaceHandler("programmableInv");
    }

    private TicketController ticketController() {
        return ForcedChunks.INSTANCE.getPcController();
    }

    @Override
    public boolean hasFluidCapability() {
        return true;
    }

    @Override
    public boolean hasEnergyCapability() {
        return true;
    }

    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction dir) {
        return tank;
    }

    @Override
    public IEnergyStorage getEnergyHandler(@Nullable Direction dir) {
        return energy;
    }

    @SubscribeEvent
    public void onSemiblockEvent(SemiblockEvent event) {
        if (!event.getWorld().isClientSide && event.getWorld() == getLevel() && event.getSemiblock() instanceof AbstractLogisticsFrameEntity) {
            logisticsManager = null;
        }
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        double speed = BASE_SPEED + speedUpgrades * SPEED_PER_UPGRADE;
        if (PneumaticCraftUtils.distBetweenSq(curX, curY, curZ, targetX, targetY, targetZ) > speed / 2d) {
            // dist-between check here avoids drone "jitter" when it's very near its target
            Vec3 vec = new Vec3(targetX - curX, targetY - curY, targetZ - curZ).normalize().scale(speed);
            curX += vec.x;
            curY += vec.y;
            curZ += vec.z;
        } else {
            curX = targetX;
            curY = targetY;
            curZ = targetZ;
        }
    }

    @Override
    public void tickClient() {
        super.tickClient();

        if ((drone == null || !drone.isAlive()) && nonNullLevel().isLoaded(BlockPos.containing(curX, curY, curZ))) {
            drone = ModEntityTypes.PROGRAMMABLE_CONTROLLER.get().create(nonNullLevel());
            if (drone != null) {
                drone.setController(this);
                drone.setPos(curX, curY, curZ);
                ClientUtils.spawnEntityClientside(drone);
            }
        } else if (drone != null) {
            // drone could still be null if in different (unloaded) chunk from controller
            drone.setPos(curX, curY, curZ);
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (shouldUpdateNeighbours) {
            updateNeighbours();
            shouldUpdateNeighbours = false;
        }

        DroneFakePlayer fp = getFakePlayer();
        for (int i = 0; i < 4; i++) {
            fp.gameMode.tick();
        }
        fp.setPos(curX, curY, curZ);
        ChunkPos newChunkPos = new ChunkPos((int)curX >> 4, (int)curZ >> 4);
        if (prevChunkPos == null || !prevChunkPos.equals(newChunkPos)) {
            handleDynamicChunkloading(newChunkPos);
        }
        prevChunkPos = newChunkPos;
        fp.tick();

        heldItem = ConfigHelper.common().drones.dronesRenderHeldItem.get() ? fp.getMainHandItem() : ItemStack.EMPTY;

        if (getPressure() >= getMinWorkingPressure()) {
            if (!isIdle) {
                addAir(-PneumaticValues.USAGE_PROGRAMMABLE_CONTROLLER);
                if (chunkloadWorkingChunk3x3) addAir(-PneumaticValues.USAGE_PROGRAMMABLE_CONTROLLER_CHUNKLOAD_WORK3);
                else if (chunkloadWorkingChunk) addAir(-PneumaticValues.USAGE_PROGRAMMABLE_CONTROLLER_CHUNKLOAD_WORK);
            }
            if (chunkloadSelf) addAir(-PneumaticValues.USAGE_PROGRAMMABLE_CONTROLLER_CHUNKLOAD_SELF);
            DroneAIManager prevActive = getActiveAIManager();
            aiManager.onUpdateTasks();
            if (getActiveAIManager() != prevActive) {
                // active AI has changed (started or stopped using External Program) - resync widget list to debugging players
                getDebugger().getDebuggingPlayers().forEach(p -> NetworkHandler.sendToPlayer(PacketSyncDroneProgWidgets.create(this), p));
            }
            maybeChargeHeldItem();
        }

        if (nonNullLevel().getGameTime() % 20 == 0) {
            debugger.updateDebuggingPlayers();
        }
    }

    private void handleDynamicChunkloading(ChunkPos newPos) {
//        Log.info("drone moved into chunk " + newPos);
        for (int cx = newPos.x - 1; cx <= newPos.x + 1; cx++) {
            for (int cz = newPos.z - 1; cz <= newPos.z + 1; cz++) {
                ChunkPos cp = new ChunkPos(cx, cz);
                if (shouldLoadChunk(cp)) loadedChunks.add(cp);
            }
        }

        Iterator<ChunkPos> iter = loadedChunks.iterator();
        while (iter.hasNext()) {
            ChunkPos cp = iter.next();
            boolean load = shouldLoadChunk(cp);
//            Log.info("chunkload " + cp + "? " + load);
            ticketController().forceChunk((ServerLevel) nonNullLevel(), worldPosition, cp.x, cp.z, load, false);
            if (!load) {
                iter.remove();
            }
        }
    }

    private boolean shouldLoadChunk(ChunkPos cp) {
        int cx = (int)curX >> 4;
        int cz = (int)curZ >> 4;
        return chunkloadSelf && cp.x == worldPosition.getX() >> 4 && cp.z == worldPosition.getZ() >> 4
                || chunkloadWorkingChunk && !isIdle && cp.x == cx && cp.z == cz
                || chunkloadWorkingChunk3x3 && !isIdle && cp.x >= cx - 1 && cp.x <= cx + 1 && cp.z >= cz - 1 && cp.z <= cz + 1;
    }

    private void maybeChargeHeldItem() {
        if (!shouldChargeHeldItem) return;

        ItemStack held = droneItemHandler.getStackInSlot(0);

        if (energy.getEnergyStored() > 100) {
            IOHelper.getEnergyStorageForItem(held).ifPresent(handler -> {
                if (handler.getMaxEnergyStored() - handler.getEnergyStored() > 250) {
                    handler.receiveEnergy(energy.extractEnergy(250, false), false);
                }
            });
        }

        PNCCapabilities.getAirHandler(held).ifPresent(handler -> {
            if (getPressure() > handler.getPressure() && handler.getPressure() < handler.maxPressure()) {
                int maxAir = (int) (handler.maxPressure() * handler.getVolume());
                int toAdd = Math.min(250, maxAir - handler.getAir());
                handler.addAir(toAdd);
                airHandler.addAir(-toAdd);
            }
        });
    }

    @Override
    public void onVariableChanged(String varname, boolean isCoordinate) {
        setChanged();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (level instanceof ServerLevel) {
            loadedChunks.forEach(cp -> ticketController().forceChunk((ServerLevel) level, worldPosition, cp.x, cp.z, false, false));
        }
        NeoForge.EVENT_BUS.unregister(this);
    }

    @Override
    public UUID getOwnerUUID() {
        if (ownerID == null) {
            ownerID = UUID.randomUUID();
            ownerName = Component.literal("[Programmable Controller]");
            Log.warning("Programmable controller with owner '{}' has no UUID! Substituting a random UUID ({}).", ownerName, ownerID);
        }
        return ownerID;
    }

    @Override
    public BlockPos getDeployPos() {
        return getBlockPos();
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (tag.equals("charging")) {
            shouldChargeHeldItem = !shouldChargeHeldItem;
        } else if (tag.equals("chunkload_self")) {
            chunkloadSelf = !chunkloadSelf;
        } else if (tag.equals("chunkload_work")) {
            chunkloadWorkingChunk = !chunkloadWorkingChunk;
        } else if (tag.equals("chunkload_work_3x3")) {
            chunkloadWorkingChunk3x3 = !chunkloadWorkingChunk3x3;
        } else if (itemHandlerSideConfigurator.handleButtonPress(tag, shiftHeld)) {
            shouldUpdateNeighbours = true;
        }
        setChanged();
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return itemHandlerSideConfigurator.getHandler(dir);
    }

    public void setOwner(Player ownerID) {
        this.ownerID = ownerID.getUUID();
        this.ownerName = ownerID.getName();
        this.ownerNameClient = this.ownerName.getString();
    }

    @Override
    public List<SideConfigurator<?>> getSideConfigurators() {
        return Collections.singletonList(itemHandlerSideConfigurator);
    }

    @Override
    public Direction byIndex() {
        return getRotation();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new ProgrammableControllerMenu(i, playerInventory, getBlockPos());
    }


    @Override
    public void onUpgradesChanged() {
        super.onUpgradesChanged();
        if (getLevel() != null && !getLevel().isClientSide) {
            calculateUpgrades();
        }
    }

    private void calculateUpgrades() {
        int oldInvUpgrades = droneItemHandler.getSlots() - 1;
        int newInvUpgrades = Math.min(35, getUpgrades(ModUpgrades.INVENTORY.get()));
        if (oldInvUpgrades != newInvUpgrades) {
            resizeDroneInventory(oldInvUpgrades + 1, newInvUpgrades + 1);
            tank.setCapacity((newInvUpgrades + 1) * 16000);
            if (tank.getFluidAmount() > tank.getCapacity()) {
                tank.getFluid().setAmount(tank.getCapacity());
            }
        }

        speedUpgrades = getUpgrades(ModUpgrades.SPEED.get());
    }

    private void resizeDroneInventory(int oldSize, int newSize) {
        // if the inventory has shrunk, eject any excess items
        for (int i = newSize; i < oldSize; i++) {
            ItemStack stack = droneItemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                droneItemHandler.setStackInSlot(i, ItemStack.EMPTY);
                PneumaticCraftUtils.dropItemOnGround(stack, getLevel(), getBlockPos().above());
            }
        }
        droneItemHandler.setUseableSlots(newSize);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        inventory.deserializeNBT(provider, tag.getCompound("Items"));
        tank.setCapacity((getUpgrades(ModUpgrades.INVENTORY.get()) + 1) * 16000);
        tank.readFromNBT(provider, tag.getCompound("tank"));

        ownerID = tag.contains("ownerID") ? UUID.fromString(tag.getString("ownerID")) : FALLBACK_UUID;
        ownerName = Component.literal(tag.contains("ownerName") ? tag.getString("ownerName") : FALLBACK_NAME);
        ownerNameClient = ownerName.getString();

        droneItemHandler.setUseableSlots(getUpgrades(ModUpgrades.INVENTORY.get()) + 1);
        ItemStackHandler tmpInv = new ItemStackHandler();
        tmpInv.deserializeNBT(provider, tag.getCompound("droneItems"));
        for (int i = 0; i < Math.min(tmpInv.getSlots(), droneItemHandler.getSlots()); i++) {
            droneItemHandler.setStackInSlot(i, tmpInv.getStackInSlot(i).copy());
        }

        energy.readFromNBT(tag);

        itemHandlerSideConfigurator.updateHandler("droneInv", () -> droneItemHandler);

        shouldChargeHeldItem = tag.getBoolean("chargeHeld");

        variablesNBT = tag.getCompound("variables");

        chunkloadSelf = tag.getBoolean("chunkload_self");
        chunkloadWorkingChunk = tag.getBoolean("chunkload_work");
        chunkloadWorkingChunk3x3 = tag.getBoolean("chunkload_work_3x3");
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        tag.put("Items", inventory.serializeNBT(provider));

        CompoundTag tankTag = new CompoundTag();
        tank.writeToNBT(provider, tankTag);
        tag.put("tank", tankTag);

        ItemStackHandler handler = new ItemStackHandler(droneItemHandler.getSlots());
        for (int i = 0; i < droneItemHandler.getSlots(); i++) {
            handler.setStackInSlot(i, droneItemHandler.getStackInSlot(i));
        }
        tag.put("droneItems", handler.serializeNBT(provider));

        if (ownerID != null) tag.putString("ownerID", ownerID.toString());
        if (ownerName != null) tag.putString("ownerName", ownerName.getString());

        energy.writeToNBT(tag);

        tag.putBoolean("chargeHeld", shouldChargeHeldItem);

        if (aiManager != null) tag.put("variables", aiManager.writeToNBT(new CompoundTag()));

        tag.putBoolean("chunkload_self", chunkloadSelf);
        tag.putBoolean("chunkload_work", chunkloadWorkingChunk);
        tag.putBoolean("chunkload_work_3x3", chunkloadWorkingChunk3x3);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        droneItemHandler.setFakePlayerReady();

        calculateUpgrades();
        inventory.onContentsChanged(0);  // force initial read of any installed drone/network api
        curX = targetX = getBlockPos().getX() + 0.5;
        curY = targetY = getBlockPos().getY() + 1.0;
        curZ = targetZ = getBlockPos().getZ() + 0.5;

        if (chunkloadSelf) {
            ChunkPos cp = new ChunkPos(worldPosition);
            ticketController().forceChunk((ServerLevel) nonNullLevel(), worldPosition, cp.x, cp.z, true, false);
            loadedChunks.add(cp);
        }
    }

    private static boolean isProgrammableAndValidForDrone(IDroneBase drone, ItemStack programmable) {
        return SavedDroneProgram.fromItemStack(programmable).isValidForDrone(drone);
    }

    @Override
    public float getMinWorkingPressure() {
        return 10;
    }

    @Override
    public Level getDroneLevel() {
        return getLevel();
    }

    @Override
    public FluidTank getFluidTank() {
        return tank;
    }

    @Override
    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    @Override
    public IItemHandlerModifiable getInv() {
        return droneItemHandler;
    }

    @Override
    public Vec3 getDronePos() {
        if (curX == 0 && curY == 0 && curZ == 0) {
            curX = getBlockPos().getX() + 0.5;
            curY = getBlockPos().getY() + 1.0;
            curZ = getBlockPos().getZ() + 0.5;
            targetX = curX;
            targetY = curY;
            targetZ = curZ;
        }
        return new Vec3(curX, curY, curZ);
    }

    public BlockPos getTargetPos() {
        return BlockPos.containing(targetX, targetY, targetZ);
    }

    @Override
    public BlockPos getControllerPos() {
        return worldPosition;
    }

    @Override
    public IPathNavigator getPathNavigator() {
        return controllerNavigator;
    }

    @Override
    public void sendWireframeToClient(BlockPos pos) {
    }

    @Override
    public DroneFakePlayer getFakePlayer() {
        if (fakePlayer == null) {
            fakePlayer = new DroneFakePlayer((ServerLevel) nonNullLevel(), new GameProfile(getOwnerUUID(), ownerName.getString()), this);
        }
        return fakePlayer;
    }

    @Override
    public boolean isBlockValidPathfindBlock(BlockPos pos) {
        return !nonNullLevel().getBlockState(pos).getCollisionShape(nonNullLevel(), pos, CollisionContext.empty()).equals(Shapes.block());
    }

    @Override
    public void dropItem(ItemStack stack) {
        Vec3 pos = getDronePos();
        nonNullLevel().addFreshEntity(new ItemEntity(nonNullLevel(), pos.x, pos.y, pos.z, stack));
    }

    @Override
    public void getContentsToDrop(NonNullList<ItemStack> drops) {
        super.getContentsToDrop(drops);

        for (int i = 0; i < droneItemHandler.getSlots(); i++) {
            if (!fakePlayer.getInventory().getItem(i).isEmpty()) {
                drops.add(fakePlayer.getInventory().getItem(i).copy());
            }
        }
    }

    @Override
    public void setDugBlock(BlockPos pos) {
        if (pos != null) {
            diggingX = pos.getX();
            diggingY = pos.getY();
            diggingZ = pos.getZ();
        } else {
            diggingX = diggingY = diggingZ = 0;
        }
    }

    public BlockPos getDugPosition() {
        return diggingX != 0 || diggingY != 0 || diggingZ != 0 ? new BlockPos(diggingX, diggingY, diggingZ) : null;
    }

    @Override
    public List<IProgWidget> getProgWidgets() {
        return progWidgets;
    }

    @Override
    public void setActiveProgram(IProgWidget widget) {
        activeWidgetIndex = progWidgets.indexOf(widget);
    }

    @Override
    public boolean isProgramApplicable(ProgWidgetType<?> widgetType) {
        return PneumaticCraftUtils.getRegistryName(PNCRegistries.PROG_WIDGETS_REGISTRY, widgetType)
                .map(regName -> !BLACKLISTED_WIDGETS.contains(regName))
                .orElseThrow();
    }

    @Override
    public GoalSelector getTargetAI() {
        return null;
    }

    @Override
    public void setEmittingRedstone(Direction orientation, int emittingRedstone) {
        redstoneLevels[orientation.get3DDataValue()] = emittingRedstone;
        updateNeighbours();
    }

    @Override
    public int getEmittingRedstone(Direction direction) {
        return redstoneLevels[direction.get3DDataValue()];
    }

    @Override
    public void setName(Component name) {
        if (drone != null) {
            drone.setCustomName(name);
        }
        ItemStack stack = inventory.getStackInSlot(0).copy();
        if (!stack.isEmpty()) {
            stack.set(DataComponents.CUSTOM_NAME, name);
            inventory.setStackInSlot(0, stack);
        }
    }

    @Override
    public void setCarryingEntity(Entity entity) {
        Log.warning("Drone AI setting carrying entity. However a Programmable Controller can't carry entities!");
        new Throwable().printStackTrace();
    }

    @Override
    public List<Entity> getCarryingEntities() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAIOverridden() {
        return false;
    }

    @Override
    public void onItemPickupEvent(ItemEntity curPickingUpEntity, int stackSize) {
    }

    @Override
    public Player getOwner() {
        if (ownerID == null) return null;
        if (nonNullLevel().isClientSide) return ClientUtils.getClientPlayer();

        return PneumaticCraftUtils.getPlayerFromId(ownerID);
    }

    @Override
    public void overload(String msgKey, Object... params) {
        // insert the programmable item (drone or api) into a chest on a "programmable" side
        // or failing that, drop it in-world on top of the PC
        ItemStack stack = inventory.extractItem(0, 1, false);
        if (stack.getCount() == 1) {
            boolean inserted = findEjectionDest()
                    .map(h -> ItemHandlerHelper.insertItem(h, stack, false).isEmpty())
                    .orElse(false);
            if (!inserted) PneumaticCraftUtils.dropItemOnGround(stack, level, worldPosition.above());
            nonNullLevel().playSound(null, worldPosition, ModSounds.DRONE_DEATH.get(), SoundSource.BLOCKS, 1f, 1f);
        }
        NetworkHandler.sendToAllTracking(new PacketSpawnParticle(ParticleTypes.SMOKE,
                new Vector3f(getBlockPos().getX() - 0.5f, getBlockPos().getY() + 1, getBlockPos().getZ() - 0.5f),
                new Vector3f(0, 0, 0),
                10,
                Optional.of(new Vector3f(1, 1, 1))),
                this
        );
    }

    @Override
    public DroneAIManager getAIManager() {
        if (!nonNullLevel().isClientSide) {
            if (aiManager == null) {
                aiManager = new DroneAIManager(this, new ArrayList<>());
                aiManager.dontStopWhenEndReached();
                if (variablesNBT != null) {
                    aiManager.readFromNBT(variablesNBT);
                    variablesNBT = null;
                }
            }
        }
        return aiManager;
    }

    @Override
    public void updateLabel() {
        label = aiManager != null ? getAIManager().getLabel() : "Main";
    }

    @Override
    public LogisticsManager getLogisticsManager() {
        return logisticsManager;
    }

    @Override
    public void setLogisticsManager(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;
    }

    @Override
    public void playSound(SoundEvent soundEvent, SoundSource category, float volume, float pitch) {
        // nothing
    }

    @Override
    public void addAirToDrone(int air) {
        airHandler.addAir(air);
    }

    @Override
    public boolean canMoveIntoFluid(Fluid fluid) {
        return true;
    }

    @Override
    public DroneItemHandler getDroneItemHandler() {
        return droneItemHandler;
    }

    @Override
    public float getDronePressure() {
        return getPressure();
    }

    @Override
    public DronePacket.DroneTarget getPacketTarget() {
        return DronePacket.DroneTarget.forPos(getBlockPos());
    }

    @Override
    public int getActiveWidgetIndex() {
        return activeWidgetIndex;
    }

    @Override
    public DroneDebugger getDebugger() {
        return debugger;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Component getDroneName() {
        return getDisplayName();
    }

    @Override
    public void storeTrackerData(ItemStack stack) {
        stack.set(ModDataComponents.DRONE_DEBUG_TARGET, DronePacket.DroneTarget.forPos(getBlockPos()));
    }

    @Override
    public boolean isDroneStillValid() {
        return !remove;
    }

    public boolean chunkloadSelf() {
        return chunkloadSelf;
    }

    public boolean chunkloadWorkingChunk() {
        return chunkloadWorkingChunk;
    }

    public boolean chunkloadWorkingChunk3x3() {
        return chunkloadWorkingChunk3x3;
    }

    private Optional<IItemHandler> findEjectionDest() {
        Direction dir = null;
        for (Direction d : DirectionUtil.VALUES) {
            if (IOHelper.getInventoryForBlock(this, d).map(h -> h == inventory).orElse(false)) {
                dir = d;
                break;
            }
        }
        if (dir != null) {
            BlockEntity te = nonNullLevel().getBlockEntity(worldPosition.relative(dir));
            return IOHelper.getInventoryForBlock(te, dir.getOpposite());
        }
        return Optional.empty();
    }

    private class ProgrammableItemStackHandler extends BaseItemStackHandler {
        ProgrammableItemStackHandler(BlockEntity te) {
            super(te, INVENTORY_SIZE);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            ItemStack stack = getStackInSlot(slot);
            progWidgets.clear();
            if (!stack.isEmpty() && isProgrammableAndValidForDrone(ProgrammableControllerBlockEntity.this, stack)) {
                progWidgets.addAll(SavedDroneProgram.loadProgWidgets(stack));
                ProgWidgetUtils.updatePuzzleConnections(progWidgets);
                isIdle = false;
            } else {
                setDugBlock(null);
                targetX = getBlockPos().getX() + 0.5;
                targetY = getBlockPos().getY() + 1.0;
                targetZ = getBlockPos().getZ() + 0.5;
                boolean updateNeighbours = false;
                for (int i = 0; i < redstoneLevels.length; i++) {
                    if (redstoneLevels[i] > 0) {
                        redstoneLevels[i] = 0;
                        updateNeighbours = true;
                    }
                }
                if (updateNeighbours) updateNeighbours();
                isIdle = true;
            }
            if (getLevel() != null && !getLevel().isClientSide) {
                aiManager = null;
                aiManager = getAIManager();
                aiManager.setWidgets(progWidgets);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || isProgrammableAndValidForDrone(ProgrammableControllerBlockEntity.this, itemStack);
        }
    }

    private class ControllerNavigator implements IPathNavigator {
        @Override
        public boolean moveToXYZ(double x, double y, double z) {
            if (isBlockValidPathfindBlock(BlockPos.containing(x, y, z))) {
                targetX = x + 0.5;
                targetY = y + 0.5;
                targetZ = z + 0.5;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean moveToEntity(Entity entity) {
            return moveToXYZ(entity.getX(), entity.getY() + 0.3, entity.getZ());
        }

        @Override
        public boolean hasNoPath() {
            return PneumaticCraftUtils.distBetweenSq(curX, curY, curZ, targetX, targetY, targetZ) < 0.5;
        }

        @Override
        public boolean isGoingToTeleport() {
            return false;
        }
    }
}
