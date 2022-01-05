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

package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticBase;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.common.util.upgrade.IUpgradeHolder;
import me.desht.pneumaticcraft.common.util.upgrade.UpgradeCache;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.INameable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

public abstract class TileEntityBase extends TileEntity
        implements INameable, IGUIButtonSensitive, IDescSynced, IUpgradeAcceptor, IUpgradeHolder, ILuaMethodProvider {
    private final UpgradeCache upgradeCache = new UpgradeCache(this);
    private final UpgradeHandler upgradeHandler;
    private boolean firstTick = true;
    private List<SyncedField<?>> descriptionFields;
    private final CachedTileNeighbours neighbourCache = new CachedTileNeighbours(this);
    private boolean preserveStateOnBreak = false; // set to true if shift-wrenched to keep upgrades in the block
    private float actualSpeedMult = PneumaticValues.DEF_SPEED_UPGRADE_MULTIPLIER;
    private float actualUsageMult = PneumaticValues.DEF_SPEED_UPGRADE_USAGE_MULTIPLIER;
    private final LuaMethodRegistry luaMethodRegistry = new LuaMethodRegistry(this);
    private ITextComponent customName = null;
    private boolean forceFullSync;
    private BitSet fieldsToSync;  // tracks which synced fields have changed and need to be synced on the next tick

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

    private String getBlockTranslationKey() {
        return "block.pneumaticcraft." + getType().getRegistryName().getPath();
    }

    @Override
    public ITextComponent getName() {
        return customName == null ? new TranslationTextComponent(getBlockTranslationKey()) : customName;
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return customName;
    }

    public void setCustomName(ITextComponent customName) {
        this.customName = customName;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getName();
    }

    // server side, chunk sending
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT compound = super.getUpdateTag();
        return new PacketDescription(this, true).writeNBT(compound);
    }

    // client side, chunk sending
    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        new PacketDescription(tag).process();
    }

    /***********
       We don't override getUpdatePacket() or onDataPacket() because TE sync'ing is all handled
       by our custom PacketDescription and the @DescSynced system
     ***********/

    @Override
    public BlockPos getPosition() {
        return getBlockPos();
    }

    @Override
    public boolean shouldSyncField(int idx) {
        return fieldsToSync.get(idx);
    }

    @Override
    public List<SyncedField<?>> getDescriptionFields() {
        if (descriptionFields == null) {
            descriptionFields = NetworkUtils.getSyncedFields(this, DescSynced.class);
            fieldsToSync = new BitSet(descriptionFields.size());
            for (SyncedField<?> field : descriptionFields) {
                field.update();
            }
        }
        return descriptionFields;
    }

    /**
     * Force a sync of this TE to the client right now.
     */
    public void sendDescriptionPacket() {
        PacketDescription descPacket = new PacketDescription(this, forceFullSync);
        if (descPacket.hasData()) {
            NetworkHandler.sendToAllTracking(descPacket, this);
        }
        fieldsToSync.clear();
        forceFullSync = false;
    }

    /**
     * A way to safely mark a block for an update from another thread (like the CC Lua thread).
     */
    void scheduleDescriptionPacket() {
        forceFullSync = true;
    }

    /*
     * Even though this class doesn't implement ITickableTileEntity, we'll keep the base update() logic here; classes
     * which extend non-tickable subclasses might need it (e.g. TileEntityPressureChamberInterface)
     */
    void tickImpl() {
        if (firstTick && !level.isClientSide) {
            onFirstServerTick();
        }
        firstTick = false;

        upgradeCache.validate();

        if (!level.isClientSide) {
            if (this instanceof IHeatExchangingTE) {
                // tick default heat exchanger; if the TE has other exchangers, they are handled in the subclass
                IHeatExchangerLogic logic = ((IHeatExchangingTE) this).getHeatExchanger();
                if (logic != null) logic.tick();
            }

            if (this instanceof IAutoFluidEjecting && getUpgrades(EnumUpgrade.DISPENSER) > 0) {
                ((IAutoFluidEjecting) this).autoExportFluid(this);
            }

            for (int i = 0; i < getDescriptionFields().size(); i++) {
                if (getDescriptionFields().get(i).update()) {
                    fieldsToSync.set(i);
                }
            }

            if (forceFullSync || !fieldsToSync.isEmpty()) {
                sendDescriptionPacket();
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (getInventoryCap().isPresent()) getInventoryCap().invalidate();
        if (getHeatCap(null).isPresent()) getHeatCap(null).invalidate();
    }

    @Override
    public void setChanged() {
        // overridden to only update neighbours if this TE actually has a useful comparator output
        if (level != null) {
            if (level.isAreaLoaded(worldPosition, 0)) {
                level.getChunkAt(worldPosition).markUnsaved();
            }
            if (this instanceof IComparatorSupport && !this.getBlockState().isAir(this.level, this.worldPosition)) {
                this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
            }
        }
    }

    protected void onFirstServerTick() {
        // TODO 1.17 should be able to replace onFirstServerTick() with onLoad()
        if (this instanceof IHeatExchangingTE) {
            ((IHeatExchangingTE) this).initializeHullHeatExchangers(level, worldPosition);
        }
    }

    protected void updateNeighbours() {
        if (level != null) {
            level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        }
    }

    public void onBlockRotated() {
        if (this instanceof ISideConfigurable) {
            for (SideConfigurator<?> sc : ((ISideConfigurable) this).getSideConfigurators()) {
                sc.setupFacingMatrix();
            }
        }
        if (!level.isClientSide) {
            PneumaticRegistry.getInstance().forceClientShapeRecalculation(level, worldPosition);
        }
    }

    void rerenderTileEntity() {
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 0);
        }
    }

    protected boolean shouldRerenderChunkOnDescUpdate() {
        return this instanceof ICamouflageableTE;
    }

    /**
     * Encoded into the description packet. Also included in saved data written by {@link TileEntityBase#save(CompoundNBT)}
     *
     * Prefer to use @DescSynced where possible - use this either for complex fields not handled by @DescSynced,
     * or for non-ticking tile entities.
     *
     * @param tag NBT tag
     */
    @Override
    public void writeToPacket(CompoundNBT tag) {
        if (this instanceof ISideConfigurable) {
            tag.put(NBTKeys.NBT_SIDE_CONFIG, SideConfigurator.writeToNBT((ISideConfigurable) this));
        }
    }

    /**
     * Encoded into the description packet. Also included in saved data read by {@link TileEntityBase#load(BlockState, CompoundNBT)}.
     *
     * Prefer to use @DescSynced where possible - use this either for complex fields not handled by @DescSynced,
     * or for non-ticking tile entities.
     *
     * @param tag NBT tag
     */
    @Override
    public void readFromPacket(CompoundNBT tag) {
        if (this instanceof ISideConfigurable) {
            SideConfigurator.readFromNBT(tag.getCompound(NBTKeys.NBT_SIDE_CONFIG), (ISideConfigurable) this);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);

        if (customName != null) {
            tag.putString("CustomName", ITextComponent.Serializer.toJson(customName));
        }
        if (getUpgradeHandler().getSlots() > 0) {
            tag.put(NBTKeys.NBT_UPGRADE_INVENTORY, getUpgradeHandler().serializeNBT());
        }
        if (this instanceof IHeatExchangingTE) {
            IHeatExchangerLogic logic = ((IHeatExchangingTE) this).getHeatExchanger();
            if (logic != null) tag.put(NBTKeys.NBT_HEAT_EXCHANGER, logic.serializeNBT());
        }
        if (this instanceof IRedstoneControl) {
            ((IRedstoneControl<?>) this).getRedstoneController().serialize(tag);
        }
        if (this instanceof ISerializableTanks) {
            tag.put(NBTKeys.NBT_SAVED_TANKS, ((ISerializableTanks) this).serializeTanks());
        }
        writeToPacket(tag);

        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        if (tag.contains("CustomName", Constants.NBT.TAG_STRING)) {
            customName = ITextComponent.Serializer.fromJson(tag.getString("CustomName"));
        }
        if (tag.contains(NBTKeys.NBT_UPGRADE_INVENTORY) && getUpgradeHandler() != null) {
            getUpgradeHandler().deserializeNBT(tag.getCompound(NBTKeys.NBT_UPGRADE_INVENTORY));
        }
        if (this instanceof IHeatExchangingTE) {
            IHeatExchangerLogic logic = ((IHeatExchangingTE) this).getHeatExchanger();
            if (logic != null) logic.deserializeNBT(tag.getCompound(NBTKeys.NBT_HEAT_EXCHANGER));
        }
        if (this instanceof IRedstoneControl) {
            ((IRedstoneControl<?>) this).getRedstoneController().deserialize(tag);
        }
        if (this instanceof ISerializableTanks) {
            ((ISerializableTanks) this).deserializeTanks(tag.getCompound(NBTKeys.NBT_SAVED_TANKS));
        }
        readFromPacket(tag);
    }

    @Override
    public void onDescUpdate() {
        if (shouldRerenderChunkOnDescUpdate()) {
            rerenderTileEntity();
            if (this instanceof ICamouflageableTE) requestModelDataUpdate();
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        if (this instanceof ICamouflageableTE) {
            return new ModelDataMap.Builder()
                    .withInitial(BlockPneumaticCraftCamo.BLOCK_ACCESS, level)
                    .withInitial(BlockPneumaticCraftCamo.BLOCK_POS, worldPosition)
                    .withInitial(BlockPneumaticCraftCamo.CAMO_STATE, ((ICamouflageableTE) this).getCamouflage())
                    .build();
        } else {
            return super.getModelData();
        }
    }

    /**
     * Called when a key is synced in the container.
     */
    public void onGuiUpdate() {
    }

    public Direction getRotation() {
        BlockState state = getBlockState();
        return state.getBlock() instanceof BlockPneumaticCraft ? ((BlockPneumaticCraft) state.getBlock()).getRotation(state) : Direction.NORTH;
    }

    public int getUpgrades(EnumUpgrade upgrade) {
        return upgradeCache.getUpgrades(upgrade);
    }

    public float getSpeedMultiplierFromUpgrades() {
        return actualSpeedMult;
    }

    public float getSpeedUsageMultiplierFromUpgrades() {
        return actualUsageMult;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
    }

    public boolean isGuiUseableByPlayer(PlayerEntity player) {
        return level != null && level.getBlockEntity(getBlockPos()) == this
                && player.distanceToSqr(Vector3d.atCenterOf(getBlockPos())) <= 64.0D;
    }

    public TileEntity getCachedNeighbor(Direction dir) {
        // don't attempt to cache client-side; we don't get neighbour block updates there so can't reliably clear the cache
        if (level == null) return null;

        return level.isClientSide ?
                level.getBlockEntity(worldPosition.relative(dir)) :
                neighbourCache.getCachedNeighbour(dir);
    }

    /**
     * Called when a neighboring tile entity changes state (specifically when
     * {@link net.minecraft.world.World#updateNeighbourForOutputSignal(BlockPos, Block)} is called.
     * @param tilePos the blockpos of the neighboring tile entity
     */
    public void onNeighborTileUpdate(BlockPos tilePos) {
    }

    /**
     * Called when a neighboring block changes state (i.e. blockstate update)
     * @param fromPos the blockpos of the block that caused this update
     */
    public void onNeighborBlockUpdate(BlockPos fromPos) {
        if (this instanceof IHeatExchangingTE) {
            ((IHeatExchangingTE) this).initializeHullHeatExchangers(level, worldPosition);
        }
        if (this instanceof IRedstoneControl) {
            ((IRedstoneControl<?>)this).getRedstoneController().updateRedstonePower();
        }
        neighbourCache.purge();
    }

    /**
     * Take a fluid-containing from the input slot, use it to fill the primary input tank of the tile entity,
     * and place the resulting emptied container in the output slot.
     *
     * @param inputSlot input slot
     * @param outputSlot output slot
     */
    void processFluidItem(int inputSlot, int outputSlot) {
        getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemHandler -> {
            ItemStack inputStack = itemHandler.getStackInSlot(inputSlot);
            if (inputStack.getCount() != 1) return;

            FluidUtil.getFluidHandler(inputStack).ifPresent(fluidHandlerItem -> {
                FluidStack itemContents = fluidHandlerItem.drain(1000, IFluidHandler.FluidAction.SIMULATE);

                getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(fluidHandler -> {
                    if (!itemContents.isEmpty()) {
                        // input item contains fluid: drain from input item into tank, move to output if empty
                        FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandler, fluidHandlerItem, itemContents.getAmount(), true);
                        if (transferred.getAmount() == itemContents.getAmount()) {
                            // all transferred; move empty container to output if possible
                            ItemStack emptyContainerStack = fluidHandlerItem.getContainer();
                            ItemStack excess = itemHandler.insertItem(outputSlot, emptyContainerStack, true);
                            if (excess.isEmpty()) {
                                itemHandler.extractItem(inputSlot, 1, false);
                                itemHandler.insertItem(outputSlot, emptyContainerStack, false);
                            }
                        }
                    } else if (itemHandler.getStackInSlot(outputSlot).isEmpty()) {
                        // input item is empty: drain from tank to item, move to output
                        FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandlerItem, fluidHandler, Integer.MAX_VALUE, true);
                        if (!transferred.isEmpty()) {
                            itemHandler.extractItem(inputSlot, 1, false);
                            ItemStack filledContainerStack = fluidHandlerItem.getContainer();
                            itemHandler.insertItem(outputSlot, filledContainerStack, false);
                        }
                    }
                });
            });
        });
    }

    @Override
    public Map<EnumUpgrade, Integer> getApplicableUpgrades() {
        return ApplicableUpgradesDB.getInstance().getApplicableUpgrades(this);
    }

    @Override
    public void addLuaMethods(LuaMethodRegistry registry) {
        if (this instanceof IHeatExchangingTE) {
            registry.registerLuaMethod(new LuaMethod("getTemperature") {
                @Override
                public Object[] call(Object[] args) {
                    requireArgs(args, 0, 1, "face? (down/up/north/south/west/east)");
                    Direction dir = args.length == 0 ? null : getDirForString((String) args[0]);
                    IHeatExchangerLogic logic = ((IHeatExchangingTE) TileEntityBase.this).getHeatExchanger(dir);
                    double temp = logic == null ? HeatExchangerLogicAmbient.getAmbientTemperature(level, worldPosition) : logic.getTemperature();
                    return new Object[] { temp };
                }
            });
        }
    }

    @Override
    public LuaMethodRegistry getLuaMethodRegistry() {
        return luaMethodRegistry;
    }

    @Override
    public String getPeripheralType() {
        return getType().getRegistryName().toString();
    }

    public abstract IItemHandler getPrimaryInventory();

    @Override
    public UpgradeHandler getUpgradeHandler() {
        return upgradeHandler;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return getInventoryCap().cast();
        } else if (cap == PNCCapabilities.HEAT_EXCHANGER_CAPABILITY) {
            return getHeatCap(side).cast();
        }
        return super.getCapability(cap, side);
    }

    @Nonnull
    protected LazyOptional<IItemHandler> getInventoryCap() {
        // for internal use only!
        return LazyOptional.empty();
    }

    @Nonnull
    public LazyOptional<IHeatExchangerLogic> getHeatCap(Direction side) {
        // for internal use only!
        return LazyOptional.empty();
    }

    /**
     * Collect all items which should be dropped when this TE is broken.  Override and extend this in subclassing
     * TE's which have extra inventories to be dropped.
     *
     * @param drops list in which to collect dropped items
     */
    public void getContentsToDrop(NonNullList<ItemStack> drops) {
        PneumaticCraftUtils.collectNonEmptyItems(getPrimaryInventory(), drops);

        if (!shouldPreserveStateOnBreak()) {
            UpgradeHandler uh = getUpgradeHandler();
            for (int i = 0; i < uh.getSlots(); i++) {
                if (!uh.getStackInSlot(i).isEmpty()) {
                    drops.add(uh.getStackInSlot(i));
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
     * For machines which use recipes, get the synced recipe ID client-side for informational purposes
     * @return the recipe id (in string form), or the empty string if no current recipe or not applicable
     */
    public String getCurrentRecipeIdSynced() {
        return "";
    }

    /**
     * Called when a machine's upgrades have changed in any way.  This is also called from readNBT() when saved upgrades
     * are deserialized, so it is not guaranteed that the world field is non-null - beware.  If you override this,
     * remember to call the super method!
     */
    @Override
    public void onUpgradesChanged() {
        actualSpeedMult = (float) Math.pow(ConfigHelper.common().machines.speedUpgradeSpeedMultiplier.get(), Math.min(10, getUpgrades(EnumUpgrade.SPEED)));
        actualUsageMult = (float) Math.pow(ConfigHelper.common().machines.speedUpgradeUsageMultiplier.get(), Math.min(10, getUpgrades(EnumUpgrade.SPEED)));
    }

    public UpgradeCache getUpgradeCache() {
        return upgradeCache;
    }

    /**
     * Get any extra data to be serialized onto a dropped item stack. The supplied tag is the "BlockEntityTag" subtag of
     * the item's NBT data, so will be automatically deserialized into the TE (i.e. available to
     * {@link TileEntity#load(BlockState, CompoundNBT)} method) when the itemblock  is next placed.
     *
     * @param blockEntityTag the existing "BlockEntityTag" subtag to add data to
     * @param preserveState true when dropped with a wrench, false when broken with a pickaxe etc.
     */
    public void serializeExtraItemData(CompoundNBT blockEntityTag, boolean preserveState) {
    }

    /**
     * Get the number of players who have a GUI open for this tile entity.  Only use this server-side.
     *
     * @return the player count
     */
    public int countPlayersUsing() {
        return (int) level.players().stream()
                .filter(player -> player.containerMenu instanceof ContainerPneumaticBase)
                .filter(player -> ((ContainerPneumaticBase<?>) player.containerMenu).te == this)
                .count();
    }

    @Override
    public void requestModelDataUpdate() {
        // it is possible for the TE's client world to be a fake one, e.g. Create schematicannon previews
        // https://github.com/TeamPneumatic/pnc-repressurized/issues/812
        if (level != null && level.isClientSide && level == ClientUtils.getClientWorld()) {
            ModelDataManager.requestModelDataRefresh(this);
        }
    }

    public class UpgradeHandler extends BaseItemStackHandler {
        UpgradeHandler(int upgradeSize) {
            super(TileEntityBase.this, upgradeSize);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || isApplicable(itemStack) && isUnique(slot, itemStack);
        }

        @Override
        protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
            EnumUpgrade upgrade = EnumUpgrade.from(stack);
            if (upgrade == null) return 0;
            return ApplicableUpgradesDB.getInstance().getMaxUpgrades(te, upgrade);
        }

        private boolean isUnique(int slot, ItemStack stack) {
            for (int i = 0; i < getSlots(); i++) {
                if (i != slot && EnumUpgrade.from(stack) == EnumUpgrade.from(getStackInSlot(i))) return false;
            }
            return true;
        }

        private boolean isApplicable(ItemStack stack) {
            EnumUpgrade upgrade = EnumUpgrade.from(stack);
            return ApplicableUpgradesDB.getInstance().getMaxUpgrades(TileEntityBase.this, upgrade) > 0;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            upgradeCache.invalidate();
        }
    }
}
