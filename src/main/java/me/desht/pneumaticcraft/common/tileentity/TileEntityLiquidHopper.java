package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.util.FluidUtils;
import me.desht.pneumaticcraft.common.util.IOHelper;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class TileEntityLiquidHopper extends TileEntityOmnidirectionalHopper {
    @DescSynced
    private final FluidTank tank = new FluidTank(PneumaticValues.NORMAL_TANK_CAPACITY);

    public TileEntityLiquidHopper() {
        super(4);
        addApplicableUpgrade(EnumUpgrade.DISPENSER);
    }

    @Override
    protected int getInvSize() {
        return 0;
    }

    @Override
    public String getName() {
        return Blockss.LIQUID_HOPPER.getUnlocalizedName();
    }

    @Override
    protected boolean doExport(int maxItems) {
        EnumFacing dir = getRotation();
        if (tank.getFluid() != null) {
            TileEntity neighbor = IOHelper.getNeighbor(this, dir);
            if (neighbor != null && neighbor.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite())) {
                IFluidHandler fluidHandler = neighbor.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
                int amount = Math.min(maxItems * 100, tank.getFluid().amount - (leaveMaterial ? 1000 : 0));
                FluidStack transferred = FluidUtil.tryFluidTransfer(fluidHandler, tank, amount, true);
                return transferred != null && transferred.amount > 0;
            }
        }

        if (getWorld().isAirBlock(getPos().offset(dir))) {
            for (EntityItem entity : getNeighborItems(this, dir)) {
                if (!entity.isDead) {
                    NonNullList<ItemStack> returnedItems = NonNullList.create();
                    if (FluidUtils.tryExtractingLiquid(tank, entity.getItem(), returnedItems)) {
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
        TileEntity inputInv = IOHelper.getNeighbor(this, inputDir);
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
                if (!entity.isDead) {
                    NonNullList<ItemStack> returnedItems = NonNullList.create();
                    if (FluidUtils.tryInsertingLiquid(this, entity.getItem(), returnedItems)) {
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
        }

        if (getUpgrades(EnumUpgrade.DISPENSER) > 0) {
            BlockPos neighborPos = getPos().offset(inputDir);
            Fluid fluid = FluidRegistry.lookupFluidForBlock(getWorld().getBlockState(neighborPos).getBlock());
            if (fluid != null && FluidUtils.isSourceBlock(getWorld(), neighborPos)) {
                if (tank.fill(new FluidStack(fluid, 1000), false) == 1000) {
                    tank.fill(new FluidStack(fluid, 1000), true);
                    getWorld().setBlockToAir(neighborPos);
                    return true;
                }
            }
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    public FluidTank getTank() {
        return tank;
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
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tank);
        } else {
            return super.getCapability(capability, facing);
        }
    }
}
