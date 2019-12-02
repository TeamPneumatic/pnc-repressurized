package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.block.BlockChargingStation;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStation;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.inventory.handler.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.item.IChargeableContainerProvider;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.GlobalTileEntityCacheManager;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityChargingStation extends TileEntityPneumaticBase implements IRedstoneControl, ICamouflageableTE, INamedContainerProvider {
    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "gui.tab.redstoneBehaviour.button.never",
            "gui.tab.redstoneBehaviour.chargingStation.button.doneDischarging",
            "gui.tab.redstoneBehaviour.chargingStation.button.charging",
            "gui.tab.redstoneBehaviour.chargingStation.button.discharging"
    );
    private static final int INVENTORY_SIZE = 1;
    public static final int CHARGE_INVENTORY_INDEX = 0;

    @DescSynced
    public ItemStack chargingStackSynced = ItemStack.EMPTY;  // the item being charged, minus any meta/nbt - for client display purposes

    private ChargingStationHandler itemHandler;  // holds the item being charged
    private final LazyOptional<IItemHandlerModifiable> inventoryCap = LazyOptional.of(() -> itemHandler);

    private ChargeableItemHandler chargeableInventory;  // inventory of the item being charged

    @GuiSynced
    public float chargingItemPressure;
    @GuiSynced
    public boolean charging;
    @GuiSynced
    public boolean discharging;
    @GuiSynced
    public int redstoneMode;
    private boolean oldRedstoneStatus;
    @DescSynced
    private ItemStack camoStack = ItemStack.EMPTY;
    private BlockState camoState;

    public TileEntityChargingStation() {
        super(ModTileEntityTypes.CHARGING_STATION, PneumaticValues.DANGER_PRESSURE_CHARGING_STATION, PneumaticValues.MAX_PRESSURE_CHARGING_STATION, PneumaticValues.VOLUME_CHARGING_STATION, 4);
        itemHandler = new ChargingStationHandler();
        addApplicableUpgrade(EnumUpgrade.SPEED, EnumUpgrade.DISPENSER);
    }

    @Override
    public void onDescUpdate() {
        camoState = ICamouflageableTE.getStateForStack(camoStack);

        super.onDescUpdate();
    }

    @Nonnull
    public ItemStack getChargingStack() {
        return itemHandler.getStackInSlot(CHARGE_INVENTORY_INDEX);
    }

    @Override
    public void tick() {
        super.tick();

        if (!world.isRemote) {
            discharging = false;
            charging = false;

            chargingStackSynced = itemHandler.getStackInSlot(CHARGE_INVENTORY_INDEX);

            int airToTransfer = (int) (PneumaticValues.CHARGING_STATION_CHARGE_RATE * getSpeedMultiplierFromUpgrades());

            List<Pair<IPressurizable, ItemStack>> l = findChargeableItems();
            for (int i = 0; i < l.size() && airHandler.getAir() > 0; i++) {
                IPressurizable p = l.get(i).getLeft();
                ItemStack chargingStack = l.get(i).getRight();

                float itemPressure = p.getPressure(chargingStack);
                float itemVolume = p.getVolume(chargingStack);
                float delta = Math.abs(getPressure() - itemPressure) / 2.0F;
                int airInItem = (int) (itemPressure * itemVolume);

                if (itemPressure > getPressure() + 0.01F && itemPressure > 0F) {
                    // move air from item to charger
                    int airToMove = Math.min(Math.min(airToTransfer, airInItem), (int) (delta * airHandler.getVolume()));
                    p.addAir(chargingStack, -airToMove);
                    this.addAir(airToMove);
                    discharging = true;
                } else if (itemPressure < getPressure() - 0.01F && itemPressure < p.maxPressure(chargingStack)) {
                    // move air from charger to item
                    int maxAirInItem = (int) (p.maxPressure(chargingStack) * itemVolume);
                    int airToMove = Math.min(Math.min(airToTransfer, airHandler.getAir()), maxAirInItem - airInItem);
                    airToMove = Math.min((int) (delta * itemVolume), airToMove);
                    p.addAir(chargingStack, airToMove);
                    this.addAir(-airToMove);
                    charging = true;
                }
            }

            if (oldRedstoneStatus != shouldEmitRedstone()) {
                oldRedstoneStatus = shouldEmitRedstone();
                updateNeighbours();
            }

            List<Pair<Direction, IAirHandler>> teList = getAirHandler(null).getConnectedPneumatics();
            if (teList.size() == 0) {
                getAirHandler(null).airLeak(getRotation());
            }
        }
    }

    @Override
    protected void onFirstServerUpdate() {
        super.onFirstServerUpdate();

//        chargingStackSynced = new ItemStack(getChargingStack().getItem());
    }

    private List<Pair<IPressurizable, ItemStack>> findChargeableItems() {
        List<Pair<IPressurizable, ItemStack>> res = new ArrayList<>();

        IPressurizable p = IPressurizable.of(getChargingStack());
        if (p != null) {
            res.add(Pair.of(p, getChargingStack()));
            chargingItemPressure = p.getPressure(getChargingStack());
        }

        if (getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            // creating a new word, 'entities padding'!
            List<Entity> entitiesPadding = getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(getPos().up()));
            for (Entity entity : entitiesPadding) {
                if (entity instanceof IPressurizable) {
                    res.add(Pair.of((IPressurizable) entity, ItemStack.EMPTY));
                } else if (entity instanceof ItemEntity) {
                    ItemStack entityStack = ((ItemEntity) entity).getItem();
                    if (entityStack.getItem() instanceof IPressurizable) {
                        res.add(Pair.of(IPressurizable.of(entityStack), entityStack));
                    }
                } else if (entity instanceof PlayerEntity) {
                    PlayerInventory inv = ((PlayerEntity) entity).inventory;
                    for (int i = 0; i < inv.getSizeInventory(); i++) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if (stack.getItem() instanceof IPressurizable) {
                            res.add(Pair.of(IPressurizable.of(stack), stack));
                        }
                    }
                }
            }
        }

        return res;
    }

    @Override
    public boolean canConnectTo(Direction side) {
        return getRotation() == side;
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        switch (tag) {
            case IGUIButtonSensitive.REDSTONE_TAG:
                redstoneMode++;
                if (redstoneMode > 3) redstoneMode = 0;
                updateNeighbours();
                break;
            case "open_upgrades":
                INamedContainerProvider provider = ((IChargeableContainerProvider) getChargingStack().getItem()).getContainerProvider(this);
                NetworkHooks.openGui((ServerPlayerEntity) player, provider, getPos());
                break;
            case "close_upgrades":
                NetworkHooks.openGui((ServerPlayerEntity) player, this, getPos());
                break;
        }
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return itemHandler;
    }

    public boolean shouldEmitRedstone() {
        switch (redstoneMode) {
            case 0:
                return false;
            case 1:
                return !charging && !discharging && getChargingStack().getItem() instanceof IPressurizable;
            case 2:
                return charging;
            case 3:
                return discharging;

        }
        return false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
    }

    public ChargeableItemHandler getChargeableInventory() {
        return getWorld().isRemote ? new ChargeableItemHandler(this) : chargeableInventory;
    }

    @Override
    public LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return inventoryCap;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        redstoneMode = tag.getInt("redstoneMode");
        itemHandler = new ChargingStationHandler();
        itemHandler.deserializeNBT(tag.getCompound("Items"));

        ItemStack chargeSlot = getChargingStack();
        if (chargeSlot.getItem() instanceof IChargeableContainerProvider) {
            chargeableInventory = new ChargeableItemHandler(this);
        }

        camoStack = ICamouflageableTE.readCamoStackFromNBT(tag);
        camoState = ICamouflageableTE.getStateForStack(camoStack);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        if (chargeableInventory != null) {
            chargeableInventory.writeToNBT();
        }
        tag.putInt("redstoneMode", redstoneMode);
        tag.put("Items", itemHandler.serializeNBT());
        ICamouflageableTE.writeCamoStackToNBT(camoStack, tag);
        return tag;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (chargeableInventory != null) {
            chargeableInventory.writeToNBT();
        }
    }

    @Override
    protected void onUpgradesChanged() {
        super.onUpgradesChanged();

        if (world != null && !world.isRemote) {
            BlockState state = world.getBlockState(pos);
            world.setBlockState(pos, state.with(BlockChargingStation.CHARGE_PAD, getUpgrades(EnumUpgrade.DISPENSER) > 0));
        }
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    protected List<String> getRedstoneButtonLabels() {
        return REDSTONE_LABELS;
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
        camoStack = ICamouflageableTE.getStackForState(state);
        sendDescriptionPacket();
        markDirty();
    }
    
    @Override
    public void remove(){
        super.remove();
        GlobalTileEntityCacheManager.getInstance().chargingStations.remove(this);
    }
    
    @Override
    public void validate(){
        super.validate();
        GlobalTileEntityCacheManager.getInstance().chargingStations.add(this);
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerChargingStation(i, playerInventory, getPos());
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
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
            return slot == CHARGE_INVENTORY_INDEX && (itemStack.isEmpty() || itemStack.getItem() instanceof IPressurizable);
        }

        @Override
        protected void onContentsChanged(int slot) {
            TileEntityChargingStation teCS = TileEntityChargingStation.this;

            ItemStack newStack = getStackInSlot(slot);
            if (!ItemStack.areItemsEqual(chargingStackSynced, newStack)) {
                chargingStackSynced = new ItemStack(newStack.getItem());
            }

            if (teCS.getWorld().isRemote || slot != CHARGE_INVENTORY_INDEX) return;

            teCS.chargeableInventory = newStack.getItem() instanceof IChargeableContainerProvider ?
                    new ChargeableItemHandler(teCS) :
                    null;

            // if any other player has a gui open for the previous item, force a reopen of the charging station gui
            for (PlayerEntity player : teCS.getWorld().getPlayers()) {
                if (player instanceof ServerPlayerEntity
                        && player.openContainer instanceof ContainerChargingStationItemInventory
                        && ((ContainerChargingStationItemInventory) player.openContainer).te == te) {
                    NetworkHooks.openGui((ServerPlayerEntity) player, TileEntityChargingStation.this, getPos());
                }
            }
        }
    }
}
