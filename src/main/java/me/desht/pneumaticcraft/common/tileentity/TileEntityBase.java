package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.common.config.Config;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.item.ItemMachineUpgrade;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.thirdparty.IHeatDisperser;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.TileEntityCache;
import me.desht.pneumaticcraft.lib.NBTKeys;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = ModIds.COMPUTERCRAFT)
//})
public abstract class TileEntityBase extends TileEntity implements IGUIButtonSensitive, IDescSynced, IUpgradeAcceptor /*, IPeripheral*/ {
    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "gui.tab.redstoneBehaviour.button.anySignal",
            "gui.tab.redstoneBehaviour.button.highSignal",
            "gui.tab.redstoneBehaviour.button.lowSignal"
    );

    private static final List<IHeatDisperser> moddedDispersers = new ArrayList<>();

    private final Set<Item> applicableUpgrades = new HashSet<>();
    private final Set<String> applicableCustomUpgrades = new HashSet<>();
    private final UpgradeCache upgradeCache = new UpgradeCache(this);

    private final UpgradeHandler upgradeHandler;
    boolean firstRun = true;  // True only the first time updateEntity invokes in a session
    int poweredRedstone; // The redstone strength currently applied to the block.
    private boolean descriptionPacketScheduled;
    private List<SyncedField> descriptionFields;
    private TileEntityCache[] tileCache;
    private BlockState cachedBlockState;
    private boolean preserveStateOnBreak = false; // set to true if shift-wrenched to keep upgrades in the block
    private float actualSpeedMult = PneumaticValues.DEF_SPEED_UPGRADE_MULTIPLIER;
    private float actualUsageMult = PneumaticValues.DEF_SPEED_UPGRADE_USAGE_MULTIPLIER;
