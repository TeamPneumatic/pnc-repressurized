package pneumaticCraft.common.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.LazySynced;
import pneumaticCraft.lib.TileEntityConstants;

public class TileEntityAssemblyPlatform extends TileEntityBase implements IAssemblyMachine, IResettable{
    @DescSynced
    private boolean shouldClawClose;
    @DescSynced
    @LazySynced
    public float clawProgress;
    public float oldClawProgress;
    @DescSynced
    private ItemStack[] inventory = new ItemStack[1];
    private float speed = 1.0F;
    public boolean hasDrilledStack;
    public boolean hasLaseredStack;

    @Override
    public void updateEntity(){
        super.updateEntity();
        oldClawProgress = clawProgress;
        if(!shouldClawClose && clawProgress > 0F) {
            clawProgress = Math.max(clawProgress - TileEntityConstants.ASSEMBLY_IO_UNIT_CLAW_SPEED * speed, 0);
        } else if(shouldClawClose && clawProgress < 1F) {
            clawProgress = Math.min(clawProgress + TileEntityConstants.ASSEMBLY_IO_UNIT_CLAW_SPEED * speed, 1);
        }
    }

    private boolean isClawDone(){
        return clawProgress == (shouldClawClose ? 1F : 0F);
    }

    @Override
    public boolean isIdle(){
        return !shouldClawClose && isClawDone() && inventory[0] == null;
    }

    @Override
    public boolean reset(){
        openClaw();
        return isIdle();
    }

    public boolean closeClaw(){
        hasDrilledStack = false;
        hasLaseredStack = false;
        shouldClawClose = true;
        return isClawDone();
    }

    public boolean openClaw(){
        shouldClawClose = false;
        return isClawDone();
    }

    public ItemStack getHeldStack(){
        return inventory[0];
    }

    public void setHeldStack(ItemStack stack){
        if(stack == null) {
            hasDrilledStack = false;
            hasLaseredStack = false;
        }
        inventory[0] = stack;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("clawClosing", shouldClawClose);
        tag.setFloat("clawProgress", clawProgress);
        tag.setFloat("speed", speed);
        tag.setBoolean("drilled", hasDrilledStack);
        tag.setBoolean("lasered", hasLaseredStack);
        // Write the ItemStacks in the inventory to NBT
        NBTTagList tagList = new NBTTagList();
        for(int currentIndex = 0; currentIndex < inventory.length; ++currentIndex) {
            if(inventory[currentIndex] != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)currentIndex);
                inventory[currentIndex].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        tag.setTag("Items", tagList);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        shouldClawClose = tag.getBoolean("clawClosing");
        clawProgress = tag.getFloat("clawProgress");
        speed = tag.getFloat("speed");
        hasDrilledStack = tag.getBoolean("drilled");
        hasLaseredStack = tag.getBoolean("lasered");
        // Read in the ItemStacks in the inventory from NBT
        NBTTagList tagList = tag.getTagList("Items", 10);
        inventory = new ItemStack[1];
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
    }

    @Override
    public void setSpeed(float speed){
        this.speed = speed;
    }

}
