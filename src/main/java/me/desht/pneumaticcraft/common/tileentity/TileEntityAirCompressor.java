package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.common.block.BlockAirCompressor;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class TileEntityAirCompressor extends TileEntityPneumaticBase implements IRedstoneControlled {

    private static final int INVENTORY_SIZE = 1;
    private AirCompressorHandler inventory = new AirCompressorHandler();

    private static final int FUEL_SLOT = 0;

    @GuiSynced
    public int burnTime;
    @GuiSynced
    private int maxBurnTime; // in here the total burn time of the current burning item is stored.
    @GuiSynced
    public int redstoneMode = 0; // determines how the compressor responds to redstone.

    @DescSynced
    private boolean isActive;

    @GuiSynced
    public int curFuelUsage;
    private class AirCompressorHandler extends FilteredItemStackHandler {

        AirCompressorHandler() {
            super(TileEntityAirCompressor.this, INVENTORY_SIZE);
        }
        @Override
        public boolean test(Integer slot, ItemStack itemStack) {
            return slot == FUEL_SLOT &&
                    (itemStack.isEmpty() || TileEntityFurnace.isItemFuel(itemStack) && FluidUtil.getFluidContained(itemStack) == null);
        }

    }
    public TileEntityAirCompressor() {
        this(PneumaticValues.DANGER_PRESSURE_AIR_COMPRESSOR, PneumaticValues.MAX_PRESSURE_AIR_COMPRESSOR, PneumaticValues.VOLUME_AIR_COMPRESSOR);
    }

    public TileEntityAirCompressor(float dangerPressure, float criticalPressure, int volume) {
        super(dangerPressure, criticalPressure, volume, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            if (redstoneAllows() && burnTime < curFuelUsage && TileEntityFurnace.isItemFuel(inventory.getStackInSlot(FUEL_SLOT))) {
                ItemStack fuelStack = inventory.getStackInSlot(FUEL_SLOT);
                burnTime += TileEntityFurnace.getItemBurnTime(fuelStack);
                maxBurnTime = burnTime;
                fuelStack.shrink(1);
            }

            curFuelUsage = (int) (getBaseProduction() * getSpeedUsageMultiplierFromUpgrades() / 10);
            if (burnTime >= curFuelUsage) {
                burnTime -= curFuelUsage;
                if (!getWorld().isRemote) {
                    addAir((int) (getBaseProduction() * getSpeedMultiplierFromUpgrades() * getEfficiency() / 100D));
                    onFuelBurn(curFuelUsage);
                }
            }
            boolean wasActive = isActive;
            isActive = burnTime > curFuelUsage;
            if (wasActive != isActive) {
                getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).withProperty(BlockAirCompressor.ON, isActive));
            }
        } else if (isActive) spawnBurningParticle();

        super.update();

        if (!getWorld().isRemote) {
            List<Pair<EnumFacing, IAirHandler>> teList = getAirHandler(null).getConnectedPneumatics();
            if (teList.size() == 0) getAirHandler(null).airLeak(getRotation());
        }
    }

    protected void onFuelBurn(int burnedFuel) {
    }

    public int getEfficiency() {
        return 100;
    }

    public int getBaseProduction() {
        return PneumaticValues.PRODUCTION_COMPRESSOR;
    }

    private void spawnBurningParticle() {
        if (getWorld().rand.nextInt(3) != 0) return;
        float px = getPos().getX() + 0.5F;
        float py = getPos().getY() + getWorld().rand.nextFloat() * 6.0F / 16.0F;
        float pz = getPos().getZ() + 0.5F;
        float f3 = 0.5F;
        float f4 = getWorld().rand.nextFloat() * 0.4F - 0.2F;
        switch (getRotation()) {
            case EAST:
                getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px - f3, py, pz + f4, 0.0D, 0.0D, 0.0D);
                getWorld().spawnParticle(EnumParticleTypes.FLAME, px - f3, py, pz + f4, 0.0D, 0.0D, 0.0D);
                break;
            case WEST:
                getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px + f3, py, pz + f4, 0.0D, 0.0D, 0.0D);
                getWorld().spawnParticle(EnumParticleTypes.FLAME, px + f3, py, pz + f4, 0.0D, 0.0D, 0.0D);
                break;
            case SOUTH:
                getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px + f4, py, pz - f3, 0.0D, 0.0D, 0.0D);
                getWorld().spawnParticle(EnumParticleTypes.FLAME, px + f4, py, pz - f3, 0.0D, 0.0D, 0.0D);
                break;
            case NORTH:
                getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px + f4, py, pz + f3, 0.0D, 0.0D, 0.0D);
                getWorld().spawnParticle(EnumParticleTypes.FLAME, px + f4, py, pz + f3, 0.0D, 0.0D, 0.0D);
                break;
        }
    }

    @Override
    public boolean isConnectedTo(EnumFacing side) {
        return getRotation() == side;
    }

    public int getBurnTimeRemainingScaled(int parts) {
        if (maxBurnTime == 0 || burnTime < curFuelUsage) return 0;
        return parts * burnTime / maxBurnTime;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
    }

    @Override
    public String getName() {
        return Blockss.AIR_COMPRESSOR.getTranslationKey();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        burnTime = nbtTagCompound.getInteger("burnTime");
        maxBurnTime = nbtTagCompound.getInteger("maxBurn");
        redstoneMode = nbtTagCompound.getInteger("redstoneMode");
        inventory = new AirCompressorHandler();
        inventory.deserializeNBT(nbtTagCompound.getCompoundTag("Items"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setInteger("burnTime", burnTime);
        nbtTagCompound.setInteger("maxBurn", maxBurnTime);
        nbtTagCompound.setInteger("redstoneMode", redstoneMode);
        nbtTagCompound.setTag("Items", inventory.serializeNBT());
        return nbtTagCompound;
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }
}
