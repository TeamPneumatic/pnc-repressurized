package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class TileEntityLiquidHopper extends TileEntityOmnidirectionalHopper implements ISerializableTanks, ISmartFluidSync {
    private int comparatorValue = -1;

    @LazySynced
    @DescSynced
    @GuiSynced
    private final SmartSyncTank tank;

    @SuppressWarnings("unused")
    @DescSynced
    private int fluidAmountScaled;

    private final WrappedFluidTank inputWrapper, outputWrapper;

    public TileEntityLiquidHopper() {
        super(4);
        addApplicableUpgrade(EnumUpgrade.DISPENSER);
        tank = new SmartSyncTank(this, PneumaticValues.NORMAL_TANK_CAPACITY) {
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                comparatorValue = -1;
            }
        };
        inputWrapper = new WrappedFluidTank(tank, true);
        outputWrapper = new WrappedFluidTank(tank, false);
    }

    @Override
    protected int getInvSize() {
        return 0;
    }

    @Override
    public String getName() {
        return Blockss.LIQUID_HOPPER.getTranslationKey();
    }

    @Override
    protected int getComparatorValueInternal() {
        if (comparatorValue < 0) {
            if (tank.getFluidAmount() == 0) return 0;
            FluidStack fluidStack = tank.getFluid();
            comparatorValue = (int) (1 + ((float) fluidStack.amount / tank.getCapacity() * 14f));
        }
        return comparatorValue;
    }

    @Override
    protected boolean doExport(int maxItems) {
        EnumFacing dir = getRotation();
        if (tank.getFluid() != null) {
            TileEntity neighbor = getCachedNeighbor(dir);
            if (neighbor != null && neighbor.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite())) {
                IFluidHandler fluidHandler = neighbor.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
                int amount = Math.min(maxItems * 100, tank.getFluid().amount - (leaveMaterial ? 1000 : 0));
                FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandler, tank, amount, true);
                return transferred != null && transferred.amount > 0;
            }
        }

        if (getWorld().isAirBlock(getPos().offset(dir))) {
            for (EntityItem entity : getNeighborItems(this, dir)) {
                NonNullList<ItemStack> returnedItems = NonNullList.create();
                if (FluidUtils.tryFluidExtraction(tank, entity.getItem(), returnedItems)) {
                    if (entity.getItem().getCount() <= 0) entity.setDead();
                    for (ItemStack stack : returnedItems) {
                        EntityItem item = new EntityItem(getWorld(), entity.posX, entity.posY, entity.posZ, stack);
                        item.motionX = entity.motionX;
                        item.motionY = entity.motionY;
                        item.motionZ = entity.motionZ;
                        getWorld().spawnEntity(item);
                    }
                    return true;
                }
            }
        }

        if (getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            if (getWorld().isAirBlock(getPos().offset(dir))) {
                FluidStack extractedFluid = tank.drain(1000, false);
                if (extractedFluid != null && extractedFluid.amount == 1000) {
                    Block fluidBlock = extractedFluid.getFluid().getBlock();
                    if (fluidBlock != null) {
                        tank.drain(1000, true);
                        getWorld().setBlockState(getPos().offset(dir), fluidBlock.getDefaultState());
                    }
                }
            }
        }

        return false;
    }

    @Override
    protected boolean doImport(int maxItems) {
        TileEntity inputInv = getCachedNeighbor(inputDir);
        if (inputInv != null && inputInv.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inputDir.getOpposite())) {
            IFluidHandler fluidHandler = inputInv.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inputDir.getOpposite());
            FluidStack fluid = fluidHandler.drain(maxItems * 100, false);
            if (fluid != null) {
                int filledFluid = tank.fill(fluid, true);
                if (filledFluid > 0) {
                    fluidHandler.drain(filledFluid, true);
                    return true;
                }
            }
        }

        if (getWorld().isAirBlock(getPos().offset(inputDir))) {
            for (EntityItem entity : getNeighborItems(this, inputDir)) {
                NonNullList<ItemStack> returnedItems = NonNullList.create();
                if (FluidUtils.tryFluidInsertion(tank, entity.getItem(), returnedItems)) {
                    if (entity.getItem().isEmpty()) entity.setDead();
                    for (ItemStack stack : returnedItems) {
                        EntityItem item = new EntityItem(getWorld(), entity.posX, entity.posY, entity.posZ, stack);
                        item.motionX = entity.motionX;
                        item.motionY = entity.motionY;
                        item.motionZ = entity.motionZ;
                        getWorld().spawnEntity(item);
                    }
                    return true;
                }
            }
        }

        if (getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            BlockPos neighborPos = getPos().offset(inputDir);
            FluidStack fluidStack = FluidUtils.getFluidAt(getWorld(), neighborPos, false);
            if (fluidStack != null && fluidStack.amount == Fluid.BUCKET_VOLUME) {
                if (tank.fill(fluidStack, false) == Fluid.BUCKET_VOLUME) {
                    tank.fill(fluidStack, true);
                    FluidUtils.getFluidAt(getWorld(), neighborPos, true);
                    return true;
                }
            }
        }

        return false;
    }

    public FluidTank getTank() {
        return tank;
    }

    public EnumFacing getInputDirection() {
        return inputDir;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag);
        tag.setTag("tank", tankTag);

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        tank.readFromNBT(tag.getCompoundTag("tank"));
        fluidAmountScaled = tank.getScaledFluidAmount();
        comparatorValue = -1;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (facing == inputDir) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(inputWrapper);
            } else if (facing == getRotation()) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(outputWrapper);
            } else {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tank);
            }
        } else {
            return super.getCapability(capability, facing);
        }
    }

    @Nonnull
    @Override
    public Map<String, FluidTank> getSerializableTanks() {
        return ImmutableMap.of("Tank", tank);
    }

    @Override
    public void updateScaledFluidAmount(int tankIndex, int amount) {
        fluidAmountScaled = amount;
    }

    class WrappedFluidTank implements IFluidTank, IFluidHandler {
        private final FluidTank wrappedTank;
        private final boolean inbound;

        WrappedFluidTank(FluidTank wrappedTank, boolean inbound) {
            // inbound == true: fill *only*, inbound == false: drain *only*
            this.wrappedTank = wrappedTank;
            this.inbound = inbound;
        }

        @Nullable
        @Override
        public FluidStack getFluid() {
            return wrappedTank.getFluid();
        }

        @Override
        public int getFluidAmount() {
            return wrappedTank.getFluidAmount();
        }

        @Override
        public int getCapacity() {
            return wrappedTank.getCapacity();
        }

        @Override
        public FluidTankInfo getInfo() {
            return wrappedTank.getInfo();
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return wrappedTank.getTankProperties();
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return inbound ? wrappedTank.fill(resource, doFill) : 0;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return inbound ? null : tank.drain(resource, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return inbound ? null : wrappedTank.drain(maxDrain, doDrain);
        }
    }
}
