package pneumaticCraft.common.tileentity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityKeroseneLamp extends TileEntityBase implements IFluidHandler, IRedstoneControlled,
        ISidedInventory{
    private final Set<ChunkPosition> managingLights = new HashSet<ChunkPosition>();
    @DescSynced
    private boolean isOn;
    @GuiSynced
    private int range;
    @GuiSynced
    private int targetRange = 10;
    @GuiSynced
    private int redstoneMode;
    @GuiSynced
    private int fuel;
    private static final int LIGHT_SPACING = 3;
    public static final int FUEL_PER_MB = 10000;
    private int checkingX, checkingY, checkingZ;
    @DescSynced
    private ForgeDirection sideConnected = ForgeDirection.DOWN;

    @DescSynced
    private final FluidTank tank = new FluidTank(1000);

    private final ItemStack[] inventory = new ItemStack[2];

    @Override
    public void updateEntity(){
        super.updateEntity();
        if(!worldObj.isRemote) {
            processFluidItem(0, 1);
            if(worldObj.getTotalWorldTime() % 5 == 0) {
                int realTargetRange = redstoneAllows() ? targetRange : 0;
                if(redstoneMode == 3) realTargetRange = (int)(poweredRedstone / 15D * targetRange);
                updateRange(Math.min(realTargetRange, tank.getFluidAmount())); //Fade out the lamp when almost empty.
                updateLights();
                useFuel();
            }
        } else {
            if(isOn && worldObj.getTotalWorldTime() % 5 == 0) {
                worldObj.spawnParticle("flame", xCoord + 0.4 + 0.2 * worldObj.rand.nextDouble(), yCoord + 0.2 + tank.getFluidAmount() / 1000D * 3 / 16D, zCoord + 0.4 + 0.2 * worldObj.rand.nextDouble(), 0, 0, 0);
            }
        }
    }

    private void useFuel(){
        fuel -= Math.pow(range, 3);
        if(fuel < 0 && tank.drain(1, true) != null) {
            fuel += FUEL_PER_MB;
        }
        if(fuel < 0) fuel = 0;
    }

    @Override
    public void validate(){
        super.validate();
        checkingX = xCoord;
        checkingY = yCoord;
        checkingZ = zCoord;
    }

    @Override
    public void invalidate(){
        super.invalidate();
        for(ChunkPosition pos : managingLights) {
            if(isLampLight(pos)) {
                worldObj.setBlockToAir(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
            }
        }
    }

    private boolean isLampLight(ChunkPosition pos){
        return worldObj.getBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ) == Blockss.keroseneLampLight;
    }

    private void updateLights(){
        int roundedRange = range / LIGHT_SPACING * LIGHT_SPACING;
        checkingX += LIGHT_SPACING;
        if(checkingX > xCoord + roundedRange) {
            checkingX = xCoord - roundedRange;
            checkingY += LIGHT_SPACING;
            if(checkingY > yCoord + roundedRange) {
                checkingY = yCoord - roundedRange;
                checkingZ += LIGHT_SPACING;
                if(checkingZ > zCoord + roundedRange) checkingZ = zCoord - roundedRange;
            }
        }
        ChunkPosition pos = new ChunkPosition(checkingX, checkingY, checkingZ);
        ChunkPosition lampPos = new ChunkPosition(xCoord, yCoord, zCoord);
        if(managingLights.contains(pos)) {
            if(isLampLight(pos)) {
                if(!passesRaytraceTest(pos, lampPos)) {
                    worldObj.setBlockToAir(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
                    managingLights.remove(pos);
                }
            } else {
                managingLights.remove(pos);
            }
        } else {
            tryAddLight(pos, lampPos);
        }
    }

    private void updateRange(int targetRange){
        if(targetRange > range) {
            range++;
            ChunkPosition lampPos = new ChunkPosition(xCoord, yCoord, zCoord);
            int roundedRange = range / LIGHT_SPACING * LIGHT_SPACING;
            for(int x = -roundedRange; x <= roundedRange; x += LIGHT_SPACING) {
                for(int y = -roundedRange; y <= roundedRange; y += LIGHT_SPACING) {
                    for(int z = -roundedRange; z <= roundedRange; z += LIGHT_SPACING) {
                        ChunkPosition pos = new ChunkPosition(x + xCoord, y + yCoord, z + zCoord);
                        if(!managingLights.contains(pos)) {
                            tryAddLight(pos, lampPos);
                        }
                    }
                }
            }
        } else if(targetRange < range) {
            range--;
            Iterator<ChunkPosition> iterator = managingLights.iterator();
            ChunkPosition lampPos = new ChunkPosition(xCoord, yCoord, zCoord);
            while(iterator.hasNext()) {
                ChunkPosition pos = iterator.next();
                if(!isLampLight(pos)) {
                    iterator.remove();
                } else if(PneumaticCraftUtils.distBetween(pos, lampPos) > range) {
                    worldObj.setBlockToAir(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
                    iterator.remove();
                }
            }
        }
        isOn = range > 0;
    }

    private boolean passesRaytraceTest(ChunkPosition pos, ChunkPosition lampPos){
        MovingObjectPosition mop = worldObj.rayTraceBlocks(Vec3.createVectorHelper(pos.chunkPosX + 0.5, pos.chunkPosY + 0.5, pos.chunkPosZ + 0.5), Vec3.createVectorHelper(lampPos.chunkPosX + 0.5, lampPos.chunkPosY + 0.5, lampPos.chunkPosZ + 0.5));
        return mop != null && lampPos.equals(new ChunkPosition(mop.blockX, mop.blockY, mop.blockZ));
    }

    private boolean tryAddLight(ChunkPosition pos, ChunkPosition lampPos){
        if(PneumaticCraftUtils.distBetween(pos, lampPos) <= range) {
            if(worldObj.isAirBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ) && !isLampLight(pos)) {
                if(passesRaytraceTest(pos, lampPos)) {
                    worldObj.setBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, Blockss.keroseneLampLight);
                    managingLights.add(pos);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onNeighborBlockUpdate(){
        super.onNeighborBlockUpdate();
        sideConnected = ForgeDirection.DOWN;
        for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            int x = xCoord + d.offsetX;
            int y = yCoord + d.offsetY;
            int z = zCoord + d.offsetZ;
            Block block = worldObj.getBlock(x, y, z);
            if(block.isSideSolid(worldObj, x, y, z, d.getOpposite())) {
                sideConnected = d;
                break;
            }
        }
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill){
        return canFill(from, resource.getFluid()) ? tank.fill(resource, doFill) : 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain){
        return tank.getFluid() != null && tank.getFluid().isFluidEqual(resource) ? drain(ForgeDirection.UNKNOWN, resource.amount, doDrain) : null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain){
        return tank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid){
        return Fluids.areFluidsEqual(fluid, Fluids.kerosene);
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid){
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from){
        return new FluidTankInfo[]{new FluidTankInfo(tank)};
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        NBTTagList lights = new NBTTagList();
        for(ChunkPosition pos : managingLights) {
            NBTTagCompound t = new NBTTagCompound();
            t.setInteger("x", pos.chunkPosX);
            t.setInteger("y", pos.chunkPosY);
            t.setInteger("z", pos.chunkPosZ);
            lights.appendTag(t);
        }
        tag.setTag("lights", lights);

        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag);
        tag.setTag("tank", tankTag);
        tag.setByte("redstoneMode", (byte)redstoneMode);
        tag.setByte("targetRange", (byte)targetRange);
        tag.setByte("range", (byte)range);
        tag.setByte("sideConnected", (byte)sideConnected.ordinal());
        writeInventoryToNBT(tag, inventory);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        managingLights.clear();
        NBTTagList lights = tag.getTagList("lights", 10);
        for(int i = 0; i < lights.tagCount(); i++) {
            NBTTagCompound t = lights.getCompoundTagAt(i);
            managingLights.add(new ChunkPosition(t.getInteger("x"), t.getInteger("y"), t.getInteger("z")));
        }
        tank.readFromNBT(tag.getCompoundTag("tank"));
        redstoneMode = tag.getByte("redstoneMode");
        targetRange = tag.getByte("targetRange");
        range = tag.getByte("range");
        sideConnected = ForgeDirection.getOrientation(tag.getByte("sideConnected"));
        readInventoryFromNBT(tag, inventory);
    }

    @Override
    public boolean redstoneAllows(){
        if(redstoneMode == 3) return true;
        return super.redstoneAllows();
    }

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            redstoneMode++;
            if(redstoneMode > 3) redstoneMode = 0;
        } else if(buttonID > 0 && buttonID <= 30) {
            targetRange = buttonID;
        }
    }

    @SideOnly(Side.CLIENT)
    public IFluidTank getTank(){
        return tank;
    }

    public int getRange(){
        return range;
    }

    public int getTargetRange(){
        return targetRange;
    }

    public int getFuel(){
        return fuel;
    }

    public ForgeDirection getSideConnected(){
        return sideConnected;
    }

    /*
     * ---------------IInventory---------------------
     */

    /**
     * Returns the name of the inventory.
     */
    @Override
    public String getInventoryName(){
        return Blockss.keroseneLamp.getUnlocalizedName();
    }

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory(){
        return inventory.length;
    }

    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int par1){
        return inventory[par1];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount){
        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            if(itemStack.stackSize <= amount) {
                setInventorySlotContents(slot, null);
            } else {
                itemStack = itemStack.splitStack(amount);
                if(itemStack.stackSize == 0) {
                    setInventorySlotContents(slot, null);
                }
            }
        }
        return itemStack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot){
        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            setInventorySlotContents(slot, null);
        }
        return itemStack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack){
        inventory[slot] = itemStack;
        if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack){
        return slot == 1 ? false : stack != null && (FluidContainerRegistry.getFluidForFilledItem(stack) != null || stack.getItem() instanceof IFluidContainerItem && ((IFluidContainerItem)stack.getItem()).getFluid(stack) != null);
    }

    @Override
    public boolean hasCustomInventoryName(){
        return false;
    }

    @Override
    public int getInventoryStackLimit(){
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_){
        return isGuiUseableByPlayer(p_70300_1_);
    }

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){}

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_){
        return new int[]{0, 1};
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side){
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side){
        return slot == 1;
    }
}
