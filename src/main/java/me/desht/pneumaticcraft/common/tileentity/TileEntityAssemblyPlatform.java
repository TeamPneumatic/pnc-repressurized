package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class TileEntityAssemblyPlatform extends TileEntityBase implements IAssemblyMachine, IResettable {
    @DescSynced
    private boolean shouldClawClose;
    @DescSynced
    @LazySynced
    public float clawProgress;
    public float oldClawProgress;
    @DescSynced
    private ItemStackHandler inventory = new ItemStackHandler(1);
    private float speed = 1.0F;
    public boolean hasDrilledStack;
    public boolean hasLaseredStack;

    @Override
    public void update() {
        super.update();
        oldClawProgress = clawProgress;
        if (!shouldClawClose && clawProgress > 0F) {
            clawProgress = Math.max(clawProgress - TileEntityConstants.ASSEMBLY_IO_UNIT_CLAW_SPEED * speed, 0);
        } else if (shouldClawClose && clawProgress < 1F) {
            clawProgress = Math.min(clawProgress + TileEntityConstants.ASSEMBLY_IO_UNIT_CLAW_SPEED * speed, 1);
        }
    }

    private boolean isClawDone() {
        return clawProgress == (shouldClawClose ? 1F : 0F);
    }

    @Override
    public boolean isIdle() {
        return !shouldClawClose && isClawDone() && getHeldStack().isEmpty();
    }

    @Override
    public boolean reset() {
        openClaw();
        return isIdle();
    }

    public boolean closeClaw() {
        hasDrilledStack = false;
        hasLaseredStack = false;
        shouldClawClose = true;
        return isClawDone();
    }

    public boolean openClaw() {
        shouldClawClose = false;
        return isClawDone();
    }

    @Nonnull
    public ItemStack getHeldStack() {
        return inventory.getStackInSlot(0);
    }

    public void setHeldStack(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            hasDrilledStack = false;
            hasLaseredStack = false;
        }
        inventory.setStackInSlot(0, stack);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("clawClosing", shouldClawClose);
        tag.setFloat("clawProgress", clawProgress);
        tag.setFloat("speed", speed);
        tag.setBoolean("drilled", hasDrilledStack);
        tag.setBoolean("lasered", hasLaseredStack);
        tag.setTag("Items", inventory.serializeNBT());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        shouldClawClose = tag.getBoolean("clawClosing");
        clawProgress = tag.getFloat("clawProgress");
        speed = tag.getFloat("speed");
        hasDrilledStack = tag.getBoolean("drilled");
        hasLaseredStack = tag.getBoolean("lasered");
        inventory = new ItemStackHandler(1);
        inventory.deserializeNBT(tag.getCompoundTag("Items"));
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
    }

}
