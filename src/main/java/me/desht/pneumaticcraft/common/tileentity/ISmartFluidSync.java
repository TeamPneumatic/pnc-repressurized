package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.config.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidTank;

/**
 * Represents an object (typically a tile entity) which syncs its fluid tanks carefully to avoid excessive packets
 * to the client.
 */
public interface ISmartFluidSync {

    /**
     * Update the tank holder with a scaled fluid amount; scaled by a proportion of the tank's total capacity.  The
     * exact proportion is defined in config (D:liquidTankUpdateThreshold) and is 0.01 by default.  The
     * implementation would typically update a (non-lazy) @DescSynced field in the holder.
     *
     * @param tankIndex integer index of the tank (only useful when holder has multiple tanks to sync)
     * @param amount scaled fluid amount
     */
    void updateScaledFluidAmount(int tankIndex, int amount);

    class SmartSyncTank extends FluidTank {
        private final ISmartFluidSync holder;
        private final int tankIndex;
        private final double scaleValue;

        /**
         * Create a new smart synced tank.  Use this constructor when the holder object has multiple synced tanks;
         * pass a different tankIndex for each tank.
         *
         * @param holder the tank holder object, to notify of scaled fluid changes
         * @param capacity the tank's capacity
         * @param tankIndex tank index, will be passed to {@link ISmartFluidSync#updateScaledFluidAmount(int, int)}
         */
        SmartSyncTank(ISmartFluidSync holder, int capacity, int tankIndex) {
            super(capacity);
            this.holder = holder;
            this.tankIndex = tankIndex;
            this.scaleValue = getCapacity() * Config.Common.Advanced.liquidTankUpdateThreshold;
        }

        /**
         * Create a new smart synced tank.  Use this constructor when there is only one tank in the holder object.  In
         * this case the tankIndex passed to {@link ISmartFluidSync#updateScaledFluidAmount(int, int)} is always 1.
         *
         * @param holder the tank holder object, to notify of scaled fluid changes
         * @param capacity the tank's capacity
         */
        SmartSyncTank(ISmartFluidSync holder, int capacity) {
            this(holder, capacity, 1);
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            holder.updateScaledFluidAmount(tankIndex, getScaledFluidAmount());
            if (holder instanceof TileEntity) {
                ((TileEntity) holder).markDirty();
            }
        }

        int getScaledFluidAmount() {
            return (int) (getFluidAmount() / scaleValue);
        }
    }
}
