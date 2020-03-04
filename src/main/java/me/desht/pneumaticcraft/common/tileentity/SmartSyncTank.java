package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;

/**
 * A fluid tank which smartly syncs its fluid and amount to clients to avoid performance problems due to excessive
 * packet sending.  Also marks its owning TE as dirty when changed.
 */
public class SmartSyncTank extends FluidTank {
    @DescSynced
    private FluidStack syncedFluidStackDesc = FluidStack.EMPTY;
    @GuiSynced
    private FluidStack syncedFluidStackGui = FluidStack.EMPTY;

    private boolean pending = false;

    private int syncTimer = -1;
    private final TileEntity owner;
    private final int threshold;

    SmartSyncTank(TileEntity owner, int capacity) {
        super(capacity);

        this.owner = owner;
        this.threshold = Math.min(1000, capacity / 100);
    }

    /**
     * Call from the holding TE's tick() method on both client and server
     */
    public void tick() {
        if (owner.getWorld().isRemote) {
            super.setFluid(ClientUtils.isGuiOpen() ? syncedFluidStackGui : syncedFluidStackDesc);
        } else {
            if (syncTimer > 0) {
                syncTimer--;
            } else if (syncTimer == 0) {
                if (pending) {
                    syncedFluidStackDesc = getFluid().copy();
                    pending = false;
                    syncTimer = PNCConfig.Common.Advanced.fluidTankUpdateRate;
                } else {
                    syncTimer = -1;
                }
            }
        }
    }

    private void deferredSync(int ticks) {
        if (syncTimer == -1) {
            syncedFluidStackDesc = getFluid().copy();
            syncTimer = ticks;
        } else {
            pending = true;
        }
    }

    private void onFluidChange(FluidStack newFluid) {
        // we can update the gui-synced value every tick
        syncedFluidStackGui = newFluid.copy();

        // more careful checking for the desc-synced value
        int delta = Math.abs(syncedFluidStackDesc.getAmount() - newFluid.getAmount());
        if (delta >= threshold || syncedFluidStackDesc.getFluid() != newFluid.getFluid()) {
            deferredSync(PNCConfig.Common.Advanced.fluidTankUpdateRate);
        }
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int filled = super.fill(resource, action);
        if (filled != 0 && action.execute()) {
            onFluidChange(getFluid());
        }
        return filled;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack drained = super.drain(resource, action);
        if (!drained.isEmpty() && action.execute()) {
            onFluidChange(getFluid());
        }
        return drained;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack drained = super.drain(maxDrain, action);
        if (!drained.isEmpty() && action.execute()) {
            onFluidChange(getFluid());
        }
        return drained;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();

        // We don't use onContentsChanged() for sync purposes, because its gets called even for simulated changes,
        // and we have no way of knowing whether or not this is a simulation.

        owner.markDirty();
    }

    @Override
    public void setFluid(FluidStack stack) {
        onFluidChange(stack);

        super.setFluid(stack);
    }

    @Override
    public FluidTank readFromNBT(CompoundNBT nbt) {
        FluidTank tank = super.readFromNBT(nbt);
        syncedFluidStackGui = tank.getFluid().copy();
        syncedFluidStackDesc = tank.getFluid().copy();
        return tank;
    }
}
