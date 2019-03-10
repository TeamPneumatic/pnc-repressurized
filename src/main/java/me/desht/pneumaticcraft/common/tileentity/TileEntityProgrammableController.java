package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.drone.DroneConstructingEvent;
import me.desht.pneumaticcraft.api.drone.IPathNavigator;
import me.desht.pneumaticcraft.api.event.SemiblockEvent;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.ai.LogisticsManager;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.item.Itemss;
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
import me.desht.pneumaticcraft.common.util.fakeplayer.InventoryFakePlayer;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityProgrammableController extends TileEntityPneumaticBase implements IMinWorkingPressure, IDroneBase, ISideConfigurable {
    private static final int INVENTORY_SIZE = 1;
    private static final String FALLBACK_NAME = "[ProgController]";
    private static final UUID FALLBACK_UUID = UUID.nameUUIDFromBytes(FALLBACK_NAME.getBytes());

    private final ProgrammableItemStackHandler inventory;
    private EntityProgrammableController drone;
    private final FluidTank tank = new FluidTank(16000);
    private DroneAIManager aiManager;
    private DroneFakePlayer fakePlayer;
    private DroneItemHandler droneInventory = new DroneItemHandler(1, this);
    private List<IProgWidget> progWidgets = new ArrayList<>();
    private final int[] redstoneLevels = new int[6];
    private int dispenserUpgrades;
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

    public static final Set<String> BLACKLISTED_WIDGETS = ImmutableSet.of(
            "computerCraft",
            "entityAttack",
            "droneConditionEntity",
            "standby",
            "suicide",
            "teleport",
            "entityExport",
            "entityImport"
    );

    private UUID ownerID;
    private String ownerName;
    private boolean updateNeighbours;
    // Although this is only used by DroneAILogistics, it is here rather than there
    // so it can persist, for performance reasons; DroneAILogistics is a short-lived object
    private LogisticsManager logisticsManager;

    public TileEntityProgrammableController() {
        super(5, 7, 5000, 4);
        inventory = new ProgrammableItemStackHandler(this);
        addApplicableUpgrade(EnumUpgrade.SPEED, EnumUpgrade.DISPENSER);
        MinecraftForge.EVENT_BUS.post(new DroneConstructingEvent(this));

        itemHandlerSideConfigurator = new SideConfigurator<>("items", this, 5);
        itemHandlerSideConfigurator.registerHandler("droneInv", new ItemStack(Itemss.DRONE),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, droneInventory,
                RelativeFace.TOP, RelativeFace.FRONT, RelativeFace.BACK, RelativeFace.LEFT, RelativeFace.RIGHT);
        itemHandlerSideConfigurator.registerHandler("programmableInv", new ItemStack(Itemss.NETWORK_COMPONENT, 1, 1),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inventory,
                RelativeFace.BOTTOM);
        itemHandlerSideConfigurator.setNullFaceHandler("droneInv");
    }

    @SubscribeEvent
    public void onSemiblockEvent(SemiblockEvent event) {
        if (!event.getWorld().isRemote && event.getWorld() == getWorld()) {
            logisticsManager = null;
        }
    }

    @Override
    public void update() {
        super.update();

        if (!getWorld().isRemote && updateNeighbours) {
            updateNeighbours();
            updateNeighbours = false;
        }

        double speed = getSpeed();
        if (PneumaticCraftUtils.distBetweenSq(getPos(), targetX, targetY, targetZ) <= speed * speed) {
            curX = targetX;
            curY = targetY;
            curZ = targetZ;
        } else if (PneumaticCraftUtils.distBetweenSq(curX, curY, curZ, targetX, targetY, targetZ) > 0.25) {
            // dist-between check here avoids drone "jitter" when it's very near its target
            Vec3d vec = new Vec3d(targetX - curX, targetY - curY, targetZ - curZ).normalize().scale(speed);
            curX += vec.x;
            curY += vec.y;
            curZ += vec.z;
        }

        if (!getWorld().isRemote) {
            droneInventory.updateHeldItem();
            DroneFakePlayer fp = getFakePlayer();
            for (int i = 0; i < 4; i++) {
                fp.interactionManager.updateBlockRemoving();
            }
            fp.posX = curX;
            fp.posY = curY;
            fp.posZ = curZ;
            fp.onUpdate();

            if (getPressure() >= getMinWorkingPressure()) {
                if (!aiManager.isIdling()) addAir(-PneumaticValues.USAGE_PROGRAMMABLE_CONTROLLER);
                aiManager.onUpdateTasks();
            }
        } else {
            if (drone == null || drone.isDead) {
                drone = new EntityProgrammableController(getWorld(), this);
                drone.posX = curX;
                drone.posY = curY;
                drone.posZ = curZ;
                getWorld().spawnEntity(drone);
            }
            drone.setPosition(curX, curY, curZ);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public void onDescUpdate() {
        super.onDescUpdate();
        if (drone != null) {
            drone.setDead();
        }
    }

    private double getSpeed() {
        return Math.min(10, speedUpgrades) * 0.1 + 0.1;
    }

    private UUID getOwnerUUID() {
        if (ownerID == null) {
            ownerID = UUID.randomUUID();
            Log.warning(String.format("Programmable controller with owner '%s' has no UUID! Substituting a random UUID (%s).", ownerName, ownerID.toString()));
        }
        return ownerID;
    }

    private void initializeFakePlayer() {
        fakePlayer = new DroneFakePlayer((WorldServer) getWorld(), new GameProfile(getOwnerUUID(), ownerName), this);
        fakePlayer.connection = new FakeNetHandlerPlayerServer(FMLCommonHandler.instance().getMinecraftServerInstance(), fakePlayer);
        fakePlayer.inventory = new InventoryFakePlayer(fakePlayer) {
            @Override
            public IItemHandlerModifiable getUnderlyingItemHandler() {
                return droneInventory;
            }
        };
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (itemHandlerSideConfigurator.handleButtonPress(buttonID)) {
            updateNeighbours = true;
        }
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    public void setOwner(EntityPlayer ownerID) {
        this.ownerID = ownerID.getUniqueID();
        this.ownerName = ownerID.getName();
    }

    @Override
    public List<SideConfigurator> getSideConfigurators() {
        return Collections.singletonList(itemHandlerSideConfigurator);
    }

    @Override
    public EnumFacing byIndex() {
        return getRotation();
    }

    private class ProgrammableItemStackHandler extends FilteredItemStackHandler {
        ProgrammableItemStackHandler(TileEntity te) {
            super(te, INVENTORY_SIZE);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            ItemStack stack = getStackInSlot(slot);
            if (!stack.isEmpty() && isProgrammableAndValidForDrone(TileEntityProgrammableController.this, stack)) {
                progWidgets = TileEntityProgrammer.getProgWidgets(stack);
            } else {
                progWidgets.clear();
                setDugBlock(null);
                targetX = getPos().getX() + 0.5;
                targetY = getPos().getY() + 0.6;
                targetZ = getPos().getZ() + 0.5;
                boolean updateNeighbours = false;
                for (int i = 0; i < redstoneLevels.length; i++) {
                    if (redstoneLevels[i] > 0) {
                        redstoneLevels[i] = 0;
                        updateNeighbours = true;
                    }
                }
                if (updateNeighbours) updateNeighbours();
            }
            if (!getWorld().isRemote) {
                getAIManager().setWidgets(progWidgets);
            }
        }

        @Override
        public boolean test(Integer integer, ItemStack itemStack) {
            return itemStack.isEmpty() || isProgrammableAndValidForDrone(TileEntityProgrammableController.this, itemStack);
        }
    }

    @Override
    public String getName() {
        return Blockss.PROGRAMMABLE_CONTROLLER.getTranslationKey();
    }

    @Override
    protected void onUpgradesChanged() {
        super.onUpgradesChanged();
        if (getWorld() != null && !getWorld().isRemote) {
            calculateUpgrades();
        }
    }

    private void calculateUpgrades() {
        int oldDispenserUpgrades = dispenserUpgrades;
        dispenserUpgrades = Math.min(35, getUpgrades(EnumUpgrade.DISPENSER));
        if (!getWorld().isRemote && oldDispenserUpgrades != dispenserUpgrades) {
            resizeDroneInventory(oldDispenserUpgrades + 1, dispenserUpgrades + 1);

            tank.setCapacity((dispenserUpgrades + 1) * 16000);
            if (tank.getFluidAmount() > tank.getCapacity()) {
                tank.getFluid().amount = tank.getCapacity();
            }
        }

        speedUpgrades = getUpgrades(EnumUpgrade.SPEED);
    }

    private void resizeDroneInventory(int oldSize, int newSize) {
        DroneItemHandler tmpHandler = new DroneItemHandler(newSize, this);

        for (int i = 0; i < oldSize && i < newSize; i++) {
            tmpHandler.setStackInSlot(i, droneInventory.getStackInSlot(i));
        }

        // if the inventory has shrunk, eject any excess items
        for (int i = newSize; i < oldSize; i++) {
            ItemStack stack = droneInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                PneumaticCraftUtils.dropItemOnGround(stack, getWorld(), getPos().up());
            }
        }

        droneInventory = tmpHandler;
        itemHandlerSideConfigurator.updateHandler("droneInv", droneInventory);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        inventory.deserializeNBT(tag.getCompoundTag("Items"));
        tank.readFromNBT(tag.getCompoundTag("tank"));
        droneInventory = new DroneItemHandler(getDroneSlots(), this);
        droneInventory.deserializeNBT(tag.getCompoundTag("droneItems"));
        ownerID = tag.hasKey("ownerID") ? UUID.fromString(tag.getString("ownerID")) : FALLBACK_UUID;
        ownerName = tag.hasKey("ownerName") ? tag.getString("ownerName") : FALLBACK_NAME;
        itemHandlerSideConfigurator.updateHandler("droneInv", droneInventory);

        if (getDroneSlots() != droneInventory.getSlots()) {
            Log.warning("drone inventory size mismatch: dispenser upgrades = " + getDroneSlots() + ", saved inv size = " + droneInventory.getSlots());
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        tag.setTag("Items", inventory.serializeNBT());

        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag);
        tag.setTag("tank", tankTag);

        ItemStackHandler handler = new ItemStackHandler(getFakePlayer().inventory.getSizeInventory());
        for (int i = 0; i < handler.getSlots(); i++) {
            handler.setStackInSlot(i, getFakePlayer().inventory.getStackInSlot(i));
        }
        tag.setTag("droneItems", handler.serializeNBT());

        tag.setString("ownerID", ownerID.toString());
        tag.setString("ownerName", ownerName);

        return tag;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandlerSideConfigurator.getHandler(facing) != null;
        } else {
            return super.hasCapability(capability, facing);
        }
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tank);
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandlerSideConfigurator.getHandler(facing));
        } else {
            return super.getCapability(capability, facing);
        }
    }

    @Override
    protected void onFirstServerUpdate() {
        super.onFirstServerUpdate();
        SideConfigurator.validateBlockRotation(this);
        calculateUpgrades();
        inventory.onContentsChanged(0);  // force initial read of any installed drone/network api
        curX = targetX = getPos().getX() + 0.5;
        curY = targetY = getPos().getY() + 0.6;
        curZ = targetZ = getPos().getZ() + 0.5;

        MinecraftForge.EVENT_BUS.register(this);
    }

    private int getDroneSlots() {
        return world != null && world.isRemote ? 0 : Math.min(36, 1 + dispenserUpgrades);
    }

    private static boolean isProgrammableAndValidForDrone(IDroneBase drone, ItemStack programmable) {
        if (programmable.getItem() instanceof IProgrammable && ((IProgrammable) programmable.getItem()).canProgram(programmable) && ((IProgrammable) programmable.getItem()).usesPieces(programmable)) {
            List<IProgWidget> widgets = TileEntityProgrammer.getProgWidgets(programmable);
            for (IProgWidget widget : widgets) {
                if (!drone.isProgramApplicable(widget)) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public float getMinWorkingPressure() {
        return 3;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public float getPressure(ItemStack iStack) {
        return getPressure();
    }

    @Override
    public void addAir(ItemStack iStack, int amount) {
        addAir(amount);
    }

    @Override
    public float maxPressure(ItemStack iStack) {
        return 7;
    }

    @Override
    public int getVolume(ItemStack itemStack) {
        return getAirHandler(null).getVolume();
    }

    @Override
    public World world() {
        return getWorld();
    }

    @Override
    public IFluidTank getTank() {
        return tank;
    }

    @Override
    public IItemHandlerModifiable getInv() {
        return droneInventory;
    }

    @Override
    public Vec3d getDronePos() {
        if (curX == 0 && curY == 0 && curZ == 0) {
            curX = getPos().getX() + 0.5;
            curY = getPos().getY() + 0.6;
            curZ = getPos().getZ() + 0.5;
            targetX = curX;
            targetY = curY;
            targetZ = curZ;
        }
        return new Vec3d(curX, curY, curZ);
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
                return moveToXYZ(entity.posX, entity.posY + 0.3, entity.posZ);
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
        Vec3d pos = getDronePos();
        getWorld().spawnEntity(new EntityItem(getWorld(), pos.x, pos.y, pos.z, stack));
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
    public boolean isProgramApplicable(IProgWidget widget) {
        return !BLACKLISTED_WIDGETS.contains(widget.getWidgetString());
    }

    @Override
    public EntityAITasks getTargetAI() {
        return null;
    }

    @Override
    public void setEmittingRedstone(EnumFacing orientation, int emittingRedstone) {
        redstoneLevels[orientation.ordinal()] = emittingRedstone;
        updateNeighbours();
    }

    public int getEmittingRedstone(EnumFacing direction) {
        return redstoneLevels[direction.ordinal()];
    }

    @Override
    public void setName(String string) {
        if (drone != null) {
            drone.setCustomNameTag(string);
        }
        ItemStack stack = inventory.getStackInSlot(0).copy();
        if (!stack.isEmpty()) {
            stack.setStackDisplayName(string);
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
    public boolean isAIOverriden() {
        return false;
    }

    @Override
    public void onItemPickupEvent(EntityItem curPickingUpEntity, int stackSize) {
    }

    @Override
    public void overload(String msgKey, Object... params) {
        NetworkHandler.sendToAllAround(
                new PacketSpawnParticle(EnumParticleTypes.SMOKE_LARGE,
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
}