//    private LuaMethodRegistry luaMethodRegistry = null;

    public TileEntityBase(TileEntityType type) {
        this(type, 0);
    }

    public TileEntityBase(TileEntityType type, int upgradeSize) {
        super(type);

        this.upgradeHandler = new UpgradeHandler(upgradeSize);
    }

    @Override
    public String getUpgradeAcceptorTranslationKey() {
        return getBlockTranslationKey();
    }

    private static String makeUpgradeKey(ItemStack stack) {
        return stack.getItem().getRegistryName().toString();
    }

    protected void addApplicableUpgrade(IItemRegistry.EnumUpgrade... upgrades) {
        for (IItemRegistry.EnumUpgrade upgrade : upgrades)
            addApplicableUpgrade(ModItems.Registration.UPGRADES.get(upgrade));
    }

    protected void addApplicableUpgrade(Item upgrade) {
        applicableUpgrades.add(upgrade);
    }

    protected void addApplicableCustomUpgrade(ItemStack... upgrades) {
        for (ItemStack upgrade : upgrades) {
            applicableCustomUpgrades.add(makeUpgradeKey(upgrade));
        }
    }

    private String getBlockTranslationKey() {
        return "block.pneumaticcraft." + getType().getRegistryName().getPath();
    }

    /**
     * Call this from {@link INamedContainerProvider#getDisplayName()}
     * @return display name for this TE's GUI
     */
    ITextComponent getDisplayNameInternal() {
        return new TranslationTextComponent(getBlockTranslationKey());
    }

    // server side, chunk sending
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT compound = super.getUpdateTag();
        return new PacketDescription(this).writeNBT(compound);
    }

    // client side, chunk sending
    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        super.handleUpdateTag(tag);

        new PacketDescription(tag).process();
    }

    /***********
       We don't override getUpdatePacket() or onDataPacket() because TE sync'ing is all handled
       by our custom PacketDescription and the @DescSynced system
     ***********/

    @Override
    public BlockPos getPosition() {
        return getPos();
    }

    @Override
    public List<SyncedField> getDescriptionFields() {
        if (descriptionFields == null) {
            descriptionFields = NetworkUtils.getSyncedFields(this, DescSynced.class);
            for (SyncedField field : descriptionFields) {
                field.update();
            }
        }
        return descriptionFields;
    }

    public void sendDescriptionPacket() {
        sendDescriptionPacket(256);
    }

    void sendDescriptionPacket(double maxPacketDistance) {
        NetworkHandler.sendToAllAround(new PacketDescription(this), world, maxPacketDistance);
    }

    /**
     * A way to safely mark a block for an update from another thread (like the CC Lua thread).
     */
    void scheduleDescriptionPacket() {
        descriptionPacketScheduled = true;
    }

    /**
     * A way of dispersing heat to other mods which have their own heat API.
     *
     * @param disperser a heat disperser adapter object
     */
    public static void registerHeatDisperser(IHeatDisperser disperser) {
        moddedDispersers.add(disperser);
    }

    /*
     * Even though this class doesn't implement ITickableTileEntity, we'll keep the base update() logic here; classes
     * which extend non-tickable subclasses might need it (e.g. TileEntityPressureChamberInterface)
     */
    void tickImpl() {
        if (firstRun && !world.isRemote) {
            onFirstServerUpdate();
            onNeighborTileUpdate();
            onNeighborBlockUpdate();
        }
        firstRun = false;

        if (!world.isRemote) {
            if (this instanceof IHeatExchanger) {
                ((IHeatExchanger) this).getHeatExchangerLogic(null).tick();
                for (IHeatDisperser disperser : moddedDispersers) {
                    disperser.disperseHeat(this, tileCache);
                }
            }

            if (this instanceof IAutoFluidEjecting && getUpgrades(IItemRegistry.EnumUpgrade.DISPENSER) > 0) {
                ((IAutoFluidEjecting) this).autoExportFluid(this);
            }

            if (descriptionFields == null) descriptionPacketScheduled = true;
            for (SyncedField field : getDescriptionFields()) {
                if (field.update()) {
                    descriptionPacketScheduled = true;
                }
            }

            if (descriptionPacketScheduled) {
                descriptionPacketScheduled = false;
                sendDescriptionPacket();
            }
        }
    }

    protected void onFirstServerUpdate() {
        initializeIfHeatExchanger();
    }

    protected void updateNeighbours() {
        world.notifyNeighborsOfStateChange(getPos(), getBlockState().getBlock());
    }

    public void onBlockRotated() {
        if (this instanceof ISideConfigurable) {
            for (SideConfigurator sc : ((ISideConfigurable) this).getSideConfigurators()) {
                sc.setupFacingMatrix();
            }
        }
    }

    void rerenderTileEntity() {
        world.markForRerender(getPos());
    }

    protected boolean shouldRerenderChunkOnDescUpdate() {
        return this instanceof ICamouflageableTE;
    }

    /**
     * Encoded into the description packet. Also included in saved data written by write().
     *
     * Prefer to use @DescSynced - only use this for complex fields not handled by @DescSynced,
     * or for non-ticking tile entities.
     *
     * @param tag NBT tag
     */
    @Override
    public void writeToPacket(CompoundNBT tag) {
        if (this instanceof ISideConfigurable) {
            tag.put(NBTKeys.SIDE_CONFIGURATION, SideConfigurator.writeToNBT((ISideConfigurable) this));
        }
    }

    /**
     * Encoded into the description packet. Also included in saved data read by read().
     *
     * Prefer to use @DescSynced - only use this for complex fields not handled by @DescSynced,
     * or for non-ticking tile entities.
     *
     * @param tag NBT tag
     */
    @Override
    public void readFromPacket(CompoundNBT tag) {
        if (this instanceof ISideConfigurable) {
            SideConfigurator.readFromNBT(tag.getCompound(NBTKeys.SIDE_CONFIGURATION), (ISideConfigurable) this);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        if (upgradeHandler.getSlots() > 0) {
            tag.put(NBTKeys.NBT_UPGRADE_INVENTORY, upgradeHandler.serializeNBT());
        }
        if (this instanceof IHeatExchanger) {
            tag.put(NBTKeys.NBT_HEAT_EXCHANGER, ((IHeatExchanger) this).getHeatExchangerLogic(null).serializeNBT());
        }
        if (this instanceof ISerializableTanks) {
            tag.put(NBTKeys.NBT_SAVED_TANKS, ((ISerializableTanks) this).serializeTanks());
        }
        writeToPacket(tag);

        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        if (tag.contains(NBTKeys.NBT_UPGRADE_INVENTORY) && upgradeHandler != null) {
            upgradeHandler.deserializeNBT(tag.getCompound(NBTKeys.NBT_UPGRADE_INVENTORY));
        }
        if (this instanceof IHeatExchanger) {
            ((IHeatExchanger) this).getHeatExchangerLogic(null).deserializeNBT(tag.getCompound(NBTKeys.NBT_HEAT_EXCHANGER));
        }
        if (this instanceof ISerializableTanks) {
            ((ISerializableTanks) this).deserializeTanks(tag.getCompound(NBTKeys.NBT_SAVED_TANKS));
        }
        readFromPacket(tag);
    }

    @Override
    public void validate() {
        super.validate();
        scheduleDescriptionPacket();
    }

    @Override
    public void onDescUpdate() {
        if (shouldRerenderChunkOnDescUpdate()) {
            rerenderTileEntity();
        }
    }

    /**
     * Called when a key is synced in the container.
     */
    public void onGuiUpdate() {
    }

    public Direction getRotation() {
        if (cachedBlockState == null) {
            cachedBlockState = world.getBlockState(getPos());
        }
        return cachedBlockState.get(BlockPneumaticCraft.ROTATION);
    }

    @Override
    public void updateContainingBlockInfo() {
        cachedBlockState = null;
        super.updateContainingBlockInfo();
    }

    public int getUpgrades(Item upgrade) {
        int upgrades = 0;
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            ItemStack stack = upgradeHandler.getStackInSlot(i);
            if (stack.getItem() == upgrade) {
                upgrades += stack.getCount();
            }
        }
        return upgrades;
    }

    public int getUpgrades(IItemRegistry.EnumUpgrade upgrade) {
        return upgradeCache.getUpgrades(upgrade);
    }

    protected int getCustomUpgrades(ItemStack upgradeStack) {
        return upgradeCache.getUpgrades(upgradeStack);
    }

    public float getSpeedMultiplierFromUpgrades() {
        return actualSpeedMult;
    }

    public float getSpeedUsageMultiplierFromUpgrades() {
        return actualUsageMult;
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
    }

    public boolean isGuiUseableByPlayer(PlayerEntity player) {
        return getWorld().getTileEntity(getPos()) == this && player.getDistanceSq(getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D) <= 64.0D;
    }

    public void onNeighborTileUpdate() {
        initializeIfHeatExchanger();
        for (TileEntityCache cache : getTileCache()) {
            cache.update();
        }
    }

    public TileEntityCache[] getTileCache() {
        if (tileCache == null) tileCache = TileEntityCache.getDefaultCache(getWorld(), getPos());
        return tileCache;
    }

    TileEntity getCachedNeighbor(Direction dir) {
        return getTileCache()[dir.getIndex()].getTileEntity();
    }

    public void onNeighborBlockUpdate() {
        poweredRedstone = PneumaticCraftUtils.getRedstoneLevel(getWorld(), getPos());
        initializeIfHeatExchanger();
        for (TileEntityCache cache : getTileCache()) {
            cache.update();
        }
    }

    public boolean redstoneAllows() {
        if (getWorld().isRemote) onNeighborBlockUpdate();
        switch (((IRedstoneControl) this).getRedstoneMode()) {
            case 0:
                return true;
            case 1:
                return poweredRedstone > 0;
            case 2:
                return poweredRedstone == 0;
        }
        return false;
    }

    protected void initializeIfHeatExchanger() {
        if (this instanceof IHeatExchanger) {
            initializeHeatExchanger(((IHeatExchanger) this).getHeatExchangerLogic(null), getConnectedHeatExchangerSides());
        }
    }

    void initializeHeatExchanger(IHeatExchangerLogic heatExchanger, Direction... connectedSides) {
        heatExchanger.initializeAsHull(getWorld(), getPos(), connectedSides);
    }

    /**
     * Gets the valid sides for heat exchanging to be allowed. returning an empty array will allow any side.
     *
     * @return an array of valid sides
     */
    protected Direction[] getConnectedHeatExchangerSides() {
        return new Direction[0];
    }

    @Override
    public Type getSyncType() {
        return Type.TILE_ENTITY;
    }

    /**
     * Take a fluid-containing from the input slot, use it to fill the primary input tank of the tile entity,
     * and place the resulting emptied container in the output slot.
     *
     * @param inputSlot input slot
     * @param outputSlot output slot
     */
    void processFluidItem(int inputSlot, int outputSlot) {
        // todo 1.14 fluids
//        if (!hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
//                || !hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
//            return;
//        IItemHandler itemHandler = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
//
//        ItemStack fluidContainer = itemHandler.getStackInSlot(inputSlot);
//        IFluidHandlerItem fluidHandlerItem = FluidUtil.getFluidHandler(fluidContainer);
//        if (fluidHandlerItem == null) {
//            return;
//        }
//        if (fluidContainer.getCount() > 1) {
//            FluidStack stack = fluidHandlerItem.drain(1, false);
//            if (stack != null && stack.amount > 0) {
//                // disallow multiple filled items (shouldn't normally happen anyway but let's be paranoid)
//                return;
//            } else {
//                // multiple empty items OK, but be sure to only fill one of them...
//                ItemStack itemToFill = fluidContainer.copy();
//                itemToFill.setCount(1);
//                fluidHandlerItem = FluidUtil.getFluidHandler(fluidContainer);
//            }
//        }
//
//        IFluidHandler fluidHandler = getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
//
//        FluidStack itemContents = fluidHandlerItem.drain(1000, false);
//        if (itemContents != null && itemContents.amount > 0) {
//            // input item contains fluid: drain from input item into tank, move to output if empty
//            FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandler, fluidHandlerItem, itemContents.amount, true);
//            if (transferred != null && transferred.amount == itemContents.amount) {
//                // all transferred; move empty container to output if possible
//                ItemStack emptyContainerStack = fluidHandlerItem.getContainer();
//                ItemStack excess = itemHandler.insertItem(outputSlot, emptyContainerStack, false);
//                if (excess.isEmpty()) {
//                    itemHandler.extractItem(inputSlot, 1, false);
//                }
//            }
//        } else if (itemHandler.getStackInSlot(outputSlot).isEmpty()) {
//            // input item(s) is/are empty: drain from tank to one input item, move to output
//            FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandlerItem, fluidHandler, Integer.MAX_VALUE, true);
//            if (transferred != null && transferred.amount > 0) {
//                itemHandler.extractItem(inputSlot, 1, false);
//                ItemStack filledContainerStack = fluidHandlerItem.getContainer();
//                itemHandler.insertItem(outputSlot, filledContainerStack, false);
//            }
//        }
    }


    @Override
    public Set<Item> getApplicableUpgrades() {
        return applicableUpgrades;
    }

