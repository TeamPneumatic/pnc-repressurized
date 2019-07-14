package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerLiquidCompressor;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class TileEntityLiquidCompressor extends TileEntityPneumaticBase implements IRedstoneControlled, ISerializableTanks, INamedContainerProvider {
    public static final int INVENTORY_SIZE = 2;

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    @GuiSynced
    private final FluidTank tank = new FluidTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    private final ItemStackHandler itemHandler = new BaseItemStackHandler(this, INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || FluidUtil.getFluidHandler(itemStack) != null;
        }
    };
    private final LazyOptional<IItemHandlerModifiable> inventoryCap = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> tank);

    @GuiSynced
    public int redstoneMode;
    private double internalFuelBuffer;
    @DescSynced
    @GuiSynced
    public boolean isProducing;

    public TileEntityLiquidCompressor() {
        this(5, 7, 5000);
    }

    public TileEntityLiquidCompressor(float dangerPressure, float criticalPressure, int volume) {
        super(ModTileEntityTypes.LIQUID_COMPRESSOR, dangerPressure, criticalPressure, volume, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
    }

    public FluidTank getTank() {
        return tank;
    }

    private int getFuelValue(FluidStack fluid) {
        return fluid == null ? 0 : getFuelValue(fluid.getFluid());
    }

    private int getFuelValue(Fluid fluid) {
        Integer value = PneumaticCraftAPIHandler.getInstance().liquidFuels.get(fluid.getName());
        return value == null ? 0 : value;
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isRemote) {
            processFluidItem(INPUT_SLOT, OUTPUT_SLOT);

            isProducing = false;
            if (redstoneAllows()) {
                int usageRate = (int) (getBaseProduction() * this.getSpeedUsageMultiplierFromUpgrades());
                if (internalFuelBuffer < usageRate) {
                    double fuelValue = getFuelValue(tank.getFluid()) / 1000D;
                    if (fuelValue > 0) {
                        int usedFuel = Math.min(tank.getFluidAmount(), (int) (usageRate / fuelValue) + 1);
                        tank.drain(usedFuel, true);
                        internalFuelBuffer += usedFuel * fuelValue;
                    }
                }
                if (internalFuelBuffer >= usageRate) {
                    isProducing = true;
                    internalFuelBuffer -= usageRate;
                    onFuelBurn(usageRate);
                    addAir((int) (getBaseProduction() * this.getSpeedMultiplierFromUpgrades() * getEfficiency() / 100));
                }
            }
        } else {
            if (isProducing && world.rand.nextInt(5) == 0) {
                ClientUtils.emitParticles(getWorld(), getPos(), ParticleTypes.SMOKE);
            }
        }
    }

    protected void onFuelBurn(int burnedFuel) {
    }

    public int getEfficiency() {
        return 100;
    }

    public int getBaseProduction() {
        return 10;
    }

    @Override
    public boolean canConnectTo(Direction dir) {
        Direction orientation = getRotation();
        return orientation == dir || orientation == dir.getOpposite() || dir == Direction.UP;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.put("Items", itemHandler.serializeNBT());
        tag.putByte("redstoneMode", (byte) redstoneMode);
        tag.putDouble("internalFuelBuffer", internalFuelBuffer);

        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        itemHandler.deserializeNBT(tag.getCompound("Items"));
        redstoneMode = tag.getByte("redstoneMode");
        internalFuelBuffer = tag.getDouble("internalFuelBuffer");
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return itemHandler;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction facing) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidCap.cast();
        } else {
            return super.getCapability(cap, facing);
        }
    }

    @Nonnull
    @Override
    public LazyOptional<IItemHandlerModifiable> getInventoryCap() {
        return inventoryCap;
    }

    @Nonnull
    @Override
    public Map<String, FluidTank> getSerializableTanks() {
        return ImmutableMap.of("Tank", tank);
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerLiquidCompressor(i, playerInventory, getPos());
    }
}
