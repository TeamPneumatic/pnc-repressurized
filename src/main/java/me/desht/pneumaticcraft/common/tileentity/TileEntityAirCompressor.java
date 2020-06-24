package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.block.BlockAirCompressor;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerAirCompressor;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.NBTKeys;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityAirCompressor extends TileEntityPneumaticBase implements IRedstoneControlled, INamedContainerProvider {
    private static final int INVENTORY_SIZE = 1;

    private final AirCompressorFuelHandler itemHandler = new AirCompressorFuelHandler();
    private final LazyOptional<IItemHandler> inventory = LazyOptional.of(() -> itemHandler);

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

    public TileEntityAirCompressor() {
        this(ModTileEntities.AIR_COMPRESSOR.get(), PneumaticValues.DANGER_PRESSURE_AIR_COMPRESSOR, PneumaticValues.MAX_PRESSURE_AIR_COMPRESSOR, PneumaticValues.VOLUME_AIR_COMPRESSOR);
    }

    TileEntityAirCompressor(TileEntityType type, float dangerPressure, float criticalPressure, int volume) {
        super(type, dangerPressure, criticalPressure, volume, 4);
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerAirCompressor(i, playerInventory, getPos());
    }
    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isRemote) {
            if (redstoneAllows() && burnTime < curFuelUsage) {
                ItemStack fuelStack = itemHandler.getStackInSlot(FUEL_SLOT);
                int itemBurnTime = PneumaticCraftUtils.getBurnTime(fuelStack);
                if (itemBurnTime > 0) {
                    burnTime += itemBurnTime;
                    maxBurnTime = burnTime;
                    fuelStack.shrink(1);
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
                getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(BlockAirCompressor.ON, isActive));
            }
            airHandler.setSideLeaking(hasNoConnectedAirHandlers() ? getRotation() : null);
        } else {
            if (isActive) spawnBurningParticle();
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
                getWorld().addParticle(ParticleTypes.SMOKE, px - f3, py, pz + f4, 0.0D, 0.0D, 0.0D);
                getWorld().addParticle(ParticleTypes.FLAME, px - f3, py, pz + f4, 0.0D, 0.0D, 0.0D);
                break;
            case WEST:
                getWorld().addParticle(ParticleTypes.SMOKE, px + f3, py, pz + f4, 0.0D, 0.0D, 0.0D);
                getWorld().addParticle(ParticleTypes.FLAME, px + f3, py, pz + f4, 0.0D, 0.0D, 0.0D);
                break;
            case SOUTH:
                getWorld().addParticle(ParticleTypes.SMOKE, px + f4, py, pz - f3, 0.0D, 0.0D, 0.0D);
                getWorld().addParticle(ParticleTypes.FLAME, px + f4, py, pz - f3, 0.0D, 0.0D, 0.0D);
                break;
            case NORTH:
                getWorld().addParticle(ParticleTypes.SMOKE, px + f4, py, pz + f3, 0.0D, 0.0D, 0.0D);
                getWorld().addParticle(ParticleTypes.FLAME, px + f4, py, pz + f3, 0.0D, 0.0D, 0.0D);
                break;
        }
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return getRotation() == side;
    }

    public int getBurnTimeRemainingScaled(int parts) {
        if (maxBurnTime == 0 || burnTime < curFuelUsage) return 0;
        return parts * burnTime / maxBurnTime;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return itemHandler;
    }

    @Nonnull
    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return inventory;
    }

    @Override
    public void read(CompoundNBT nbtTagCompound) {
        super.read(nbtTagCompound);
        burnTime = nbtTagCompound.getInt("burnTime");
        maxBurnTime = nbtTagCompound.getInt("maxBurn");
        redstoneMode = nbtTagCompound.getInt(NBTKeys.NBT_REDSTONE_MODE);
        itemHandler.deserializeNBT(nbtTagCompound.getCompound("Items"));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTagCompound) {
        super.write(nbtTagCompound);
        nbtTagCompound.putInt("burnTime", burnTime);
        nbtTagCompound.putInt("maxBurn", maxBurnTime);
        nbtTagCompound.putInt(NBTKeys.NBT_REDSTONE_MODE, redstoneMode);
        nbtTagCompound.put("Items", itemHandler.serializeNBT());
        return nbtTagCompound;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    private class AirCompressorFuelHandler extends BaseItemStackHandler {
        AirCompressorFuelHandler() {
            super(TileEntityAirCompressor.this, INVENTORY_SIZE);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return slot == FUEL_SLOT &&
                    (itemStack.isEmpty() || PneumaticCraftUtils.getBurnTime(itemStack) > 0 && !FluidUtil.getFluidContained(itemStack).isPresent());
        }
    }

}
