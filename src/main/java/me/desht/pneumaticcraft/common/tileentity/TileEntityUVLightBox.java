package me.desht.pneumaticcraft.common.tileentity;

import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
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
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo")
public class TileEntityUVLightBox extends TileEntityPneumaticBase implements IMinWorkingPressure, IRedstoneControl, ILightProvider {
    public static final int INVENTORY_SIZE = 1;
    public static final int PCB_SLOT = 0;

    private Object light = null;

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
            ItemStack stack = getLoadedPCB().copy();
            if (getPressure() >= PneumaticValues.MIN_PRESSURE_UV_LIGHTBOX && stack.getItem() == Itemss.EMPTY_PCB && stack.getItemDamage() > 0) {
                addAir((int) (-PneumaticValues.USAGE_UV_LIGHTBOX * getSpeedUsageMultiplierFromUpgrades()));
                if (ticksExisted % Math.max(1, (int) (TileEntityConstants.LIGHT_BOX_0_100_TIME / (5 * getSpeedMultiplierFromUpgrades()))) == 0) {
                    if (!areLightsOn) {
                        setLightsOn(true);
                        updateNeighbours();
                    }
                    stack.setItemDamage(Math.max(0, stack.getItemDamage() - 1));
                    inventory.setStackInSlot(PCB_SLOT, stack);
                }
            } else if (areLightsOn) {
                setLightsOn(false);
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

    private void setLightsOn(boolean lightsOn) {
        boolean check = areLightsOn != lightsOn;
        areLightsOn = lightsOn;
        if (check) {
            getWorld().checkLightFor(EnumSkyBlock.BLOCK, getPos());
            sendDescriptionPacket();
        }
    }

    @Override
    public void onDescUpdate() {
        getWorld().checkLightFor(EnumSkyBlock.BLOCK, getPos());
        getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
    }

    public int getLightLevel() {
        return areLightsOn ? Math.max(15, getUpgrades(EnumUpgrade.SPEED)) + 11 : 0;
    }

    @Override
    public boolean isConnectedTo(EnumFacing side) {
        return side == getRotation().rotateYCCW();
    }

    private void updateConnections() {
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
        ItemStack stack = getLoadedPCB();
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

    public ItemStack getLoadedPCB() {
        return inventory.getStackInSlot(PCB_SLOT);
    }

    @Optional.Method(modid = "albedo")
    @Override
    public Light provideLight() {
        if (light == null && areLightsOn) {
            int radius = Math.max(8, 4 + getUpgrades(EnumUpgrade.SPEED));
            light = new Light(pos().getX(), pos.getY(), pos().getZ(), 0.2f, 0.0f, 1.0f, 1.0f, radius);
        } else if (!areLightsOn) {
            light = null;
        }
        return (Light) light;
    }
}
