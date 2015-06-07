package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.util.FluidUtils;
import pneumaticCraft.common.util.IOHelper;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityLiquidHopper extends TileEntityOmnidirectionalHopper implements IFluidHandler{

    @DescSynced
    private final FluidTank tank = new FluidTank(PneumaticValues.NORMAL_TANK_CAPACITY);

    public TileEntityLiquidHopper(){
        setUpgradeSlots(0, 1, 2, 3);
    }

    @Override
    protected int getInvSize(){
        return 4;
    }

    @Override
    public String getInventoryName(){
        return Blockss.liquidHopper.getUnlocalizedName();
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int var1){
        return new int[]{0, 1, 2, 3};
    }

    @Override
    protected boolean exportItem(int maxItems){
        ForgeDirection dir = ForgeDirection.getOrientation(getBlockMetadata());
        if(tank.getFluid() != null) {
            TileEntity neighbor = IOHelper.getNeighbor(this, dir);
            if(neighbor instanceof IFluidHandler) {
                IFluidHandler fluidHandler = (IFluidHandler)neighbor;
                if(fluidHandler.canFill(dir.getOpposite(), tank.getFluid().getFluid())) {
                    FluidStack fluid = tank.getFluid().copy();
                    fluid.amount = Math.min(maxItems * 100, tank.getFluid().amount - (leaveMaterial ? 1000 : 0));
                    if(fluid.amount > 0) {
                        tank.getFluid().amount -= fluidHandler.fill(dir.getOpposite(), fluid, true);
                        if(tank.getFluidAmount() <= 0) tank.setFluid(null);
                        return true;
                    }
                }
            }
        }

        if(worldObj.isAirBlock(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ)) {
            for(EntityItem entity : getNeighborItems(this, dir)) {
                if(!entity.isDead) {
                    List<ItemStack> returnedItems = new ArrayList<ItemStack>();
                    if(FluidUtils.tryExtractingLiquid(this, entity.getEntityItem(), returnedItems)) {
                        if(entity.getEntityItem().stackSize <= 0) entity.setDead();
                        for(ItemStack stack : returnedItems) {
                            EntityItem item = new EntityItem(worldObj, entity.posX, entity.posY, entity.posZ, stack);
                            item.motionX = entity.motionX;
                            item.motionY = entity.motionY;
                            item.motionZ = entity.motionZ;
                            worldObj.spawnEntityInWorld(item);
                        }
                        return true;
                    }
                }
            }
        }

        if(getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) > 0) {
            if(worldObj.isAirBlock(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ)) {
                FluidStack extractedFluid = drain(ForgeDirection.UNKNOWN, 1000, false);
                if(extractedFluid != null && extractedFluid.amount == 1000) {
                    Block fluidBlock = extractedFluid.getFluid().getBlock();
                    if(fluidBlock != null) {
                        drain(ForgeDirection.UNKNOWN, 1000, true);
                        worldObj.setBlock(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, fluidBlock);
                    }
                }
            }
        }

        return false;
    }

    @Override
    protected boolean suckInItem(int maxItems){
        TileEntity inputInv = IOHelper.getNeighbor(this, inputDir);
        if(inputInv instanceof IFluidHandler) {
            IFluidHandler fluidHandler = (IFluidHandler)inputInv;

            FluidStack fluid = fluidHandler.drain(inputDir.getOpposite(), maxItems * 100, false);
            if(fluid != null && fluidHandler.canDrain(inputDir.getOpposite(), fluid.getFluid())) {
                int filledFluid = fill(inputDir, fluid, true);
                if(filledFluid > 0) {
                    fluidHandler.drain(inputDir.getOpposite(), filledFluid, true);
                    return true;
                }
            }
        }

        if(worldObj.isAirBlock(xCoord + inputDir.offsetX, yCoord + inputDir.offsetY, zCoord + inputDir.offsetZ)) {
            for(EntityItem entity : getNeighborItems(this, inputDir)) {
                if(!entity.isDead) {
                    List<ItemStack> returnedItems = new ArrayList<ItemStack>();
                    if(FluidUtils.tryInsertingLiquid(this, entity.getEntityItem(), false, returnedItems)) {
                        if(entity.getEntityItem().stackSize <= 0) entity.setDead();
                        for(ItemStack stack : returnedItems) {
                            EntityItem item = new EntityItem(worldObj, entity.posX, entity.posY, entity.posZ, stack);
                            item.motionX = entity.motionX;
                            item.motionY = entity.motionY;
                            item.motionZ = entity.motionZ;
                            worldObj.spawnEntityInWorld(item);
                        }
                        return true;
                    }
                }
            }
        }

        if(getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) > 0 && worldObj.getBlockMetadata(xCoord + inputDir.offsetX, yCoord + inputDir.offsetY, zCoord + inputDir.offsetZ) == 0) {
            Fluid fluid = FluidRegistry.lookupFluidForBlock(worldObj.getBlock(xCoord + inputDir.offsetX, yCoord + inputDir.offsetY, zCoord + inputDir.offsetZ));
            if(fluid != null) {
                if(fill(ForgeDirection.UNKNOWN, new FluidStack(fluid, 1000), false) == 1000) {
                    fill(ForgeDirection.UNKNOWN, new FluidStack(fluid, 1000), true);
                    worldObj.setBlockToAir(xCoord + inputDir.offsetX, yCoord + inputDir.offsetY, zCoord + inputDir.offsetZ);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill){
        return tank.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain){
        return tank.getFluid() != null && tank.getFluid().isFluidEqual(resource) ? drain(ForgeDirection.UNKNOWN, resource.amount, doDrain) : null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain){
        return tank.drain(leaveMaterial ? Math.min(maxDrain, tank.getFluidAmount() - 1000) : maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid){
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid){
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from){
        return new FluidTankInfo[]{new FluidTankInfo(tank)};
    }

    @SideOnly(Side.CLIENT)
    public FluidTank getTank(){
        return tank;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);

        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag);
        tag.setTag("tank", tankTag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        tank.readFromNBT(tag.getCompoundTag("tank"));
    }
}
