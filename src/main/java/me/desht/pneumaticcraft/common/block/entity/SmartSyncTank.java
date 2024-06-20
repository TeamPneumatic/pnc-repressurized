/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.lang.ref.WeakReference;

/**
 * A fluid tank which smartly syncs its fluid and amount to clients to avoid performance problems due to excessive
 * packet sending.  Also marks its owning BE as dirty when changed.
 */
public class SmartSyncTank extends PNCFluidTank {
    @DescSynced
    private FluidStack syncedFluidStackDesc = FluidStack.EMPTY;
    @GuiSynced
    private FluidStack syncedFluidStackGui = FluidStack.EMPTY;

    private boolean pending = false;

    private int syncTimer = 0;  // will cause immediate sync to client on initial placement
    private final WeakReference<BlockEntity> owner;
    private final int threshold;

    public SmartSyncTank(BlockEntity owner, int capacity) {
        super(capacity);

        this.owner = new WeakReference<>(owner);
        this.threshold = Math.min(1000, capacity / 100);
    }

    /**
     * Call from the holding BE's tick() method on both client and server
     */
    public void tick() {
        BlockEntity te = owner.get();
        if (te != null) {
            if (te.getLevel().isClientSide) {
                if (ClientUtils.isGuiOpen(te)) {
                    super.setFluid(syncedFluidStackGui);
                } else {
                    int currAmount = getFluidAmount();
                    FluidStack tgt = syncedFluidStackDesc;
                    int delta = tgt.getAmount() - currAmount;
                    if (delta != 0) {
                        int newAmount = Math.abs(delta) < capacity / 200 ? tgt.getAmount() : currAmount + delta / 20;
                        Fluid newFluid = fluidStack.isEmpty() ? tgt.getFluid() : fluidStack.getFluid();
                        super.setFluid(new FluidStack(newFluid, newAmount));
                    }
                }
            } else {
                if (syncTimer > 0) {
                    syncTimer--;
                } else if (syncTimer == 0) {
                    if (pending) {
                        syncedFluidStackDesc = getFluid().copy();
                        pending = false;
                        syncTimer = ConfigHelper.common().advanced.fluidTankUpdateRate.get();
                    } else {
                        syncTimer = -1;
                    }
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
            deferredSync(ConfigHelper.common().advanced.fluidTankUpdateRate.get());
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

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack drained = super.drain(resource, action);
        if (!drained.isEmpty() && action.execute()) {
            onFluidChange(getFluid());
        }
        return drained;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack drained = super.drain(maxDrain, action);
        if (!drained.isEmpty() && action.execute()) {
            onFluidChange(getFluid());
        }
        return drained;
    }

    @Override
    protected void onContentsChanged(Fluid prevFluid, int prevAmount) {
        BlockEntity be = owner.get();
        if (be != null && be.getLevel() != null && !be.getLevel().isClientSide()) {
            be.setChanged();
        }
    }

    @Override
    public void setFluid(FluidStack stack) {
        onFluidChange(stack);

        super.setFluid(stack);
    }
}
