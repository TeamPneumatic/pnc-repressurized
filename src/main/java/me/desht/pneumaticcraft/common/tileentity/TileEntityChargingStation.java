package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.common.item.IChargingStationGUIHolderItem;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TileEntityChargingStation extends TileEntityPneumaticBase implements IRedstoneControl {
    @DescSynced
    private ChargingStationHandler inventory;
    private ChargeableItemHandler chargeableInventory;
    private CombinedInvWrapper invWrapper;

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

    public TileEntityChargingStation() {
        super(PneumaticValues.DANGER_PRESSURE_CHARGING_STATION, PneumaticValues.MAX_PRESSURE_CHARGING_STATION, PneumaticValues.VOLUME_CHARGING_STATION, 4);
        inventory = new ChargingStationHandler();
        addApplicableUpgrade(EnumUpgrade.SPEED, EnumUpgrade.DISPENSER);
    }

    @Nonnull
    public ItemStack getCamoStack() {
        if (camoStack.isEmpty() || !(camoStack.getItem() instanceof ItemBlock)) {
            return new ItemStack(Blocks.COBBLESTONE);
        }
        Block block = ((ItemBlock) camoStack.getItem()).getBlock();
        if (PneumaticCraftUtils.isRenderIDCamo(block.getDefaultState().getRenderType())) {
            return camoStack;
        } else {
            return new ItemStack(Blocks.COBBLESTONE);
        }
    }

    public void setCamoStack(@Nonnull ItemStack stack) {
        camoStack = stack;
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
            //creating a new word, 'entities padding'.
            List<Entity> entitiesPadding = getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 2, getPos().getZ() + 1));
            for (Entity entity : entitiesPadding) {
                if (entity instanceof IPressurizable) {
                    chargingItems.add((IPressurizable) entity);
                    chargedStacks.add(null);
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
        for (int i = 0; i < PneumaticValues.CHARGING_STATION_CHARGE_RATE * speedMultiplier; i++) {
            boolean charged = false;
            for (int j = 0; j < chargingItems.size(); j++) {
                IPressurizable chargingItem = chargingItems.get(j);
                ItemStack chargedItem = chargedStacks.get(j);
                if (chargingItem.getPressure(chargedItem) > getPressure() + 0.01F && chargingItem.getPressure(chargedItem) > 0F) {
                    if (!getWorld().isRemote) {
                        chargingItem.addAir(chargedItem, -1);
                        addAir(1);
                    }
                    disCharging = true;
                    renderAirProgress -= ANIMATION_AIR_SPEED;
                    if (renderAirProgress < 0.0F) {
                        renderAirProgress += 1F;
                    }
                    charged = true;
                } else if (chargingItem.getPressure(chargedItem) < getPressure() - 0.01F && chargingItem.getPressure(chargedItem) < chargingItem.maxPressure(chargedItem)) {// if there is pressure, and the item isn't fully charged yet..
                    if (!getWorld().isRemote) {
                        chargingItem.addAir(chargedItem, 1);
                        addAir(-1);
                    }
                    charging = true;
                    renderAirProgress += ANIMATION_AIR_SPEED;
                    if (renderAirProgress > 1.0F) {
                        renderAirProgress -= 1F;
                    }
                    charged = true;
                }
            }
            if (!charged) break;
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

    private boolean shouldEmitRedstone() {
        // if(!getWorld().isRemote) System.out.println("redstone mode: " +
        // redstoneMode + ", charging: " + charging + ",discharging: " +
        // disCharging);

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
        return invWrapper == null ? inventory : invWrapper;
    }

    @Override
    public String getName() {
        return Blockss.CHARGING_STATION.getUnlocalizedName();
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

        if (tag.hasKey("camoStack")) {
            camoStack = new ItemStack(tag.getCompoundTag("camoStack"));
        } else {
            camoStack = ItemStack.EMPTY;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (chargeableInventory != null) {
            chargeableInventory.writeToNBT();
        }
        tag.setInteger("redstoneMode", redstoneMode);
        tag.setTag("Items", inventory.serializeNBT());
        if (!camoStack.isEmpty()) {
            NBTTagCompound subTag = new NBTTagCompound();
            camoStack.writeToNBT(subTag);
            tag.setTag("camoStack", subTag);
        }
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
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    private class ChargingStationHandler extends FilteredItemStackHandler {
        private final TileEntityChargingStation te;

        ChargingStationHandler() {
            super(INVENTORY_SIZE);
            this.te = TileEntityChargingStation.this;
        }

        @Override
        public boolean test(Integer slot, ItemStack itemStack) {
            return slot == CHARGE_INVENTORY_INDEX && (itemStack.isEmpty() || itemStack.getItem() instanceof IPressurizable);
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (te.getWorld().isRemote || slot != CHARGE_INVENTORY_INDEX) return;

            if (te.chargeableInventory != null) {
                te.chargeableInventory.saveInventory();
                te.setChargeableInventory(null);
            }

            ItemStack stack = getStackInSlot(slot);
            if (stack.getItem() instanceof IChargingStationGUIHolderItem) {
                te.chargeableInventory = new ChargeableItemHandler(te);
                te.invWrapper = new CombinedInvWrapper(te.inventory, te.chargeableInventory);
            }
            List<EntityPlayer> players = te.getWorld().playerEntities;
            for(EntityPlayer player : players) {
                if (player.openContainer instanceof ContainerChargingStationItemInventory && ((ContainerChargingStationItemInventory) player.openContainer).te == te) {
                    if (stack.getItem() instanceof IChargingStationGUIHolderItem) {
                        // player.openGui(PneumaticCraft.instance, CommonProxy.GUI_ID_PNEUMATIC_ARMOR, getWorld(), getPos().getX(), getPos().getY(), getPos().getZ());
                    } else {
                        player.openGui(PneumaticCraftRepressurized.instance, EnumGuiId.CHARGING_STATION.ordinal(), te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
                    }
                }
            }
        }
    }
}
