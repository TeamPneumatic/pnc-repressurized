package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.handler.RenderedItemStackHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class TileEntityAssemblyPlatform extends TileEntityTickableBase implements IAssemblyMachine, IResettable {
    @DescSynced
    private boolean shouldClawClose;
    @DescSynced
    @LazySynced
    public float clawProgress;
    public float oldClawProgress;
    @DescSynced
    private final RenderedItemStackHandler itemHandler = new RenderedItemStackHandler(this);
    private final LazyOptional<IItemHandlerModifiable> inventoryCap = LazyOptional.of(() -> itemHandler);
    private float speed = 1.0F;
    private BlockPos controllerPos;

    public TileEntityAssemblyPlatform() {
        super(ModTileEntities.ASSEMBLY_PLATFORM);
    }

    @Override
    public void tick() {
        super.tick();
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
        return itemHandler.getStackInSlot(0);
    }

    public void setHeldStack(@Nonnull ItemStack stack) {
        itemHandler.setStackInSlot(0, stack);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putBoolean("clawClosing", shouldClawClose);
        tag.putFloat("clawProgress", clawProgress);
        tag.putFloat("speed", speed);
        tag.put("Items", itemHandler.serializeNBT());
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        shouldClawClose = tag.getBoolean("clawClosing");
        clawProgress = tag.getFloat("clawProgress");
        speed = tag.getFloat("speed");
        itemHandler.deserializeNBT(tag.getCompound("Items"));
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

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return itemHandler;
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
