package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.fluid.FuelRegistry;
import me.desht.pneumaticcraft.common.inventory.ContainerLiquidCompressor;
import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class TileEntityLiquidCompressor extends TileEntityPneumaticBase implements
        IRedstoneControl<TileEntityLiquidCompressor>, ISerializableTanks, INamedContainerProvider {
    public static final int INVENTORY_SIZE = 2;

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    @GuiSynced
    private final SmartSyncTank tank = new SmartSyncTank(this, PneumaticValues.NORMAL_TANK_CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return FuelRegistry.getInstance().getFuelValue(level, stack.getFluid()) > 0;
        }
    };

    private final ItemStackHandler itemHandler = new BaseItemStackHandler(this, INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || FluidUtil.getFluidHandler(itemStack) != null;
        }
    };
    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> tank);

    private double internalFuelBuffer;
    private float burnMultiplier = 1f;  // how fast this fuel burns (and produces pressure)
    @GuiSynced
    public final RedstoneController<TileEntityLiquidCompressor> rsController = new RedstoneController<>(this);
    @GuiSynced
    public float airPerTick;
    private float airBuffer;
    @DescSynced
    @GuiSynced
    public boolean isProducing;

    public TileEntityLiquidCompressor() {
        this(ModTileEntities.LIQUID_COMPRESSOR.get(), 5, 7, 5000);
    }

    TileEntityLiquidCompressor(TileEntityType type, float dangerPressure, float criticalPressure, int volume) {
        super(type, dangerPressure, criticalPressure, volume, 4);
    }

    public IFluidTank getTank() {
        return tank;
    }

    @Override
    public void tick() {
        super.tick();

        tank.tick();

        if (!getLevel().isClientSide) {
            processFluidItem(INPUT_SLOT, OUTPUT_SLOT);

            isProducing = false;

            airPerTick = getBaseProduction() * burnMultiplier * this.getSpeedMultiplierFromUpgrades() * (getHeatEfficiency() / 100f);

            if (rsController.shouldRun()) {
                double usageRate = getBaseProduction() * this.getSpeedUsageMultiplierFromUpgrades() * burnMultiplier;
                if (internalFuelBuffer < usageRate) {
                    double fuelValue = FuelRegistry.getInstance().getFuelValue(level, tank.getFluid().getFluid()) / 1000D;
                    if (fuelValue > 0) {
                        int usedFuel = Math.min(tank.getFluidAmount(), (int) (usageRate / fuelValue) + 1);
                        tank.drain(usedFuel, IFluidHandler.FluidAction.EXECUTE);
                        internalFuelBuffer += usedFuel * fuelValue;
                        burnMultiplier = FuelRegistry.getInstance().getBurnRateMultiplier(level, tank.getFluid().getFluid());
                    }
                }
                if (internalFuelBuffer >= usageRate) {
                    isProducing = true;
                    internalFuelBuffer -= usageRate;

                    airBuffer += airPerTick;
                    if (airBuffer >= 1f) {
                        int toAdd = (int) airBuffer;
                        addAir(toAdd);
                        airBuffer -= toAdd;
                        addHeatForAir(toAdd);
                    }
                }
            }
        } else {
            if (isProducing && level.random.nextInt(5) == 0) {
                ClientUtils.emitParticles(getLevel(), getBlockPos(), ParticleTypes.SMOKE);
            }
        }
    }

    protected void addHeatForAir(int air) {
        // do nothing, override in advanced
    }

    public int getHeatEfficiency() {
        return 100;
    }

    public int getBaseProduction() {
        return 10;
    }

    @Override
    public boolean canConnectPneumatic(Direction dir) {
        Direction orientation = getRotation();
        return orientation == dir || orientation == dir.getOpposite() || dir == Direction.UP;
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);

        tag.put("Items", itemHandler.serializeNBT());
        tag.putDouble("internalFuelBuffer", internalFuelBuffer);
        tag.putFloat("burnMultiplier", burnMultiplier);

        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        itemHandler.deserializeNBT(tag.getCompound("Items"));
        internalFuelBuffer = tag.getDouble("internalFuelBuffer");
        burnMultiplier = tag.getFloat("burnMultiplier");
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        rsController.parseRedstoneMode(tag);
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return itemHandler;
    }

    @Override
    public RedstoneController<TileEntityLiquidCompressor> getRedstoneController() {
        return rsController;
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
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return inventoryCap;
    }

    @Nonnull
    @Override
    public Map<String, PNCFluidTank> getSerializableTanks() {
        return ImmutableMap.of("Tank", tank);
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerLiquidCompressor(i, playerInventory, getBlockPos());
    }
}
