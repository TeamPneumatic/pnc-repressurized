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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

public class TileEntityAirCompressor extends TileEntityPneumaticBase implements IRedstoneControlled {

    private static final int INVENTORY_SIZE = 1;
    private AirCompressorHandler inventory = new AirCompressorHandler();

    public static final int FUEL_SLOT = 0;

    @GuiSynced
    public int burnTime;
    @GuiSynced
    public int maxBurnTime; // in here the total burn time of the current
    // burning item is stored.
    @GuiSynced
    public int redstoneMode = 0; // determines how the compressor responds to
    // redstone.
    @DescSynced
    private boolean isActive;
    @GuiSynced
    public int curFuelUsage;

    private static class AirCompressorHandler extends FilteredItemStackHandler {
        AirCompressorHandler() {
            super(INVENTORY_SIZE);
        }

        @Override
        public boolean test(Integer slot, ItemStack itemStack) {
            return slot == FUEL_SLOT && (TileEntityFurnace.isItemFuel(itemStack) || itemStack.isEmpty());
        }
    }

    public TileEntityAirCompressor() {
        this(PneumaticValues.DANGER_PRESSURE_AIR_COMPRESSOR, PneumaticValues.MAX_PRESSURE_AIR_COMPRESSOR, PneumaticValues.VOLUME_AIR_COMPRESSOR);
    }

    public TileEntityAirCompressor(float dangerPressure, float criticalPressure, int volume) {
        super(dangerPressure, criticalPressure, volume, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            ItemStack fuelStack = inventory.getStackInSlot(FUEL_SLOT).copy();
            if (burnTime < curFuelUsage && TileEntityFurnace.isItemFuel(fuelStack) && redstoneAllows()) {
                burnTime += TileEntityFurnace.getItemBurnTime(fuelStack);
                maxBurnTime = burnTime;

                fuelStack.shrink(1);
                inventory.setStackInSlot(FUEL_SLOT, fuelStack);
//                inventory.extractItem(FUEL_SLOT, 1, false);

                if (inventory.getStackInSlot(FUEL_SLOT).isEmpty()) {
                    inventory.setStackInSlot(FUEL_SLOT, fuelStack.getItem().getContainerItem(fuelStack));
//                    inventory.insertItem(FUEL_SLOT, fuelStack.getItem().getContainerItem(fuelStack), false);
                }
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
        Random rand = new Random();
        if (rand.nextInt(3) != 0) return;
        float f = getPos().getX() + 0.5F;
        float f1 = getPos().getY() + 0.0F + rand.nextFloat() * 6.0F / 16.0F;
        float f2 = getPos().getZ() + 0.5F;
        float f3 = 0.5F;
        float f4 = rand.nextFloat() * 0.4F - 0.2F;
        switch (getRotation()) {
            case EAST:
                getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                getWorld().spawnParticle(EnumParticleTypes.FLAME, f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                break;
            case WEST:
                getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                getWorld().spawnParticle(EnumParticleTypes.FLAME, f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                break;
            case SOUTH:
                getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
                getWorld().spawnParticle(EnumParticleTypes.FLAME, f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
                break;
            case NORTH:
                getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
                getWorld().spawnParticle(EnumParticleTypes.FLAME, f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
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
        return Blockss.AIR_COMPRESSOR.getUnlocalizedName();
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
