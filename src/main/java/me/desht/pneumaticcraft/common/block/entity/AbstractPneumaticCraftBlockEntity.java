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

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.AbstractCamouflageBlock;
import me.desht.pneumaticcraft.common.block.AbstractPneumaticCraftBlock;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.heat.HeatExchangerLogicAmbient;
import me.desht.pneumaticcraft.common.inventory.AbstractPneumaticCraftMenu;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.upgrades.*;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractPneumaticCraftBlockEntity extends BlockEntity
        implements Nameable, IGUIButtonSensitive, IDescSynced, IUpgradeHolder, ILuaMethodProvider {
    private final UpgradeCache upgradeCache = new UpgradeCache(this);
    private final UpgradeHandler upgradeHandler;
    private List<SyncedField<?>> descriptionFields;
    private final CachedTileNeighbours neighbourCache = new CachedTileNeighbours(this);
    private boolean preserveStateOnBreak = false; // set to true if shift-wrenched to keep upgrades in the block
    private float actualSpeedMult = PneumaticValues.DEF_SPEED_UPGRADE_MULTIPLIER;
    private float actualUsageMult = PneumaticValues.DEF_SPEED_UPGRADE_USAGE_MULTIPLIER;
    private final LuaMethodRegistry luaMethodRegistry = new LuaMethodRegistry(this);
    private Component customName = null;
    private boolean forceFullSync;
    private BitSet fieldsToSync;  // tracks which synced fields have changed and need to be synced on the next tick

    public AbstractPneumaticCraftBlockEntity(BlockEntityType type, BlockPos pos, BlockState state) {
        this(type, pos, state, 0);
    }

    public AbstractPneumaticCraftBlockEntity(BlockEntityType type, BlockPos pos, BlockState state, int upgradeSize) {
        super(type, pos, state);

        this.upgradeHandler = new UpgradeHandler(upgradeSize);
    }

    @Nonnull
    public Level nonNullLevel() {
        // use in methods where we know the level is not null, e.g. tickers, renderers...
        return Objects.requireNonNull(super.getLevel());
    }

    private String getBlockTranslationKey() {
        String key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getResourceKey(getType())
                .map(rk -> rk.location().getPath())
                .orElse("unknown");
        return "block.pneumaticcraft." + key;
    }

    @Override
    public Component getName() {
        return customName == null ? Component.translatable(getBlockTranslationKey()) : customName;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return customName;
    }

    public void setCustomName(Component customName) {
        this.customName = customName;
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    // server side, chunk sending
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag compound = super.getUpdateTag(provider);
        return PacketDescription.create(this, true, provider)
                .writeNBT(compound, getLevel().registryAccess());
    }

    // client side, chunk sending
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        PacketDescription.fromNBT(tag, getLevel().registryAccess())
                .processPacket(this, getLevel().registryAccess());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);

        // handle any extra data serialized by PacketDescription
        if (pkt.getTag().contains(Names.MOD_ID, Tag.TAG_COMPOUND)) {
            CompoundTag tag = pkt.getTag().getCompound(Names.MOD_ID).getCompound(PacketDescription.NBT_EXTRA);
            if (!tag.isEmpty()) {
                readFromPacket(tag, lookupProvider);
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return this instanceof AbstractTickingBlockEntity ? null : ClientboundBlockEntityDataPacket.create(this);
    }

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
            for (int i = 0; i < descriptionFields.size(); i++) {
                SyncedField<?> field = descriptionFields.get(i);
                if (field.update()) fieldsToSync.set(i);
            }
        }
        return descriptionFields;
    }

    /**
     * Force a sync of this BE to the client right now.
     */
    public final void sendDescriptionPacket() {
        if (level == null || level.isClientSide) return;

        PacketDescription descPacket = PacketDescription.create(this, forceFullSync, level.registryAccess());
        if (descPacket.hasData()) {
            NetworkHandler.sendToAllTracking(descPacket, this);
        }
        fieldsToSync.clear();
        forceFullSync = false;
    }

    /**
     * A way to safely mark a block for an update from another thread (like the CC Lua thread).
     */
    protected void scheduleDescriptionPacket() {
        forceFullSync = true;
    }

    /*
     * Even though this class doesn't implement ITickableTileEntity, we'll keep the base tick logic here; classes
     * which extend non-tickable subclasses might need it (e.g. PressureChamberInterfaceBlockEntity)
     */
    void defaultServerTick() {
        if (!nonNullLevel().isClientSide) {
            if (this instanceof IHeatExchangingTE he) {
                // tick default heat exchanger; if the BE has other exchangers, they are handled in the subclass
                IHeatExchangerLogic logic = he.getHeatExchanger();
                if (logic != null) logic.tick();
            }

            if (this instanceof IAutoFluidEjecting ejector && getUpgrades(ModUpgrades.DISPENSER.get()) > 0) {
                ejector.autoExportFluid(this);
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
    public void setChanged() {
        // overridden to only update neighbours if this BE actually has a useful comparator output
        if (level != null) {
            if (level.isLoaded(worldPosition)) {
                level.getChunkAt(worldPosition).setUnsaved(true);
            }
            if (this instanceof IComparatorSupport && !this.getBlockState().isAir()) {
                this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
            }
        }
    }

    @Override
    public void onLoad() {
        if (this instanceof IHeatExchangingTE he) {
            he.initializeHullHeatExchangers(level, worldPosition);
        }
    }

    protected void updateNeighbours() {
        if (level != null) {
            level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        }
    }

    public void onBlockRotated() {
        if (this instanceof ISideConfigurable c) {
            for (SideConfigurator<?> sc : c.getSideConfigurators()) {
                sc.setupFacingMatrix();
            }
        }

        invalidateCapabilities();
    }

    protected void forceBlockEntityRerender() {
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 0);
        }
    }

    protected boolean shouldRerenderChunkOnDescUpdate() {
        return this instanceof CamouflageableBlockEntity;
    }

    /**
     * Encoded into the description packet. Also included in saved data written by
     * {@link BlockEntity#saveAdditional(CompoundTag, HolderLookup.Provider)}
     * <p>
     * Prefer to use @DescSynced where possible - use this either for complex fields not handled by @DescSynced,
     * or for non-ticking tile entities.
     *
     * @param tag      NBT tag
     * @param provider lookup provider
     */
    @Override
    public void writeToPacket(CompoundTag tag, HolderLookup.Provider provider) {
        if (this instanceof ISideConfigurable sc) {
            tag.put(NBTKeys.NBT_SIDE_CONFIG, SideConfigurator.writeToNBT(sc, provider));
        }
    }

    /**
     * Encoded into the description packet. Also included in saved data read by
     * {@link AbstractPneumaticCraftBlockEntity#loadAdditional(CompoundTag, HolderLookup.Provider)}.
     * <p>
     * Prefer to use {@code @DescSynced} where possible - use this either for complex fields not handled by {@code @DescSynced},
     * or for non-ticking tile entities.
     *
     * @param tag      NBT tag
     * @param provider
     */
    @Override
    public void readFromPacket(CompoundTag tag, HolderLookup.Provider provider) {
        if (this instanceof ISideConfigurable sc) {
            SideConfigurator.readFromNBT(tag.getCompound(NBTKeys.NBT_SIDE_CONFIG), sc, provider);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        if (customName != null) {
            tag.putString("CustomName", Component.Serializer.toJson(customName, provider));
        }
        if (getUpgradeHandler().getSlots() > 0) {
            tag.put(NBTKeys.NBT_UPGRADE_INVENTORY, getUpgradeHandler().serializeNBT(provider));
        }
        if (this instanceof IHeatExchangingTE he) {
            IHeatExchangerLogic logic = he.getHeatExchanger();
            if (logic != null) tag.put(NBTKeys.NBT_HEAT_EXCHANGER, logic.serializeNBT());
        }
        if (this instanceof IRedstoneControl<?> rc) {
            tag.put(NBTKeys.NBT_REDSTONE_MODE, rc.getRedstoneController().save().toNBT(provider));
        }
        if (this instanceof ISerializableTanks st) {
            tag.put(NBTKeys.NBT_SAVED_TANKS, st.serializeTanks(provider));
        }
        writeToPacket(tag, provider);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        if (tag.contains("CustomName", Tag.TAG_STRING)) {
            customName = Component.Serializer.fromJson(tag.getString("CustomName"), provider);
        }
        if (tag.contains(NBTKeys.NBT_UPGRADE_INVENTORY) && getUpgradeHandler() != null) {
            getUpgradeHandler().deserializeNBT(provider, tag.getCompound(NBTKeys.NBT_UPGRADE_INVENTORY));
        }
        if (this instanceof IHeatExchangingTE he) {
            IHeatExchangerLogic logic = he.getHeatExchanger();
            if (logic != null) logic.deserializeNBT(tag.getCompound(NBTKeys.NBT_HEAT_EXCHANGER));
        }
        if (this instanceof IRedstoneControl<?> rc) {
            rc.getRedstoneController().restore(RedstoneController.Saved.fromNBT(provider, tag.getCompound(NBTKeys.NBT_REDSTONE_MODE)));
        }
        if (this instanceof ISerializableTanks st) {
            st.deserializeTanks(provider, tag.getCompound(NBTKeys.NBT_SAVED_TANKS));
        }
        readFromPacket(tag, provider);
    }

    @Override
    public void onDescUpdate() {
        if (shouldRerenderChunkOnDescUpdate()) {
            forceBlockEntityRerender();
            if (this instanceof CamouflageableBlockEntity) requestModelDataUpdate();
        }
    }

    @Nonnull
    @Override
    public ModelData getModelData() {
        return modelDataBuilder().build();
    }

    protected ModelData.Builder modelDataBuilder() {
        if (this instanceof CamouflageableBlockEntity c) {
            return ModelData.builder()
                    .with(AbstractCamouflageBlock.BLOCK_ACCESS, level)
                    .with(AbstractCamouflageBlock.BLOCK_POS, worldPosition)
                    .with(AbstractCamouflageBlock.CAMO_STATE, c.getCamouflage());
        } else {
            return ModelData.builder();
        }
    }

    /**
     * Called when a key is synced in the container.
     */
    public void onGuiUpdate() {
    }

    public Direction getRotation() {
        BlockState state = getBlockState();
        return state.getBlock() instanceof AbstractPneumaticCraftBlock b ? b.getRotation(state) : Direction.NORTH;
    }

    public int getUpgrades(PNCUpgrade upgrade) {
        return upgradeCache.getUpgrades(upgrade);
    }

    public float getSpeedMultiplierFromUpgrades() {
        return actualSpeedMult;
    }

    public float getSpeedUsageMultiplierFromUpgrades() {
        return actualUsageMult;
    }

    public float getMuffledVolume(float baseVolume) {
        return ModUpgrades.getMuffledVolume(baseVolume, getUpgrades(ModUpgrades.MUFFLER.get()));
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
    }

    public boolean isGuiUseableByPlayer(Player player) {
        return level != null && level.getBlockEntity(getBlockPos()) == this
                && player.distanceToSqr(Vec3.atCenterOf(getBlockPos())) <= 64.0D;
    }

    public BlockEntity getCachedNeighbor(Direction dir) {
        // don't attempt to cache client-side; we don't get neighbour block updates there so can't reliably clear the cache
        if (level == null) return null;

        return level.isClientSide ?
                level.getBlockEntity(worldPosition.relative(dir)) :
                neighbourCache.getCachedNeighbour(dir);
    }

    /**
     * Called when a neighboring block entity changes state (specifically when
     * {@link Level#updateNeighbourForOutputSignal(BlockPos, Block)} is called.
     * @param tilePos the blockpos of the neighboring block entity
     */
    public void onNeighborTileUpdate(BlockPos tilePos) {
    }

    /**
     * Called when a neighboring block changes state (i.e. blockstate update)
     * @param fromPos the blockpos of the block that caused this update
     */
    public void onNeighborBlockUpdate(BlockPos fromPos) {
        if (this instanceof IHeatExchangingTE he) {
            he.initializeHullHeatExchangers(level, worldPosition);
        }
        if (this instanceof IRedstoneControl<?> rc) {
            rc.getRedstoneController().updateRedstonePower();
        }
        neighbourCache.purge();
    }

    /**
     * Take a fluid-containing from the input slot, use it to fill the primary input tank of the block entity,
     * and place the resulting emptied container in the output slot.
     *
     * @param inputSlot input slot
     * @param outputSlot output slot
     */
    protected void processFluidItem(int inputSlot, int outputSlot) {
        IOHelper.getInventoryForBlock(this).ifPresent(itemHandler -> {
            ItemStack inputStack = itemHandler.getStackInSlot(inputSlot);
            ItemStack outputStack = itemHandler.getStackInSlot(outputSlot);

            FluidUtil.getFluidHandler(inputStack).ifPresent(fluidHandlerItem -> {
                FluidStack itemContents = fluidHandlerItem.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);

                IOHelper.getFluidHandlerForBlock(this).ifPresent(fluidHandler -> {
                    if (!itemContents.isEmpty() && (outputStack.isEmpty() || ItemStack.isSameItemSameComponents(inputStack.getItem().getCraftingRemainingItem(inputStack), outputStack))) {
                        // input item contains fluid: drain from input item into tank, move to output if empty
                        // there must be only a single filled container in the input slot!
                        if (inputStack.getCount() != 1) {
                            return;
                        }
                        FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandler, fluidHandlerItem, itemContents.getAmount(), true);
                        if (transferred.getAmount() == itemContents.getAmount()) {
                            // all transferred; move empty container to output if possible
                            ItemStack emptyContainerStack = fluidHandlerItem.getContainer();
                            ItemStack excess = itemHandler.insertItem(outputSlot, emptyContainerStack, true);
                            if (excess.isEmpty()) {
                                itemHandler.extractItem(inputSlot, 1, false);
                                itemHandler.insertItem(outputSlot, emptyContainerStack, false);
                            }
                        } else if (!transferred.isEmpty()) {
                            // partial transfer; update the item in the input slot
                            itemHandler.extractItem(inputSlot, 1, false);
                            itemHandler.insertItem(inputSlot, fluidHandlerItem.getContainer().copy(), false);
                        }
                    } else if (itemHandler.getStackInSlot(outputSlot).isEmpty()) {
                        // input item is empty: drain from tank to item, move to output
                        // we allow multiple empty containers in the input slot
                        ItemStack workStack = inputStack.copyWithCount(1);
                        IOHelper.getFluidHandlerForItem(workStack).ifPresent(workHandler -> {
                            FluidStack transferred = FluidUtil.tryFluidTransfer(workHandler, fluidHandler, Integer.MAX_VALUE, true);
                            if (!transferred.isEmpty()) {
                                itemHandler.extractItem(inputSlot, 1, false);
                                ItemStack filledContainerStack = workHandler.getContainer();
                                itemHandler.insertItem(outputSlot, filledContainerStack, false);
                            }
                        });
                    }
                });
            });
        });
    }

    @Override
    public void addLuaMethods(LuaMethodRegistry registry) {
        if (this instanceof IHeatExchangingTE) {
            registry.registerLuaMethod(new LuaMethod("getTemperature") {
                @Override
                public Object[] call(Object[] args) {
                    requireArgs(args, 0, 1, "face? (down/up/north/south/west/east)");
                    Direction dir = args.length == 0 ? null : getDirForString((String) args[0]);
                    IHeatExchangerLogic logic = ((IHeatExchangingTE) AbstractPneumaticCraftBlockEntity.this).getHeatExchanger(dir);
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
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getResourceKey(getType())
                .map(rk -> rk.location().toString())
                .orElse("unknown");
    }

    public boolean hasItemCapability() {
        return true;
    }

    public boolean hasFluidCapability() {
        return false;
    }

    public boolean hasEnergyCapability() {
        return false;
    }

    public final IItemHandler getItemHandler() {
        return getItemHandler(null);
    }

    public final IFluidHandler getFluidHandler() {
        return getFluidHandler(null);
    }

    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return null;
    }

    public IFluidHandler getFluidHandler(@Nullable Direction dir) {
        return null;
    }

    public IEnergyStorage getEnergyHandler(@Nullable Direction dir) {
        return null;
    }

    @Override
    public UpgradeHandler getUpgradeHandler() {
        return upgradeHandler;
    }

    public BlockCapabilityCache<IItemHandler,Direction> createItemHandlerCache(Direction dir) {
        return getLevel() instanceof ServerLevel serverLevel ?
                BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, serverLevel, getBlockPos().relative(dir), dir.getOpposite(), () -> !isRemoved(), () -> {}) :
                null;
    }

    public BlockCapabilityCache<IFluidHandler,Direction> createFluidHandlerCache(Direction dir) {
        return getLevel() instanceof ServerLevel serverLevel ?
                BlockCapabilityCache.create(Capabilities.FluidHandler.BLOCK, serverLevel, getBlockPos().relative(dir), dir.getOpposite(), () -> !isRemoved(), () -> {}) :
                null;
    }

    /**
     * Collect all items which should be dropped when this BE is broken.  Override and extend this in subclassing
     * BE's which have extra inventories to be dropped.
     *
     * @param drops list in which to collect dropped items
     */
    public void getContentsToDrop(NonNullList<ItemStack> drops) {
        PneumaticCraftUtils.collectNonEmptyItems(getItemHandler(), drops);

        if (!shouldPreserveStateOnBreak()) {
            UpgradeHandler uh = getUpgradeHandler();
            for (int i = 0; i < uh.getSlots(); i++) {
                if (!uh.getStackInSlot(i).isEmpty()) {
                    drops.add(uh.getStackInSlot(i));
                }
            }
        }

        if (this instanceof CamouflageableBlockEntity c) {
            BlockState camoState = c.getCamouflage();
            if (camoState != null) {
                drops.add(CamouflageableBlockEntity.getStackForState(camoState));
            }
        }
    }

    /**
     * Should this block entity preserve its state (currently: upgrades and stored air) when broken?
     * By default, this is true when sneak-wrenched, and false when broken by pick.
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
     * Called when a machine's upgrades have changed in any way.  This is also called from loadAdditional() when saved
     * upgrades are deserialized, so it is not guaranteed that the level is non-null - beware.
     * <p>If you override this, remember to call the super method!
     */
    @Override
    public void onUpgradesChanged() {
        actualSpeedMult = (float) Math.pow(ConfigHelper.common().machines.speedUpgradeSpeedMultiplier.get(), Math.min(10, getUpgrades(ModUpgrades.SPEED.get())));
        actualUsageMult = (float) Math.pow(ConfigHelper.common().machines.speedUpgradeUsageMultiplier.get(), Math.min(10, getUpgrades(ModUpgrades.SPEED.get())));
    }

    public UpgradeCache getUpgradeCache() {
        return upgradeCache;
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);

        if (this instanceof ISerializableTanks st) {
            st.getSerializableTanks().forEach((comp, tank) ->
                    tank.loadFromContent(componentInput.getOrDefault(comp, SimpleFluidContent.EMPTY)));
        }
        if (this instanceof IRedstoneControl<?> rc) {
            RedstoneController.Saved data = componentInput.getOrDefault(ModDataComponents.SAVED_REDSTONE_CONTROLLER, RedstoneController.Saved.DEFAULT);
            rc.getRedstoneController().restore(data);
        }
        if (this instanceof ISideConfigurable sc) {
            Map<String, SideConfigurator.Saved> data = componentInput.getOrDefault(ModDataComponents.SAVED_SIDE_CONFIG, Map.of());
            SideConfigurator.loadSavedData(sc, data);
        }

        componentInput.getOrDefault(ModDataComponents.ITEM_UPGRADES, SavedUpgrades.EMPTY).fillItemHandler(getUpgradeHandler());

        IAirHandlerMachine handler = getLevel().getCapability(PNCCapabilities.AIR_HANDLER_MACHINE, getBlockPos(), getBlockState(), this, null);
        if (handler != null) {
            handler.addPendingAir(componentInput.getOrDefault(ModDataComponents.AIR, 0));
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);

        if (this instanceof ISerializableTanks st) {
            st.getSerializableTanks().forEach((comp, tank) -> {
                if (!tank.isEmpty()) {
                    builder.set(comp, tank.getContent());
                }
            });
        }
        if (this instanceof IRedstoneControl<?> rc) {
            builder.set(ModDataComponents.SAVED_REDSTONE_CONTROLLER, rc.getRedstoneController().save());
        }
        if (this instanceof ISideConfigurable sc) {
            builder.set(ModDataComponents.SAVED_SIDE_CONFIG, SideConfigurator.buildSavedMap(sc));
        }
        if (shouldPreserveStateOnBreak()) {
            IAirHandlerMachine handler = getLevel().getCapability(PNCCapabilities.AIR_HANDLER_MACHINE, getBlockPos(), getBlockState(), this, null);
            if (handler != null) {
                builder.set(ModDataComponents.AIR, handler.getAir());
            }

            SavedUpgrades upgrades = SavedUpgrades.fromItemHandler(getUpgradeHandler());
            if (!upgrades.getUpgradeMap().isEmpty()) {
                builder.set(ModDataComponents.ITEM_UPGRADES, upgrades);
            }
        }
    }

    /**
     * Get the number of players who have a GUI open for this block entity.  Only use this server-side.
     *
     * @return the player count
     */
    public int countPlayersUsing() {
        return (int) nonNullLevel().players().stream()
                .filter(player -> player.containerMenu instanceof AbstractPneumaticCraftMenu)
                .filter(player -> ((AbstractPneumaticCraftMenu<?>) player.containerMenu).blockEntity == this)
                .count();
    }

    @Override
    public void requestModelDataUpdate() {
        // it is possible for the BE's client world to be a fake one, e.g. Create schematicannon previews
        // https://github.com/TeamPneumatic/pnc-repressurized/issues/812
        // TODO: 1.19 client refactor may make this workaround unnecessary - confirm
        if (level != null && level.isClientSide && level == ClientUtils.getClientLevel()) {
            level.getModelDataManager().requestRefresh(this);
        }
    }

    public class UpgradeHandler extends BaseItemStackHandler {
        UpgradeHandler(int upgradeSize) {
            super(AbstractPneumaticCraftBlockEntity.this, upgradeSize);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || isApplicable(itemStack) && isUnique(slot, itemStack);
        }

        @Override
        protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
            PNCUpgrade upgrade = PNCUpgrade.from(stack);
            if (upgrade == null) return 0;
            return ApplicableUpgradesDB.getInstance().getMaxUpgrades(te, upgrade);
        }

        private boolean isUnique(int slot, ItemStack stack) {
            for (int i = 0; i < getSlots(); i++) {
                if (i != slot && PNCUpgrade.from(stack) == PNCUpgrade.from(getStackInSlot(i))) return false;
            }
            return true;
        }

        private boolean isApplicable(ItemStack stack) {
            PNCUpgrade upgrade = PNCUpgrade.from(stack);
            return ApplicableUpgradesDB.getInstance().getMaxUpgrades(AbstractPneumaticCraftBlockEntity.this, upgrade) > 0;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            upgradeCache.invalidateCache();
        }
    }
}
