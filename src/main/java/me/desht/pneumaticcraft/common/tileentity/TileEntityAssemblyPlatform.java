package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.inventory.handler.BaseItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.recipes.programs.AssemblyProgram;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class TileEntityAssemblyPlatform extends TileEntityTickableBase implements IAssemblyMachine, IResettable {
    @DescSynced
    private boolean shouldClawClose;
    @DescSynced
    @LazySynced
    public float clawProgress;
    public float oldClawProgress;
    @DescSynced
    private final ItemStackHandler inventory = new BaseItemStackHandler(this,1);
    private float speed = 1.0F;
    private BlockPos controllerPos;

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

    boolean closeClaw() {
        shouldClawClose = true;
        sendDescriptionPacket();
        return isClawDone();
    }

    boolean openClaw() {
        shouldClawClose = false;
        sendDescriptionPacket();
        return isClawDone();
    }

    @Nonnull
    public ItemStack getHeldStack() {
        return inventory.getStackInSlot(0);
    }

    public void setHeldStack(@Nonnull ItemStack stack) {
        inventory.setStackInSlot(0, stack);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("clawClosing", shouldClawClose);
        tag.setFloat("clawProgress", clawProgress);
        tag.setFloat("speed", speed);
        tag.setTag("Items", inventory.serializeNBT());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        shouldClawClose = tag.getBoolean("clawClosing");
        clawProgress = tag.getFloat("clawProgress");
        speed = tag.getFloat("speed");
        inventory.deserializeNBT(tag.getCompoundTag("Items"));
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public AssemblyProgram.EnumMachine getAssemblyType() {
        return AssemblyProgram.EnumMachine.PLATFORM;
    }

    @Override
    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        invalidateSystem();
    }

    private void invalidateSystem() {
        if (controllerPos != null) {
            TileEntity te = getWorld().getTileEntity(controllerPos);
            if (te instanceof TileEntityAssemblyController) {
                ((TileEntityAssemblyController) te).invalidateAssemblySystem();
            }
        }
    }
}
