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

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.block.BlockChargingStation;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStation;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationUpgradeManager;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.inventory.handler.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.item.IChargeableContainerProvider;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.EmittingRedstoneMode;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.util.GlobalTileEntityCacheManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TileEntityChargingStation extends TileEntityPneumaticBase implements IRedstoneControl<TileEntityChargingStation>, ICamouflageableTE, INamedContainerProvider {
    private static final List<RedstoneMode<TileEntityChargingStation>> REDSTONE_MODES = ImmutableList.of(
            new EmittingRedstoneMode<>("standard.never", new ItemStack(Items.GUNPOWDER), te -> false),
            new EmittingRedstoneMode<>("chargingStation.idle", Textures.GUI_CHARGE_IDLE, TileEntityChargingStation::isIdle),
            new EmittingRedstoneMode<>("chargingStation.charging", Textures.GUI_CHARGING, te -> te.charging),
            new EmittingRedstoneMode<>("chargingStation.discharging", Textures.GUI_DISCHARGING, te -> te.discharging)
    );
    private static final int INVENTORY_SIZE = 1;
    public static final int CHARGE_INVENTORY_INDEX = 0;
    private static final int MAX_REDSTONE_UPDATE_FREQ = 10;  // in ticks; used to reduce lag from rapid updates

    @DescSynced
    private ItemStack chargingStackSynced = ItemStack.EMPTY;  // the item being charged, minus any nbt - for client display purposes

    private ChargingStationHandler itemHandler = new ChargingStationHandler();  // holds the item being charged
    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> itemHandler);

    private ChargeableItemHandler chargeableInventory;  // inventory of the item being charged

    @GuiSynced
    public float chargingItemPressure;
    @GuiSynced
    public boolean charging;
    @GuiSynced
    public boolean discharging;
    private boolean oldRedstoneStatus;
    private BlockState camoState;
    private long lastRedstoneUpdate;
    private int pendingRedstoneStatus = -1;
    @GuiSynced
    private final RedstoneController<TileEntityChargingStation> rsController = new RedstoneController<>(this, REDSTONE_MODES);
    @GuiSynced
    public boolean upgradeOnly = false;

    public TileEntityChargingStation() {
        super(ModTileEntities.CHARGING_STATION.get(), PneumaticValues.DANGER_PRESSURE_CHARGING_STATION, PneumaticValues.MAX_PRESSURE_CHARGING_STATION, PneumaticValues.VOLUME_CHARGING_STATION, 4);
    }

    @Nonnull
    public ItemStack getChargingStack() {
        return itemHandler.getStackInSlot(CHARGE_INVENTORY_INDEX);
    }

    @Nonnull
    public ItemStack getChargingStackSynced() { return chargingStackSynced; }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide) {
            discharging = false;
            charging = false;

            chargingStackSynced = itemHandler.getStackInSlot(CHARGE_INVENTORY_INDEX);

            int airToTransfer = (int) (PneumaticValues.CHARGING_STATION_CHARGE_RATE * getSpeedMultiplierFromUpgrades());

            for (IAirHandler itemAirHandler : findChargeable()) {
                float itemPressure = itemAirHandler.getPressure();
                float itemVolume = itemAirHandler.getVolume();
                float chargerPressure = getPressure();
                float delta = Math.abs(chargerPressure - itemPressure) / 2.0F;
                int airInItem = itemAirHandler.getAir();

                if (PneumaticCraftUtils.epsilonEquals(chargerPressure, 0f) && delta < 0.1f) {
                    // small kludge to get last tiny bit of air out of an item (arithmetic rounding)
                    itemAirHandler.addAir(-airInItem);
                } else if (itemPressure > chargerPressure + 0.01F && itemPressure > 0F) {
                    // move air from item to charger
                    int airToMove = Math.min(Math.min(airToTransfer, airInItem), (int) (delta * airHandler.getVolume()));
                    itemAirHandler.addAir(-airToMove);
                    this.addAir(airToMove);
                    discharging = true;
                } else if (itemPressure < chargerPressure - 0.01F && itemPressure < itemAirHandler.maxPressure()) {
                    // move air from charger to item
                    int maxAirInItem = (int) (itemAirHandler.maxPressure() * itemVolume);
                    float boost = chargerPressure < 15f ? 1f : 1f + (chargerPressure - 15f) / 5f;
                    int airToMove = Math.min(Math.min((int)(airToTransfer * boost), airHandler.getAir()), maxAirInItem - airInItem);
                    airToMove = Math.min((int) (delta * itemVolume), airToMove);
                    itemAirHandler.addAir(airToMove);
                    this.addAir(-airToMove);
                    charging = true;
                }
            }

            boolean shouldEmit = rsController.shouldEmit();
            if (oldRedstoneStatus != shouldEmit) {
                if (level.getGameTime() - lastRedstoneUpdate > MAX_REDSTONE_UPDATE_FREQ) {
                    updateRedstoneOutput();
                } else {
                    pendingRedstoneStatus = shouldEmit ? 1: 0;
                }
            } else if (pendingRedstoneStatus != -1 && level.getGameTime() - lastRedstoneUpdate > MAX_REDSTONE_UPDATE_FREQ) {
                updateRedstoneOutput();
            }

            airHandler.setSideLeaking(!upgradeOnly && hasNoConnectedAirHandlers() ? getRotation() : null);
        }
    }

    private void updateRedstoneOutput() {
        oldRedstoneStatus = rsController.shouldEmit();
        updateNeighbours();
        pendingRedstoneStatus = -1;
        lastRedstoneUpdate = level.getGameTime();
    }

    private List<IAirHandler> findChargeable() {
        if (upgradeOnly) return Collections.emptyList();

        List<IAirHandler> res = new ArrayList<>();

        if (getChargingStack().getCount() == 1) {
            getChargingStack().getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(h -> {
                res.add(h);
                chargingItemPressure = h.getPressure();
            });
        }

        if (getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            List<Entity> entitiesOnPad = getLevel().getEntitiesOfClass(Entity.class, new AxisAlignedBB(getBlockPos().above()));
            for (Entity entity : entitiesOnPad) {
                if (entity instanceof ItemEntity) {
                    ((ItemEntity) entity).getItem().getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(res::add);
                } else if (entity instanceof PlayerEntity) {
                    PlayerInventory inv = ((PlayerEntity) entity).inventory;
                    for (int i = 0; i < inv.getContainerSize(); i++) {
                        ItemStack stack = inv.getItem(i);
                        if (stack.getCount() == 1) {
                            stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(res::add);
                        }
                    }
                } else {
                    entity.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY).ifPresent(res::add);
                }
            }
        }
        return res;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return getRotation() == side;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        if (rsController.parseRedstoneMode(tag))
            return;

        switch (tag) {
            case "open_upgrades":
                if (getChargingStack().getItem() instanceof IChargeableContainerProvider) {
                    INamedContainerProvider provider = ((IChargeableContainerProvider) getChargingStack().getItem()).getContainerProvider(this);
                    NetworkHooks.openGui(player, provider, getBlockPos());
                }
                break;
            case "close_upgrades":
                NetworkHooks.openGui(player, this, getBlockPos());
                break;
            case "toggle_upgrade_only":
                upgradeOnly = !upgradeOnly;
                break;
        }
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return itemHandler;
    }

    private boolean isIdle() {
        return !charging && !discharging &&
                !getChargingStack().isEmpty() &&
                getChargingStack().getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).isPresent();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), getBlockPos().getX() + 1, getBlockPos().getY() + 1, getBlockPos().getZ() + 1);
    }

    public ChargeableItemHandler getChargeableInventory() {
        return getLevel().isClientSide ? new ChargeableItemHandler(this) : chargeableInventory;
    }

    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return inventoryCap;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        itemHandler = new ChargingStationHandler();
        itemHandler.deserializeNBT(tag.getCompound("Items"));

        ItemStack chargeSlot = getChargingStack();
        if (chargeSlot.getItem() instanceof IChargeableContainerProvider) {
            chargeableInventory = new ChargeableItemHandler(this);
        }
        upgradeOnly = tag.getBoolean("UpgradeOnly");
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        if (chargeableInventory != null) {
            chargeableInventory.writeToNBT();
        }
        tag.put("Items", itemHandler.serializeNBT());
        if (upgradeOnly) tag.putBoolean("UpgradeOnly", true);
        return tag;
    }

    @Override
    public void serializeExtraItemData(CompoundNBT blockEntityTag, boolean preserveState) {
        if (upgradeOnly) blockEntityTag.putBoolean("UpgradeOnly", true);
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);

        ICamouflageableTE.writeCamo(tag, camoState);
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);

        camoState = ICamouflageableTE.readCamo(tag);
    }

    @Override
    public void onUpgradesChanged() {
        super.onUpgradesChanged();

        if (level != null && !level.isClientSide) {
            BlockState state = level.getBlockState(worldPosition);
            level.setBlockAndUpdate(worldPosition, state.setValue(BlockChargingStation.CHARGE_PAD, getUpgrades(EnumUpgrade.DISPENSER) > 0));
        }
    }

    @Override
    public RedstoneController<TileEntityChargingStation> getRedstoneController() {
        return rsController;
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public BlockState getCamouflage() {
        return camoState;
    }

    @Override
    public void setCamouflage(BlockState state) {
        camoState = state;
        ICamouflageableTE.syncToClient(this);
    }

    @Override
    public void setRemoved(){
        super.setRemoved();
        GlobalTileEntityCacheManager.getInstance().chargingStations.remove(this);
    }

    @Override
    public void clearRemoved(){
        super.clearRemoved();
        GlobalTileEntityCacheManager.getInstance().chargingStations.add(this);
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerChargingStation(i, playerInventory, getBlockPos());
    }

    private class ChargingStationHandler extends BaseItemStackHandler {
        ChargingStationHandler() {
            super(TileEntityChargingStation.this, INVENTORY_SIZE);
        }

        @Override
        public int getSlotLimit(int slot){
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return slot == CHARGE_INVENTORY_INDEX
                    && (itemStack.isEmpty() || itemStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).isPresent());
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            TileEntityChargingStation teCS = TileEntityChargingStation.this;

            ItemStack newStack = getStackInSlot(slot);
            if (!ItemStack.isSame(chargingStackSynced, newStack)) {
                chargingStackSynced = new ItemStack(newStack.getItem());
            }

            if (teCS.getLevel().isClientSide || slot != CHARGE_INVENTORY_INDEX) return;

            teCS.chargeableInventory = newStack.getItem() instanceof IChargeableContainerProvider ?
                    new ChargeableItemHandler(teCS) :
                    null;

            // if any other player has a gui open for the previous item, force a reopen of the charging station gui
            for (PlayerEntity player : teCS.getLevel().players()) {
                if (player instanceof ServerPlayerEntity
                        && player.containerMenu instanceof ContainerChargingStationUpgradeManager
                        && ((ContainerChargingStationUpgradeManager) player.containerMenu).te == te) {
                    NetworkHooks.openGui((ServerPlayerEntity) player, TileEntityChargingStation.this, getBlockPos());
                }
            }
        }
    }
}
