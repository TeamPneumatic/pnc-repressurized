package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.drone.DroneConstructingEvent;
import me.desht.pneumaticcraft.api.drone.IDrone;
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
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.inventory.ContainerProgrammableController;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.tileentity.SideConfigurator.RelativeFace;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.fakeplayer.DroneFakePlayer;
import me.desht.pneumaticcraft.common.util.fakeplayer.DroneItemHandler;
import me.desht.pneumaticcraft.common.util.fakeplayer.FakeNetHandlerPlayerServer;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

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
            RL("suicide"),
            RL("teleport"),
            RL("entity_export"),
            RL("entity_import")
    );

    private final ProgrammableItemStackHandler inventory = new ProgrammableItemStackHandler(this);
    private final LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> inventory);

    private final FluidTank tank = new FluidTank(16000);
    private final LazyOptional<IFluidHandler> tankCap = LazyOptional.of(() -> tank);

    private final PneumaticEnergyStorage energy = new PneumaticEnergyStorage(MAX_ENERGY);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    private EntityProgrammableController drone;
    private DroneAIManager aiManager;
    private DroneFakePlayer fakePlayer;
    private DroneItemHandler droneItemHandler = new DummyItemHandler(this);
    private final List<IProgWidget> progWidgets = new ArrayList<>();
    private final int[] redstoneLevels = new int[6];
    private final SideConfigurator<IItemHandler> itemHandlerSideConfigurator;

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

    private UUID ownerID;
    private ITextComponent ownerName;
    private boolean updateNeighbours;
    // Although this is only used by DroneAILogistics, it is here rather than there
    // so it can persist, for performance reasons; DroneAILogistics is a short-lived object
    private LogisticsManager logisticsManager;

    public TileEntityProgrammableController() {
        super(ModTileEntities.PROGRAMMABLE_CONTROLLER.get(), 20, 25, 10000, 4);

        MinecraftForge.EVENT_BUS.post(new DroneConstructingEvent(this));

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

        double speed = getSpeed();
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
            }
        } else {
            if (drone == null || !drone.isAlive()) {
                drone = ModEntities.PROGRAMMABLE_CONTROLLER.get().create(getWorld());
                drone.setController(this);
                drone.setPosition(curX, curY, curZ);
                ClientUtils.spawnEntityClientside(drone);
            }
            drone.setPosition(curX, curY, curZ);
        }
    }

    @Override
    public void remove() {
        super.remove();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public void onDescUpdate() {
        super.onDescUpdate();
        if (drone != null) {
            drone.remove();
        }
    }

    private double getSpeed() {
        return Math.min(10, speedUpgrades) * 0.05 + 0.15;
    }

    private UUID getOwnerUUID() {
        if (ownerID == null) {
            ownerID = UUID.randomUUID();
            Log.warning(String.format("Programmable controller with owner '%s' has no UUID! Substituting a random UUID (%s).", ownerName, ownerID.toString()));
        }
        return ownerID;
    }

    private DroneItemHandler getDroneItemHandler() {
        if (droneItemHandler == null || droneItemHandler instanceof DummyItemHandler) {
            droneItemHandler = new DroneItemHandler(this, 1);
            itemHandlerSideConfigurator.updateHandler("droneInv", () -> droneItemHandler);
        }
        return droneItemHandler;
    }

    private void initializeFakePlayer() {
        fakePlayer = new DroneFakePlayer((ServerWorld) getWorld(), new GameProfile(getOwnerUUID(), "test"), this);
        fakePlayer.connection = new FakeNetHandlerPlayerServer(ServerLifecycleHooks.getCurrentServer(), fakePlayer);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        if (itemHandlerSideConfigurator.handleButtonPress(tag)) {
            updateNeighbours = true;
        }
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
    }

    @Override
    public List<SideConfigurator<?>> getSideConfigurators() {
        return Collections.singletonList(itemHandlerSideConfigurator);
    }

    @Override
    public Direction byIndex() {
        return getRotation();
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerProgrammableController(i, playerInventory, getPos());
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
            if (!getWorld().isRemote) {
                getAIManager().setWidgets(progWidgets);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || isProgrammableAndValidForDrone(TileEntityProgrammableController.this, itemStack);
        }
    }

    @Override
    public void onUpgradesChanged() {
        super.onUpgradesChanged();
        if (getWorld() != null && !getWorld().isRemote) {
            calculateUpgrades();
        }
    }

    private void calculateUpgrades() {
        // can be null on initial placement
        int oldInvUpgrades = droneItemHandler == null ? 0 : droneItemHandler.getSlots() - 1;
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
        DroneItemHandler tmpHandler = new DroneItemHandler(this, newSize);

        for (int i = 0; i < oldSize && i < newSize; i++) {
            tmpHandler.setStackInSlot(i, droneItemHandler.getStackInSlot(i));
        }

        // if the inventory has shrunk, eject any excess items
        for (int i = newSize; i < oldSize; i++) {
            ItemStack stack = droneItemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                PneumaticCraftUtils.dropItemOnGround(stack, getWorld(), getPos().up());
            }
        }

        droneItemHandler = tmpHandler;
        itemHandlerSideConfigurator.updateHandler("droneInv", () -> droneItemHandler);
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        inventory.deserializeNBT(tag.getCompound("Items"));
        tank.readFromNBT(tag.getCompound("tank"));

        ownerID = tag.contains("ownerID") ? UUID.fromString(tag.getString("ownerID")) : FALLBACK_UUID;
        ownerName = tag.contains("ownerName") ? new StringTextComponent(tag.getString("ownerName")) : new StringTextComponent(FALLBACK_NAME);

        droneItemHandler = new DroneItemHandler(this, getUpgrades(EnumUpgrade.INVENTORY) + 1);
        ItemStackHandler tmpInv = new ItemStackHandler();
        tmpInv.deserializeNBT(tag.getCompound("droneItems"));
        if (getDroneSlots() != droneItemHandler.getSlots()) {
            Log.warning("drone inventory size mismatch: dispenser upgrades = " + getDroneSlots() + ", saved inv size = " + droneItemHandler.getSlots());
        }
        for (int i = 0; i < tmpInv.getSlots() && i < droneItemHandler.getSlots(); i++) {
            droneItemHandler.setStackInSlot(i, tmpInv.getStackInSlot(i).copy());
        }
        energy.writeToNBT(tag);

        itemHandlerSideConfigurator.updateHandler("droneInv", () -> droneItemHandler);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.put("Items", inventory.serializeNBT());

        CompoundNBT tankTag = new CompoundNBT();
        tank.writeToNBT(tankTag);
        tag.put("tank", tankTag);

        ItemStackHandler handler = new ItemStackHandler(getDroneSlots());
        for (int i = 0; i < getDroneSlots(); i++) {
            handler.setStackInSlot(i, getDroneItemHandler().getStackInSlot(i));
        }
        tag.put("droneItems", handler.serializeNBT());

        if (ownerID != null) tag.putString("ownerID", ownerID.toString());
        if (ownerName != null) tag.putString("ownerName", ownerName.getString());

        energy.readFromNBT(tag);

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

        getDroneItemHandler().setFakePlayerReady();

        calculateUpgrades();
        inventory.onContentsChanged(0);  // force initial read of any installed drone/network api
        curX = targetX = getPos().getX() + 0.5;
        curY = targetY = getPos().getY() + 1.0;
        curZ = targetZ = getPos().getZ() + 0.5;

        MinecraftForge.EVENT_BUS.register(this);
    }

    private int getDroneSlots() {
        return world != null && world.isRemote ? 0 : Math.min(36, 1 + getUpgrades(EnumUpgrade.INVENTORY));
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
        return getDroneItemHandler();
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
            initializeFakePlayer();
        }
        return fakePlayer;
    }

    @Override
    public boolean isBlockValidPathfindBlock(BlockPos pos) {
        return getWorld().isAirBlock(pos);
    }

    @Override
    public void dropItem(ItemStack stack) {
        Vector3d pos = getDronePos();
        getWorld().addEntity(new ItemEntity(getWorld(), pos.x, pos.y, pos.z, stack));
    }

    @Override
    public void getContentsToDrop(NonNullList<ItemStack> drops) {
        super.getContentsToDrop(drops);

        for (int i = 0; i < getDroneSlots(); i++) {
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
        NetworkHandler.sendToAllAround(
                new PacketSpawnParticle(ParticleTypes.SMOKE,
                        getPos().getX() - 0.5, getPos().getY() + 1, getPos().getZ() - 0.5,
                        0, 0, 0, 10, 1, 1, 1),
                getWorld());
    }

    @Override
    public DroneAIManager getAIManager() {
        if (!getWorld().isRemote) {
            if (aiManager == null) {
                aiManager = new DroneAIManager(this, new ArrayList<>());
                aiManager.dontStopWhenEndReached();
            }
        }
        return aiManager;
    }

    @Override
    public void updateLabel() {
    }

    @Override
    public void addDebugEntry(String message) {
    }

    @Override
    public void addDebugEntry(String message, BlockPos pos) {
    }

    @Override
    public LogisticsManager getLogisticsManager() {
        return logisticsManager;
    }

    @Override
    public void setLogisticsManager(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;
    }

    private static class DummyItemHandler extends DroneItemHandler {
        public DummyItemHandler(IDrone holder) {
            super(holder, 1);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        protected boolean isFakePlayerReady() {
            return false;
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        }
    }
}
