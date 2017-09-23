package me.desht.pneumaticcraft.common.tileentity;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.drone.DroneConstructingEvent;
import me.desht.pneumaticcraft.api.drone.IPathNavigator;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.common.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.ai.FakePlayerItemInWorldManager;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone.DroneFakePlayer;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.ProgWidgetCC;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityProgrammableController extends TileEntityPneumaticBase implements IMinWorkingPressure, IDroneBase {
    private static final int INVENTORY_SIZE = 1;

    private ProgrammableItemStackHandler inventory;

    private final FluidTank tank = new FluidTank(16000);
    private DroneAIManager aiManager;
    private DroneFakePlayer fakePlayer;
    private ItemStackHandler droneItems;
//    private final Map<String, IExtendedEntityProperties> properties = new HashMap<String, IExtendedEntityProperties>();
    private List<IProgWidget> progWidgets = new ArrayList<IProgWidget>();
    private final int[] redstoneLevels = new int[6];
    private String droneName = "";
    @DescSynced
    private double targetX, targetY, targetZ;
    @DescSynced
    @LazySynced
    private double curX, curY, curZ;
    public double oldCurX, oldCurY, oldCurZ;
    private EntityProgrammableController drone;
    @DescSynced
    private int diggingX, diggingY, diggingZ;
    private int dispenserUpgrades, speedUpgrades;

    private static final Set<Class<? extends IProgWidget>> WIDGET_BLACKLIST = new HashSet<Class<? extends IProgWidget>>();

    static {
        WIDGET_BLACKLIST.add(ProgWidgetCC.class);
        WIDGET_BLACKLIST.add(ProgWidgetEntityAttack.class);
        WIDGET_BLACKLIST.add(ProgWidgetDroneConditionEntity.class);
        WIDGET_BLACKLIST.add(ProgWidgetStandby.class);
        WIDGET_BLACKLIST.add(ProgWidgetSuicide.class);
        WIDGET_BLACKLIST.add(ProgWidgetTeleport.class);
        WIDGET_BLACKLIST.add(ProgWidgetEntityExport.class);
        WIDGET_BLACKLIST.add(ProgWidgetEntityImport.class);
    }

    public TileEntityProgrammableController() {
        super(5, 7, 5000, 4);
        inventory = new ProgrammableItemStackHandler();
        addApplicableUpgrade(EnumUpgrade.SPEED, EnumUpgrade.DISPENSER);
        MinecraftForge.EVENT_BUS.post(new DroneConstructingEvent(this));
    }

    @Override
    public void update() {
        super.update();

        oldCurX = curX;
        oldCurY = curY;
        oldCurZ = curZ;
        if (PneumaticCraftUtils.distBetween(getPos(), targetX, targetY, targetZ) <= getSpeed()) {
            curX = targetX;
            curY = targetY;
            curZ = targetZ;
        } else {
            Vec3d vec = new Vec3d(targetX - curX, targetY - curY, targetZ - curZ).normalize();
            curX += vec.x * getSpeed();
            curY += vec.y * getSpeed();
            curZ += vec.z * getSpeed();
        }

        if (!getWorld().isRemote) {
            getAIManager();
            if (getWorld().getTotalWorldTime() % 40 == 0) {
                dispenserUpgrades = getUpgrades(EnumUpgrade.DISPENSER);
                speedUpgrades = getUpgrades(EnumUpgrade.SPEED);

                for (int i = getDroneSlots(); i < 36; i++) {
                    ItemStack stack = getFakePlayer().inventory.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        getWorld().spawnEntity(new EntityItem(getWorld(), getPos().getX() + 0.5, getPos().getY() + 1.5, getPos().getZ() + 0.5, stack));
                        getFakePlayer().inventory.setInventorySlotContents(i, ItemStack.EMPTY);
                    }
                }

                tank.setCapacity((dispenserUpgrades + 1) * 16000);
                if (tank.getFluidAmount() > tank.getCapacity()) {
                    tank.getFluid().amount = tank.getCapacity();
                }
            }
            for (int i = 0; i < 4; i++) {
                getFakePlayer().interactionManager.updateBlockRemoving();
            }
            if (getPressure() >= getMinWorkingPressure()) {
                if (!aiManager.isIdling()) addAir(-10);
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
            // drone.getMoveHelper().setMoveTo(curX, curY, curZ, 0);
            /*   drone.prevPosX = oldCurX;
            drone.prevPosY = oldCurY;
            drone.prevPosZ = oldCurZ;*/
            //drone.getMoveHelper().setMoveTo(curX, curY, curZ, getSpeed());
        }
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

    private void initializeFakePlayer() {
        String playerName = "Drone";
        fakePlayer = new DroneFakePlayer((WorldServer) getWorld(), new GameProfile(null, playerName), new FakePlayerItemInWorldManager(getWorld(), fakePlayer, this), this);
        fakePlayer.connection = new NetHandlerPlayServer(FMLCommonHandler.instance().getMinecraftServerInstance(), new NetworkManager(EnumPacketDirection.SERVERBOUND), fakePlayer);
        fakePlayer.inventory = new InventoryPlayer(fakePlayer) {
            private ItemStack oldStack;

            @Override
            public int getSizeInventory() {
                return getDroneSlots();
            }

            @Override
            public void setInventorySlotContents(int slot, ItemStack stack) {
                super.setInventorySlotContents(slot, stack);
                if (slot == 0) {
                    for (EntityEquipmentSlot ee : EntityEquipmentSlot.values()) {
                        if (!oldStack.isEmpty()) {
                            getFakePlayer().getAttributeMap().removeAttributeModifiers(oldStack.getAttributeModifiers(ee));
                        }
                        if (!stack.isEmpty()) {
                            getFakePlayer().getAttributeMap().applyAttributeModifiers(stack.getAttributeModifiers(ee));
                        }
                    }
                    oldStack = stack;
                }
            }
        };
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {

    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    private class ProgrammableItemStackHandler extends FilteredItemStackHandler {
        ProgrammableItemStackHandler() {
            super(INVENTORY_SIZE);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            ItemStack stack = getStackInSlot(slot);
            if (!stack.isEmpty() && isProgrammableAndValidForDrone(TileEntityProgrammableController.this, stack)) {
                progWidgets = TileEntityProgrammer.getProgWidgets(stack);
                if (!getWorld().isRemote) getAIManager().setWidgets(progWidgets);
            } else {
                progWidgets.clear();
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
            getAIManager();
        }

        @Override
        public boolean test(Integer integer, ItemStack itemStack) {
            return isProgrammableAndValidForDrone(TileEntityProgrammableController.this, itemStack);
        }
    }

    @Override
    public String getName() {
        return Blockss.PROGRAMMABLE_CONTROLLER.getUnlocalizedName();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        inventory.deserializeNBT(tag.getCompoundTag("Items"));
        tank.readFromNBT(tag.getCompoundTag("tank"));
        droneItems = new ItemStackHandler(getDroneSlots());
        droneItems.deserializeNBT(tag.getCompoundTag("droneItems"));

//        NBTTagList extendedList = tag.getTagList("extendedProperties", 10);
//        for (int i = 0; i < extendedList.tagCount(); ++i) {
//            NBTTagCompound propertyTag = extendedList.getCompoundTagAt(i);
//            String key = propertyTag.getString("key");
//            IExtendedEntityProperties property = properties.get(key);
//            if (property != null) {
//                property.loadNBTData(propertyTag);
//            } else {
//                Log.warning("Extended entity property \"" + key + "\" doesn't exist in a Programmable Controller");
//            }
//        }
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

//        NBTTagList extendedList = new NBTTagList();
//        for (Map.Entry<String, IExtendedEntityProperties> entry : properties.entrySet()) {
//            NBTTagCompound propertyTag = new NBTTagCompound();
//            propertyTag.setString("key", entry.getKey());
//            entry.getValue().saveNBTData(propertyTag);
//            extendedList.appendTag(propertyTag);
//        }
//        tag.setTag("extendedProperties", extendedList);

        return tag;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tank);
        } else {
            return super.getCapability(capability, facing);
        }
    }

    @Override
    protected void onFirstServerUpdate() {
        super.onFirstServerUpdate();
        inventory.setStackInSlot(0, inventory.getStackInSlot(0));
    }

    private int getDroneSlots() {
        return getWorld().isRemote ? 0 : Math.min(36, 1 + dispenserUpgrades);
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
    public World world() {
        return getWorld();
    }

    @Override
    public IFluidTank getTank() {
        return tank;
    }

    @Override
    public IItemHandlerModifiable getInv() {
        return inventory;
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
                return targetX == curX && targetY == curY && targetZ == curZ;
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
    public EntityPlayerMP getFakePlayer() {
        if (fakePlayer == null) initializeFakePlayer();
        if (droneItems != null) {
            for (int i = 0; i < droneItems.getSlots(); i++) {
                fakePlayer.inventory.setInventorySlotContents(i, droneItems.getStackInSlot(i).copy());
            }
            droneItems = null;
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
        return !WIDGET_BLACKLIST.contains(widget.getClass());
    }

    @Override
    public EntityAITasks getTargetAI() {
        return null;
    }

//    @Override
//    public IExtendedEntityProperties getProperty(String key) {
//        return properties.get(key);
//    }
//
//    @Override
//    public void setProperty(String key, IExtendedEntityProperties property) {
//        properties.put(key, property);
//    }

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
        droneName = string;
        if (drone != null) {
            drone.setCustomNameTag(droneName);
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

    /*
     * Liquid handling
     */
//
//    @Override
//    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
//        return tank.fill(resource, doFill);
//    }
//
//    @Override
//    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
//        return tank.getFluid() != null && tank.getFluid().isFluidEqual(resource) ? tank.drain(resource.amount, doDrain) : null;
//    }
//
//    @Override
//    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
//        return tank.drain(maxDrain, doDrain);
//    }
//
//    @Override
//    public boolean canFill(EnumFacing from, Fluid fluid) {
//        return true;
//    }
//
//    @Override
//    public boolean canDrain(EnumFacing from, Fluid fluid) {
//        return true;
//    }
//
//    @Override
//    public FluidTankInfo[] getTankInfo(EnumFacing from) {
//        return new FluidTankInfo[]{new FluidTankInfo(tank)};
//    }

    @Override
    public void overload() {
        for (int i = 0; i < 10; i++) {
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(EnumParticleTypes.SMOKE_LARGE, getPos().getX() + getWorld().rand.nextDouble(), getPos().getY() + 1, getPos().getZ() + getWorld().rand.nextDouble(), 0, 0, 0), getWorld());
        }
    }

    @Override
    public DroneAIManager getAIManager() {
        if (aiManager == null && !getWorld().isRemote) {
            aiManager = new DroneAIManager(this, new ArrayList<IProgWidget>());
            aiManager.setWidgets(getProgWidgets());
            aiManager.dontStopWhenEndReached();
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
}