//    @Override
//    public boolean shouldRefresh(World world, BlockPos pos, BlockState oldState, BlockState newSate) {
//        return oldState.getBlock() != newSate.getBlock();
//    }

    protected void addLuaMethods(LuaMethodRegistry registry) {
        if (this instanceof IHeatExchanger) {
            final IHeatExchanger exchanger = (IHeatExchanger) this;
            registry.registerLuaMethod(new LuaMethod("getTemperature") {
                @Override
                public Object[] call(Object[] args) {
                    requireArgs(args, 0, 1, "face? (down/up/north/south/west/east)");
                    if (args.length == 0) {
                        return new Object[]{exchanger.getHeatExchangerLogic(null).getTemperature()};
                    } else  {
                        IHeatExchangerLogic logic = exchanger.getHeatExchangerLogic(getDirForString((String) args[0]));
                        return new Object[]{logic != null ? logic.getTemperature() : 0};
                    }
                }
            });
        }
    }

    // todo 1.14 computercraft
//    private LuaMethodRegistry getLuaMethodRegistry() {
//        if (luaMethodRegistry == null) {
//            luaMethodRegistry = new LuaMethodRegistry();
//            addLuaMethods(luaMethodRegistry);
//        }
//        return luaMethodRegistry;
//    }
//
//    @Override
//    public String getType() {
//        return getBlockType().getTranslationKey().substring(5);
//    }
//
//    @Override
//    public String[] getMethodNames() {
//        return getLuaMethodRegistry().getMethodNames();
//    }
//
//    public Object[] callLuaMethod(String methodName, Object... args) throws Exception {
//        return getLuaMethodRegistry().getMethod(methodName).call(args);
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
//    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException {
//        try {
//            return getLuaMethodRegistry().getMethod(method).call(arguments);
//        } catch (Exception e) {
//            throw new LuaException(e.getMessage());
//        }
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
//    public void attach(IComputerAccess computer) {
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
//    public void detach(IComputerAccess computer) {
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
//    public boolean equals(IPeripheral other) {
//        if (other == null) {
//            return false;
//        }
//        if (this == other) {
//            return true;
//        }
//        if (other instanceof TileEntity) {
//            TileEntity otherTE = (TileEntity) other;
//            return otherTE.getWorld().equals(getWorld()) && otherTE.getPos().equals(getPos());
//        }
//
//        return false;
//    }

    public abstract IItemHandlerModifiable getPrimaryInventory();

    @Nonnull
    public LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return LazyOptional.empty();
    }

    public UpgradeHandler getUpgradeHandler() {
        return upgradeHandler;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return getInventoryCap().cast();
//        } else if (cap == Mekanism.CAPABILITY_HEAT_TRANSFER && this instanceof IHeatExchanger) {
//            return Mekanism.getHeatAdapter(this, side).cast();
        }
        return super.getCapability(cap, side);
    }


    /**
     * Collect all items which should be dropped when this TE is broken.  Override and extend this in subclassing
     * TE's which have extra inventories to be dropped.
     *
     * @param drops list in which to collect dropped items
     */
    public void getContentsToDrop(NonNullList<ItemStack> drops) {
        getInventoryCap().ifPresent(h -> {
            for (int i = 0; i < h.getSlots(); i++) {
                if (!h.getStackInSlot(i).isEmpty()) {
                    drops.add(h.getStackInSlot(i));
                }
            }
        });

        if (!shouldPreserveStateOnBreak()) {
            for (int i = 0; i < upgradeHandler.getSlots(); i++) {
                if (!upgradeHandler.getStackInSlot(i).isEmpty()) {
                    drops.add(upgradeHandler.getStackInSlot(i));
                }
            }
        }

        if (this instanceof ICamouflageableTE) {
            BlockState camoState = ((ICamouflageableTE) this).getCamouflage();
            if (camoState != null) {
                drops.add(ICamouflageableTE.getStackForState(camoState));
            }
        }
    }

    /**
     * Carry out any tasks which need a world object (the world is null in the TE constructor)
     */
    public void onTileEntityCreated() {
    }

    public final String getRedstoneButtonText(int mode) {
        try {
            return getRedstoneButtonLabels().get(mode);
        } catch (ArrayIndexOutOfBoundsException e) {
            return "<ERROR>";
        }
    }

    protected List<String> getRedstoneButtonLabels() {
        return REDSTONE_LABELS;
    }

    public int getRedstoneModeCount() {
        return getRedstoneButtonLabels().size();
    }

    public String getRedstoneTabTitle() {
        return this instanceof IRedstoneControlled ? "gui.tab.redstoneBehaviour.enableOn" : "gui.tab.redstoneBehaviour.emitRedstoneWhen";
    }

    /**
     * Should this tile entity preserve its state (currently: upgrades and stored air) when broken?
     * By default this is true when sneak-wrenched, and false when broken by pick.
     *
     * @return true if state should be preserved, false otherwise
     */
    public boolean shouldPreserveStateOnBreak() {
        return preserveStateOnBreak;
    }

    public void setPreserveStateOnBreak(boolean preserveStateOnBreak) {
        this.preserveStateOnBreak = preserveStateOnBreak;
    }

    /**
     * Called when a machine's upgrades have changed in any way.  This is also called from readNBT() when saved upgrades
     * are deserialized, so it is not guaranteed that the world field is non-null - beware.  If you override this,
     * remember to call the super method!
     */
    protected void onUpgradesChanged() {
        actualSpeedMult = (float) Math.pow(Config.Common.Machines.speedUpgradeSpeedMultiplier, Math.min(10, getUpgrades(IItemRegistry.EnumUpgrade.SPEED)));
        actualUsageMult = (float) Math.pow(Config.Common.Machines.speedUpgradeUsageMultiplier, Math.min(10, getUpgrades(IItemRegistry.EnumUpgrade.SPEED)));
    }

    public UpgradeCache getUpgradeCache() {
        return upgradeCache;
    }

    public class UpgradeHandler extends BaseItemStackHandler {
        UpgradeHandler(int upgradeSize) {
            super(TileEntityBase.this, upgradeSize);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty()
                    || applicableUpgrades.contains(itemStack.getItem())
                    || applicableCustomUpgrades.contains(makeUpgradeKey(itemStack));
        }

        @Override
        protected void onContentsChanged(int slot) {
            upgradeCache.invalidate();
        }
    }

    public class UpgradeCache {
        private final int[] upgradeCount = new int[IItemRegistry.EnumUpgrade.values().length];
        private Map<String,Integer> customUpgradeCount;
        private final TileEntityBase te;
        private boolean isValid = false;
        private Direction ejectDirection;

        UpgradeCache(TileEntityBase te) {
            this.te = te;
        }

        void validate() {
            if (isValid) return;

            Arrays.fill(upgradeCount, 0);
            customUpgradeCount = null;
            ejectDirection = null;
            for (int i = 0; i < upgradeHandler.getSlots(); i++) {
                ItemStack stack = upgradeHandler.getStackInSlot(i);
                if (stack.getItem() instanceof ItemMachineUpgrade) {
                    // native upgrade
                    IItemRegistry.EnumUpgrade type = ((ItemMachineUpgrade) stack.getItem()).getUpgradeType();
                    upgradeCount[type.ordinal()] += upgradeHandler.getStackInSlot(i).getCount();
                    if (type == IItemRegistry.EnumUpgrade.DISPENSER && stack.hasTag()) {
                        ejectDirection = Direction.byName(NBTUtil.getString(stack, ItemMachineUpgrade.NBT_DIRECTION));
                    }
                } else if (!upgradeHandler.getStackInSlot(i).isEmpty()) {
                    // custom upgrade from another mod
                    if (customUpgradeCount == null)
                        customUpgradeCount = Maps.newHashMap();
                    String key = makeUpgradeKey(stack);
                    customUpgradeCount.put(key, customUpgradeCount.getOrDefault(key, 0) + stack.getCount());
                }
            }
            te.onUpgradesChanged();
            isValid = true;
        }

        /**
         * Mark the upgrade cache as invalid.  It will be revalidated at the start of the next update tick for the TE.
         */
        public void invalidate() {
            isValid = false;
        }

        public int getUpgrades(IItemRegistry.EnumUpgrade type) {
            validate();
            return upgradeCount[type.ordinal()];
        }

        public int getUpgrades(ItemStack stack) {
            validate();
            return customUpgradeCount == null ? 0 : customUpgradeCount.getOrDefault(makeUpgradeKey(stack), 0);
        }

        Direction getEjectDirection() {
            return ejectDirection;
        }
    }
}
