package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class TileEntityUVLightBox extends TileEntityPneumaticBase implements IMinWorkingPressure, IRedstoneControl {
    public static final int INVENTORY_SIZE = 1;
    public static final int PCB_INDEX = 0;

    @DescSynced
    public boolean leftConnected;
    @DescSynced
    public boolean rightConnected;
    @DescSynced
    public boolean areLightsOn;
    @GuiSynced
    public int redstoneMode;
    @DescSynced
    public ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE);
    public int ticksExisted;
    private boolean oldRedstoneStatus;

    public TileEntityUVLightBox() {
        super(PneumaticValues.DANGER_PRESSURE_UV_LIGHTBOX, PneumaticValues.MAX_PRESSURE_UV_LIGHTBOX, PneumaticValues.VOLUME_UV_LIGHTBOX, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        redstoneMode = nbt.getInteger("redstoneMode");
        inventory.deserializeNBT(nbt.getCompoundTag("Items"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("redstoneMode", redstoneMode);
        nbt.setTag("Items", inventory.serializeNBT());
        return nbt;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            ticksExisted++;
            ItemStack stack = inventory.getStackInSlot(PCB_INDEX).copy();
            if (getPressure() >= PneumaticValues.MIN_PRESSURE_UV_LIGHTBOX && stack.getItem() == Itemss.EMPTY_PCB && stack.getItemDamage() > 0) {
                addAir((int) (-PneumaticValues.USAGE_UV_LIGHTBOX * getSpeedUsageMultiplierFromUpgrades()));
                if (ticksExisted % Math.max(1, (int) (TileEntityConstants.LIGHT_BOX_0_100_TIME / (5 * getSpeedMultiplierFromUpgrades()))) == 0) {
                    if (!areLightsOn) {
                        areLightsOn = true;
                        updateNeighbours();
                    }
                    stack.setItemDamage(Math.max(0, stack.getItemDamage() - 1));
                    inventory.setStackInSlot(PCB_INDEX, stack);
                }
            } else if (areLightsOn) {
                areLightsOn = false;
                updateNeighbours();
            }
            if (oldRedstoneStatus != shouldEmitRedstone()) {
                oldRedstoneStatus = !oldRedstoneStatus;
                updateNeighbours();
            }
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public void onNeighborTileUpdate() {
        super.onNeighborTileUpdate();
        updateConnections();
    }

    public int getLightLevel() {
        return areLightsOn ? Math.min(5, getUpgrades(EnumUpgrade.SPEED) * 2) + 10 : 0;
    }

    // used in the air dispersion methods.
    @Override
    public boolean isConnectedTo(EnumFacing side) {
        return side != EnumFacing.UP && side != getRotation() && side != getRotation().getOpposite();
    }

    public void updateConnections() {
        leftConnected = false;
        rightConnected = false;

        List<Pair<EnumFacing, IAirHandler>> connections = getAirHandler(null).getConnectedPneumatics();
        for (Pair<EnumFacing, IAirHandler> entry : connections) {
            if (entry.getKey() == getRotation().rotateY()) { //TODO 1.8 test
                leftConnected = true;
            } else if (entry.getKey() == getRotation().rotateYCCW()) {
                rightConnected = true;
            }
        }
    }

    @Override
    public String getName() {
        return Blockss.UV_LIGHT_BOX.getUnlocalizedName();
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 4) redstoneMode = 0;
            updateNeighbours();
        }
    }

    public boolean shouldEmitRedstone() {
        ItemStack stack = inventory.getStackInSlot(PCB_INDEX);
        if (redstoneMode == 0 || stack.getItem() != Itemss.EMPTY_PCB) return false;
        switch (redstoneMode) {
            case 1:
                return stack.getItemDamage() < 30;
            case 2:
                return stack.getItemDamage() < 20;
            case 3:
                return stack.getItemDamage() < 10;
            case 4:
                return stack.getItemDamage() == 0;
        }
        return false;
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_UV_LIGHTBOX;
    }
}
