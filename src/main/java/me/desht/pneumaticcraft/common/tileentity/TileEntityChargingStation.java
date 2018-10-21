package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.common.item.IChargingStationGUIHolderItem;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.GlobalTileEntityCacheManager;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TileEntityChargingStation extends TileEntityPneumaticBase implements IRedstoneControl, ICamouflageableTE {
    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "gui.tab.redstoneBehaviour.button.never",
            "gui.tab.redstoneBehaviour.chargingStation.button.doneDischarging",
            "gui.tab.redstoneBehaviour.chargingStation.button.charging",
            "gui.tab.redstoneBehaviour.chargingStation.button.discharging"
    );

    @DescSynced
    private ChargingStationHandler inventory;
    private ChargeableItemHandler chargeableInventory;
    private CombinedInvWrapper invWrapper;
    private boolean droppingItems = false; // https://github.com/TeamPneumatic/pnc-repressurized/issues/80

    private static final int INVENTORY_SIZE = 1;

    public static final int CHARGE_INVENTORY_INDEX = 0;
    private static final float ANIMATION_AIR_SPEED = 0.001F;

    @GuiSynced
    public boolean charging;
    @GuiSynced
    public boolean disCharging;
    @GuiSynced
    public int redstoneMode;
    private boolean oldRedstoneStatus;
    public float renderAirProgress;
    @DescSynced
    private ItemStack camoStack = ItemStack.EMPTY;
    private IBlockState camoState;

    public TileEntityChargingStation() {
        super(PneumaticValues.DANGER_PRESSURE_CHARGING_STATION, PneumaticValues.MAX_PRESSURE_CHARGING_STATION, PneumaticValues.VOLUME_CHARGING_STATION, 4);
        inventory = new ChargingStationHandler();
        addApplicableUpgrade(EnumUpgrade.SPEED, EnumUpgrade.DISPENSER);
    }

    @Override
    public void onDescUpdate() {
        camoState = ICamouflageableTE.getStateForStack(camoStack);
        getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
    }

    @Nonnull
    public ItemStack getChargingItem() {
        return inventory.getStackInSlot(CHARGE_INVENTORY_INDEX);
    }

    @Override
    public void update() {
        disCharging = false;
        charging = false;
        List<IPressurizable> chargingItems = new ArrayList<>();
        List<ItemStack> chargedStacks = new ArrayList<>();
        if (getChargingItem().getItem() instanceof IPressurizable) {
            chargingItems.add((IPressurizable) getChargingItem().getItem());
            chargedStacks.add(getChargingItem());
        }
        if (this.getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            // creating a new word, 'entities padding'.
            List<Entity> entitiesPadding = getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 2, getPos().getZ() + 1));
            for (Entity entity : entitiesPadding) {
                if (entity instanceof IPressurizable) {
                    chargingItems.add((IPressurizable) entity);
                    chargedStacks.add(ItemStack.EMPTY);
                } else if (entity instanceof EntityItem) {
                    ItemStack entityStack = ((EntityItem) entity).getItem();
                    if (entityStack.getItem() instanceof IPressurizable) {
                        chargingItems.add((IPressurizable) entityStack.getItem());
                        chargedStacks.add(entityStack);
                    }
                } else if (entity instanceof EntityPlayer) {
                    InventoryPlayer inv = ((EntityPlayer) entity).inventory;
                    for (int i = 0; i < inv.getSizeInventory(); i++) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if (stack.getItem() instanceof IPressurizable) {
                            chargingItems.add((IPressurizable) stack.getItem());
                            chargedStacks.add(stack);
                        }
                    }
                }
            }
        }

        int speedMultiplier = (int) getSpeedMultiplierFromUpgrades();
        int airToTransfer = PneumaticValues.CHARGING_STATION_CHARGE_RATE * speedMultiplier;
        int airInCharger = (int) (getPressure() * airHandler.getVolume());

        for (int i = 0; i < chargingItems.size() && airInCharger > 0; i++) {
            IPressurizable chargingItem = chargingItems.get(i);
            ItemStack chargingStack = chargedStacks.get(i);
            float itemPressure = chargingItem.getPressure(chargingStack);
            float itemVolume = chargingItem.getVolume(chargingStack);
            float delta = Math.abs(getPressure() - itemPressure) / 2.0F;

            int airInItem = (int) (itemPressure * itemVolume);

            if (itemPressure > getPressure() + 0.01F && itemPressure > 0F) {
                // move air from item to charger
                int airToMove = Math.min(Math.min(airToTransfer, airInItem), (int) (delta * airHandler.getVolume()));
                if (!getWorld().isRemote) {
                    chargingItem.addAir(chargingStack, -airToMove);
                    addAir(airToMove);
                    airInCharger += airToMove;
                }
                disCharging = true;
                renderAirProgress -= ANIMATION_AIR_SPEED;
                if (renderAirProgress < 0.0F) {
                    renderAirProgress += 1F;
                }
            } else if (itemPressure < getPressure() - 0.01F && itemPressure < chargingItem.maxPressure(chargingStack)) {
                // move air from charger to item
                int maxAirInItem = (int) (chargingItem.maxPressure(chargingStack) * itemVolume);
                int airToMove = Math.min(Math.min(airToTransfer, airInCharger), maxAirInItem - airInItem);
                airToMove = Math.min((int) (delta * itemVolume), airToMove);
                if (!getWorld().isRemote) {
                    chargingItem.addAir(chargingStack, airToMove);
                    addAir(-airToMove);
                    airInCharger -= airToMove;
                }
                charging = true;
                renderAirProgress += ANIMATION_AIR_SPEED;
                if (renderAirProgress > 1.0F) {
                    renderAirProgress -= 1F;
                }
            }
        }

        if (!getWorld().isRemote && oldRedstoneStatus != shouldEmitRedstone()) {
            oldRedstoneStatus = shouldEmitRedstone();
            updateNeighbours();
        }

        super.update();

        if (!getWorld().isRemote) {
            List<Pair<EnumFacing, IAirHandler>> teList = getAirHandler(null).getConnectedPneumatics();
            if (teList.size() == 0) getAirHandler(null).airLeak(getRotation());
        }
    }

    @Override
    public boolean isConnectedTo(EnumFacing side) {
        return getRotation() == side;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 3) redstoneMode = 0;
            updateNeighbours();
        } else if ((buttonID == 1 || buttonID == 2) && getChargingItem().getItem() instanceof IChargingStationGUIHolderItem) {
            player.openGui(PneumaticCraftRepressurized.instance,
                    buttonID == 1 ?
                            ((IChargingStationGUIHolderItem) getChargingItem().getItem()).getGuiID().ordinal() :
                            EnumGuiId.CHARGING_STATION.ordinal(),
                    getWorld(), getPos().getX(), getPos().getY(), getPos().getZ());
        }
    }

    public boolean shouldEmitRedstone() {
        switch (redstoneMode) {
            case 0:
                return false;
            case 1:
                return !charging && !disCharging && getChargingItem().getItem() instanceof IPressurizable;
            case 2:
                return charging;
            case 3:
                return disCharging;

        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
    }

    public ChargeableItemHandler getChargeableInventory() {
        return getWorld().isRemote ? new ChargeableItemHandler(this) : chargeableInventory;
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return invWrapper == null || droppingItems ? inventory : invWrapper;
    }

    @Override
    public void getContentsToDrop(NonNullList<ItemStack> drops) {
        droppingItems = true;
        super.getContentsToDrop(drops);
        droppingItems = false;
    }

    @Override
    public String getName() {
        return Blockss.CHARGING_STATION.getTranslationKey();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        redstoneMode = tag.getInteger("redstoneMode");
        inventory = new ChargingStationHandler();
        inventory.deserializeNBT(tag.getCompoundTag("Items"));

        ItemStack chargeSlot = getChargingItem();
        if (chargeSlot.getItem() instanceof IChargingStationGUIHolderItem) {
            setChargeableInventory(new ChargeableItemHandler(this));
        }

        camoStack = ICamouflageableTE.readCamoStackFromNBT(tag);
        camoState = ICamouflageableTE.getStateForStack(camoStack);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (chargeableInventory != null) {
            chargeableInventory.writeToNBT();
        }
        tag.setInteger("redstoneMode", redstoneMode);
        tag.setTag("Items", inventory.serializeNBT());
        ICamouflageableTE.writeCamoStackToNBT(camoStack, tag);
        return tag;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (chargeableInventory != null) {
            chargeableInventory.saveInventory();
        }
    }

    private void setChargeableInventory(ChargeableItemHandler handler) {
        this.chargeableInventory = handler;
        if (chargeableInventory == null) {
            invWrapper = null;
        } else {
            invWrapper = new CombinedInvWrapper(inventory, chargeableInventory);
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
    public IBlockState getCamouflage() {
        return camoState;
    }

    @Override
    public void setCamouflage(IBlockState state) {
        camoState = state;
        camoStack = ICamouflageableTE.getStackForState(state);
        sendDescriptionPacket();
        markDirty();
    }
    
    @Override
    public void invalidate(){
        super.invalidate();
        GlobalTileEntityCacheManager.getInstance().chargingStations.remove(this);
    }
    
    @Override
    public void validate(){
        super.validate();
        GlobalTileEntityCacheManager.getInstance().chargingStations.add(this);
    }

    private class ChargingStationHandler extends FilteredItemStackHandler {
        ChargingStationHandler() {
            super(TileEntityChargingStation.this, INVENTORY_SIZE);
        }
        
        @Override
        public int getSlotLimit(int slot){
            return 1;
        }

        @Override
        public boolean test(Integer slot, ItemStack itemStack) {
            return slot == CHARGE_INVENTORY_INDEX && (itemStack.isEmpty() || itemStack.getItem() instanceof IPressurizable);
        }

        @Override
        protected void onContentsChanged(int slot) {
            TileEntityChargingStation teCS = (TileEntityChargingStation) te;
            if (teCS.getWorld().isRemote || slot != CHARGE_INVENTORY_INDEX) return;

            if (teCS.chargeableInventory != null) {
                teCS.chargeableInventory.saveInventory();
                teCS.setChargeableInventory(null);
            }

            ItemStack stack = getStackInSlot(slot);
            if (stack.getItem() instanceof IChargingStationGUIHolderItem) {
                teCS.chargeableInventory = new ChargeableItemHandler(teCS);
                teCS.invWrapper = new CombinedInvWrapper(teCS.inventory, teCS.chargeableInventory);
            }
            List<EntityPlayer> players = teCS.getWorld().playerEntities;
            for(EntityPlayer player : players) {
                if (player.openContainer instanceof ContainerChargingStationItemInventory && ((ContainerChargingStationItemInventory) player.openContainer).te == te) {
                    if (stack.getItem() instanceof IChargingStationGUIHolderItem) {
                        // player.openGui(PneumaticCraft.instance, CommonProxy.GUI_ID_PNEUMATIC_ARMOR, getWorld(), getPos().getX(), getPos().getY(), getPos().getZ());
                    } else {
                        player.openGui(PneumaticCraftRepressurized.instance, EnumGuiId.CHARGING_STATION.ordinal(), teCS.getWorld(), teCS.getPos().getX(), teCS.getPos().getY(), teCS.getPos().getZ());
                    }
                }
            }
        }
    }
}
