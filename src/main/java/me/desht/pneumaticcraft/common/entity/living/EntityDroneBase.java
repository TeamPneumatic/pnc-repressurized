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

package me.desht.pneumaticcraft.common.entity.living;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public abstract class EntityDroneBase extends PathfinderMob {
    public float oldPropRotation;
    public float propRotation;
    public float laserExtension; // How far the laser comes out of the drone. 1F is fully extended
    public float oldLaserExtension;

    public EntityDroneBase(EntityType<? extends PathfinderMob> type, Level world) {
        super(type, world);
    }

    public double getLaserOffsetY() {
        return 0.05;
    }

    public int getLaserColor() {
        return 0xFFFF0000;
    }

    public int getDroneColor() {
        return DyeColor.BLACK.getId();
    }

    public boolean isAccelerating() {
        return true;
    }

    public abstract BlockPos getDugBlock();

    @Nonnull
    public abstract ItemStack getDroneHeldItem();

    public abstract  BlockPos getTargetedBlock();

    public abstract Component getOwnerName();

    public abstract String getLabel();

    public abstract boolean isTeleportRangeLimited();
}
