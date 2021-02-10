package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.drone.DroneConstructingEvent;
import me.desht.pneumaticcraft.api.drone.IPathNavigator;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.api.semiblock.SemiblockEvent;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.ai.LogisticsManager;
import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.debug.DroneDebugger;
import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.inventory.ContainerProgrammableController;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.tileentity.SideConfigurator.RelativeFace;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.fakeplayer.DroneFakePlayer;
import me.desht.pneumaticcraft.common.util.fakeplayer.DroneItemHandler;
import me.desht.pneumaticcraft.common.util.fakeplayer.FakeNetHandlerPlayerServer;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.NBTKeys;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class TileEntityProgrammableController extends TileEntityPneumaticBase
        implements IMinWorkingPressure, IDroneBase, ISideConfigurable, INamedContainerProvider {
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
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventory);

    private final FluidTank tank = new FluidTank(16000);
    private final LazyOptional<IFluidHandler> tankCap = LazyOptional.of(() -> tank);

    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(MAX_ENERGY);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    private final DroneItemHandler droneItemHandler = new DroneItemHandler(this, 1);

    private EntityProgrammableController drone;
    private DroneAIManager aiManager;
    private DroneFakePlayer fakePlayer;
    private final List<IProgWidget> progWidgets = new ArrayList<>();
    private final int[] redstoneLevels = new int[6];
    private final SideConfigurator<IItemHandler> itemHandlerSideConfigurator;
    private CompoundNBT variablesNBT = null;  // pending variable data to add to ai manager

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
    @GuiSynced
    public boolean shouldChargeHeldItem;
    @DescSynced
    public String label = "";
    @DescSynced
    public String ownerNameClient = "";

    private UUID ownerID;
    private ITextComponent ownerName;
    private boolean updateNeighbours;
    // Although this is only used by DroneAILogistics, it is here rather than there so it can persist,
    // for performance reasons; DroneAILogistics is a short-lived object and LogisticsManager is expensive to create
    private LogisticsManager logisticsManager;
    private final DroneDebugger debugger = new DroneDebugger(this);
    @DescSynced
    private int activeWidgetIndex;

    public TileEntityProgrammableController() {
        super(ModTileEntities.PROGRAMMABLE_CONTROLLER.get(), 20, 25, 10000, 4);

        MinecraftForge.EVENT_BUS.post(new DroneConstructingEvent(this));

        MinecraftForge.EVENT_BUS.register(this);

        itemHandlerSideConfigurator = new SideConfigurator<>("items", this);
        itemHandlerSideConfigurator.registerHandler("droneInv", new ItemStack(ModItems.DRONE.get()),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> droneItemHandler,
                RelativeFace.TOP, RelativeFace.FRONT, RelativeFace.BACK, RelativeFace.LEFT, RelativeFace.RIGHT);
        itemHandlerSideConfigurator.registerHandler("programmableInv", new ItemStack(ModItems.NETWORK_API.get()),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> inventory,
                RelativeFace.BOTTOM);
        itemHandlerSideConfigurator.setNullFaceHandler("droneInv");
    }

    @SubscribeEvent
    public void onSemiblockEvent(SemiblockEvent event) {
        if (!event.getWorld().isRemote && event.getWorld() == getWorld() && event.getSemiblock() instanceof EntityLogisticsFrame) {
            logisticsManager = null;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isRemote && updateNeighbours) {
            updateNeighbours();
            updateNeighbours = false;
        }

        double speed = BASE_SPEED + speedUpgrades * SPEED_PER_UPGRADE;
        if (PneumaticCraftUtils.distBetweenSq(getPos(), targetX, targetY, targetZ) <= 1 && isIdle) {
            curX = targetX;
            curY = targetY;
            curZ = targetZ;
        } else if (PneumaticCraftUtils.distBetweenSq(curX, curY, curZ, targetX, targetY, targetZ) > 0.25) {
            // dist-between check here avoids drone "jitter" when it's very near its target
            Vector3d vec = new Vector3d(targetX - curX, targetY - curY, targetZ - curZ).normalize().scale(speed);
            curX += vec.x;
            curY += vec.y;
            curZ += vec.z;
        }

        if (!getWorld().isRemote) {
            DroneFakePlayer fp = getFakePlayer();
            for (int i = 0; i < 4; i++) {
                fp.interactionManager.tick();
            }
            fp.setPosition(curX, curY, curZ);
            fp.tick();

            if (getPressure() >= getMinWorkingPressure()) {
                if (!aiManager.isIdling()) addAir(-PneumaticValues.USAGE_PROGRAMMABLE_CONTROLLER);
                aiManager.onUpdateTasks();
                maybeChargeHeldItem();
            }

            if (world.getGameTime() % 20 == 0) {
                debugger.updateDebuggingPlayers();
            }
        } else {
            if ((drone == null || !drone.isAlive()) && getWorld().isAreaLoaded(new BlockPos(curX, curY, curZ), 1)) {
                drone = ModEntities.PROGRAMMABLE_CONTROLLER.get().create(getWorld());
                if (drone != null) {
                    drone.setController(this);
                    drone.setPosition(curX, curY, curZ);
                    ClientUtils.spawnEntityClientside(drone);
                }
            }
            drone.setPosition(curX, curY, curZ);
        }
    }

    private void maybeChargeHeldItem() {
        if (!shouldChargeHeldItem) return;

        ItemStack held = droneItemHandler.getStackInSlot(0);

        if (energy.getEnergyStored() > 100) {
            held.getCapability(CapabilityEnergy.ENERGY).ifPresent(handler -> {
                if (handler.getMaxEnergyStored() - handler.getEnergyStored() > 250) {
                    handler.receiveEnergy(energy.extractEnergy(250, false), false);
                }
            });
        }

        held.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(handler -> {
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
        markDirty();
    }

    @Override
    public void remove() {
        super.remove();

        MinecraftForge.EVENT_BUS.unregister(this);
    }

    private UUID getOwnerUUID() {
        if (ownerID == null) {
            ownerID = UUID.randomUUID();
            Log.warning(String.format("Programmable controller with owner '%s' has no UUID! Substituting a random UUID (%s).", ownerName, ownerID.toString()));
        }
        return ownerID;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        if (tag.equals("charging")) {
            shouldChargeHeldItem = !shouldChargeHeldItem;
        } else if (itemHandlerSideConfigurator.handleButtonPress(tag)) {
            updateNeighbours = true;
        }
        markDirty();
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return inventory;
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return invCap;
    }

    public void setOwner(PlayerEntity ownerID) {
        this.ownerID = ownerID.getUniqueID();
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
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerProgrammableController(i, playerInventory, getPos());
    }


    @Override
    public void onUpgradesChanged() {
        super.onUpgradesChanged();
        if (getWorld() != null && !getWorld().isRemote) {
            calculateUpgrades();
        }
    }

    private void calculateUpgrades() {
        int oldInvUpgrades = droneItemHandler.getSlots() - 1;
        int newInvUpgrades = Math.min(35, getUpgrades(EnumUpgrade.INVENTORY));
        if (oldInvUpgrades != newInvUpgrades) {
            resizeDroneInventory(oldInvUpgrades + 1, newInvUpgrades + 1);
            tank.setCapacity((newInvUpgrades + 1) * 16000);
            if (tank.getFluidAmount() > tank.getCapacity()) {
                tank.getFluid().setAmount(tank.getCapacity());
            }
        }

        speedUpgrades = getUpgrades(EnumUpgrade.SPEED);
    }

    private void resizeDroneInventory(int oldSize, int newSize) {
        // if the inventory has shrunk, eject any excess items
        for (int i = newSize; i < oldSize; i++) {
            ItemStack stack = droneItemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                droneItemHandler.setStackInSlot(i, ItemStack.EMPTY);
                PneumaticCraftUtils.dropItemOnGround(stack, getWorld(), getPos().up());
            }
        }
        droneItemHandler.setUseableSlots(newSize);
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        inventory.deserializeNBT(tag.getCompound("Items"));
        tank.setCapacity((getUpgrades(EnumUpgrade.INVENTORY) + 1) * 16000);
        tank.readFromNBT(tag.getCompound("tank"));

        ownerID = tag.contains("ownerID") ? UUID.fromString(tag.getString("ownerID")) : FALLBACK_UUID;
        ownerName = tag.contains("ownerName") ? new StringTextComponent(tag.getString("ownerName")) : new StringTextComponent(FALLBACK_NAME);
        ownerNameClient = ownerName.getString();

        droneItemHandler.setUseableSlots(getUpgrades(EnumUpgrade.INVENTORY) + 1);
        ItemStackHandler tmpInv = new ItemStackHandler();
        tmpInv.deserializeNBT(tag.getCompound("droneItems"));
        for (int i = 0; i < Math.min(tmpInv.getSlots(), droneItemHandler.getSlots()); i++) {
            droneItemHandler.setStackInSlot(i, tmpInv.getStackInSlot(i).copy());
        }

        energy.readFromNBT(tag);

        itemHandlerSideConfigurator.updateHandler("droneInv", () -> droneItemHandler);

        shouldChargeHeldItem = tag.getBoolean("chargeHeld");

        variablesNBT = tag.getCompound("variables");
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.put("Items", inventory.serializeNBT());

        CompoundNBT tankTag = new CompoundNBT();
        tank.writeToNBT(tankTag);
        tag.put("tank", tankTag);

        ItemStackHandler handler = new ItemStackHandler(droneItemHandler.getSlots());
        for (int i = 0; i < droneItemHandler.getSlots(); i++) {
            handler.setStackInSlot(i, droneItemHandler.getStackInSlot(i));
        }
        tag.put("droneItems", handler.serializeNBT());

        if (ownerID != null) tag.putString("ownerID", ownerID.toString());
        if (ownerName != null) tag.putString("ownerName", ownerName.getString());

        energy.writeToNBT(tag);

        tag.putBoolean("chargeHeld", shouldChargeHeldItem);

        if (aiManager != null) tag.put("variables", aiManager.writeToNBT(new CompoundNBT()));

        return tag;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, tankCap);
        } else if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, itemHandlerSideConfigurator.getHandler(side));
        } else if (cap == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.orEmpty(cap, energyCap);
        } else {
            return super.getCapability(cap, side);
        }
    }

    @Override
    protected void onFirstServerTick() {
        super.onFirstServerTick();

        droneItemHandler.setFakePlayerReady();

        calculateUpgrades();
        inventory.onContentsChanged(0);  // force initial read of any installed drone/network api
        curX = targetX = getPos().getX() + 0.5;
        curY = targetY = getPos().getY() + 1.0;
        curZ = targetZ = getPos().getZ() + 0.5;
    }

    private static boolean isProgrammableAndValidForDrone(IDroneBase drone, ItemStack programmable) {
        if (programmable.getItem() instanceof IProgrammable
                && ((IProgrammable) programmable.getItem()).canProgram(programmable)
                && ((IProgrammable) programmable.getItem()).usesPieces(programmable)) {
            List<IProgWidget> widgets = TileEntityProgrammer.getProgWidgets(programmable);
            return widgets.stream().allMatch(widget -> drone.isProgramApplicable(widget.getType()));
        }
        return false;
    }

    @Override
    public float getMinWorkingPressure() {
        return 10;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public World world() {
        return getWorld();
    }

    @Override
    public IFluidTank getFluidTank() {
        return tank;
    }

    @Override
    public IItemHandlerModifiable getInv() {
        return droneItemHandler;
    }

    @Override
    public Vector3d getDronePos() {
        if (curX == 0 && curY == 0 && curZ == 0) {
            curX = getPos().getX() + 0.5;
            curY = getPos().getY() + 1.0;
            curZ = getPos().getZ() + 0.5;
            targetX = curX;
            targetY = curY;
            targetZ = curZ;
        }
        return new Vector3d(curX, curY, curZ);
    }

    public BlockPos getTargetPos() {
        return new BlockPos(targetX, targetY, targetZ);
    }

    @Override
    public BlockPos getControllerPos() {
        return pos;
    }

    @Override
    public IPathNavigator getPathNavigator() {
        return new IPathNavigator() {

            @Override
            public boolean moveToXYZ(double x, double y, double z) {
                if (isBlockValidPathfindBlock(new BlockPos(x, y, z))) {
                    targetX = x + 0.5;
                    targetY = y - 0.3;
                    targetZ = z + 0.5;
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean moveToEntity(Entity entity) {
                return moveToXYZ(entity.getPosX(), entity.getPosY() + 0.3, entity.getPosZ());
            }

            @Override
            public boolean hasNoPath() {
                return PneumaticCraftUtils.distBetweenSq(curX, curY, curZ, targetX, targetY, targetZ) < 0.5;
            }

            @Override
            public boolean isGoingToTeleport() {
                return false;
            }

        };
    }

    @Override
    public void sendWireframeToClient(BlockPos pos) {
    }

    @Override
    public DroneFakePlayer getFakePlayer() {
        if (fakePlayer == null) {
            fakePlayer = new DroneFakePlayer((ServerWorld) getWorld(), new GameProfile(getOwnerUUID(), ownerName.getString()), this);
            fakePlayer.connection = new FakeNetHandlerPlayerServer(ServerLifecycleHooks.getCurrentServer(), fakePlayer);
        }
        return fakePlayer;
    }

    @Override
    public boolean isBlockValidPathfindBlock(BlockPos pos) {
        return !getWorld().getBlockState(pos).getCollisionShape(getWorld(), pos, ISelectionContext.dummy()).equals(VoxelShapes.fullCube());
    }

    @Override
    public void dropItem(ItemStack stack) {
        Vector3d pos = getDronePos();
        getWorld().addEntity(new ItemEntity(getWorld(), pos.x, pos.y, pos.z, stack));
    }

    @Override
    public void getContentsToDrop(NonNullList<ItemStack> drops) {
        super.getContentsToDrop(drops);

        for (int i = 0; i < droneItemHandler.getSlots(); i++) {
            if (!fakePlayer.inventory.getStackInSlot(i).isEmpty()) {
                drops.add(fakePlayer.inventory.getStackInSlot(i).copy());
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
    public boolean isProgramApplicable(ProgWidgetType widgetType) {
        return !BLACKLISTED_WIDGETS.contains(widgetType.getRegistryName());
    }

    @Override
    public GoalSelector getTargetAI() {
        return null;
    }

    @Override
    public void setEmittingRedstone(Direction orientation, int emittingRedstone) {
        redstoneLevels[orientation.ordinal()] = emittingRedstone;
        updateNeighbours();
    }

    public int getEmittingRedstone(Direction direction) {
        return redstoneLevels[direction.ordinal()];
    }

    @Override
    public void setName(ITextComponent name) {
        if (drone != null) {
            drone.setCustomName(name);
        }
        ItemStack stack = inventory.getStackInSlot(0).copy();
        if (!stack.isEmpty()) {
            stack.setDisplayName(name);
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
    public PlayerEntity getOwner() {
        if (ownerID == null) return null;
        if (getWorld().isRemote) return ClientUtils.getClientPlayer();

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
            if (!inserted) PneumaticCraftUtils.dropItemOnGround(stack, world, pos.up());
            world.playSound(null, pos, ModSounds.DRONE_DEATH.get(), SoundCategory.BLOCKS, 1f, 1f);
        }
        NetworkHandler.sendToAllTracking(new PacketSpawnParticle(ParticleTypes.SMOKE,
                getPos().getX() - 0.5, getPos().getY() + 1, getPos().getZ() - 0.5,
                0, 0, 0, 10, 1, 1, 1), this);
    }

    @Override
    public DroneAIManager getAIManager() {
        if (!getWorld().isRemote) {
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
    public void playSound(SoundEvent soundEvent, SoundCategory category, float volume, float pitch) {
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
    public ITextComponent getDroneName() {
        return getDisplayName();
    }

    @Override
    public void storeTrackerData(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        tag.put(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_PC, NBTUtil.writeBlockPos(getPos()));
        tag.remove(NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE);
    }

    @Override
    public boolean isDroneStillValid() {
        return !removed;
    }

    private LazyOptional<IItemHandler> findEjectionDest() {
        Direction dir = null;
        for (Direction d : Direction.VALUES) {
            if (getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d).map(h -> h == inventory).orElse(false)) {
                dir = d;
                break;
            }
        }
        if (dir != null) {
            TileEntity te = world.getTileEntity(pos.offset(dir));
            return IOHelper.getInventoryForTE(te, dir.getOpposite());
        }
        return LazyOptional.empty();
    }

    private class ProgrammableItemStackHandler extends BaseItemStackHandler {
        ProgrammableItemStackHandler(TileEntity te) {
            super(te, INVENTORY_SIZE);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            ItemStack stack = getStackInSlot(slot);
            progWidgets.clear();
            if (!stack.isEmpty() && isProgrammableAndValidForDrone(TileEntityProgrammableController.this, stack)) {
                progWidgets.addAll(TileEntityProgrammer.getProgWidgets(stack));
                TileEntityProgrammer.updatePuzzleConnections(progWidgets);
                isIdle = false;
            } else {
                setDugBlock(null);
                targetX = getPos().getX() + 0.5;
                targetY = getPos().getY() + 1.0;
                targetZ = getPos().getZ() + 0.5;
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
            if (getWorld() != null && !getWorld().isRemote) {
                aiManager = null;
                aiManager = getAIManager();
                aiManager.setWidgets(progWidgets);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || isProgrammableAndValidForDrone(TileEntityProgrammableController.this, itemStack);
        }
    }
}
