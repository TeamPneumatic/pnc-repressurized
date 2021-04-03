package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.block.BlockAirCompressor;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerAirCompressor;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityAirCompressor extends TileEntityPneumaticBase implements IRedstoneControl<TileEntityAirCompressor>, INamedContainerProvider {
    private static final int INVENTORY_SIZE = 1;

    private final AirCompressorFuelHandler itemHandler = new AirCompressorFuelHandler();
    private final LazyOptional<IItemHandler> inventory = LazyOptional.of(() -> itemHandler);

    private static final int FUEL_SLOT = 0;

    @GuiSynced
    public int burnTime;
    @GuiSynced
    private int maxBurnTime; // in here the total burn time of the current burning item is stored.
    @GuiSynced
    public final RedstoneController<TileEntityAirCompressor> rsController = new RedstoneController<>(this);
    @DescSynced
    private boolean isActive;
    @GuiSynced
    public int curFuelUsage;
    @GuiSynced
    public float airPerTick;
    private float airBuffer;

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

    public boolean isActive() {
        return isActive;
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isRemote) {
            airPerTick = getBaseProduction() * getSpeedMultiplierFromUpgrades() * getHeatEfficiency() / 100F;

            if (rsController.shouldRun() && burnTime < curFuelUsage) {
                ItemStack fuelStack = itemHandler.getStackInSlot(FUEL_SLOT);
                int itemBurnTime = PneumaticCraftUtils.getBurnTime(fuelStack);
                if (itemBurnTime > 0) {
                    burnTime += itemBurnTime;
                    maxBurnTime = burnTime;
                    if (fuelStack.hasContainerItem()) {
                        itemHandler.setStackInSlot(FUEL_SLOT, fuelStack.getContainerItem());
                    } else {
                        itemHandler.extractItem(FUEL_SLOT, 1, false);
                    }
                }
            }

            curFuelUsage = (int) (getBaseProduction() * getSpeedUsageMultiplierFromUpgrades() / 10);
            if (burnTime >= curFuelUsage) {
                burnTime -= curFuelUsage;
                if (!getWorld().isRemote) {
                    airBuffer += airPerTick;
                    if (airBuffer >= 1f) {
                        int toAdd = (int) airBuffer;
                        addAir(toAdd);
                        airBuffer -= toAdd;
                        addHeatForAir(toAdd);
                    }
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

    protected void addHeatForAir(int air) {
        // do nothing, override in advanced
    }

    public int getHeatEfficiency() {
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
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        rsController.parseRedstoneMode(tag);
    }

    @Override
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
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        burnTime = tag.getInt("burnTime");
        maxBurnTime = tag.getInt("maxBurn");
        itemHandler.deserializeNBT(tag.getCompound("Items"));
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putInt("burnTime", burnTime);
        tag.putInt("maxBurn", maxBurnTime);
        tag.put("Items", itemHandler.serializeNBT());
        return tag;
    }

    @Override
    public RedstoneController<TileEntityAirCompressor> getRedstoneController() {
        return rsController;
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
