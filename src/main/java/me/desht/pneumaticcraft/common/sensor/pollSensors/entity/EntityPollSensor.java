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

package me.desht.pneumaticcraft.common.sensor.pollSensors.entity;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

abstract class EntityPollSensor implements IPollSensorSetting {

    @Override
    public Set<EnumUpgrade> getRequiredUpgrades() {
        return ImmutableSet.of(EnumUpgrade.ENTITY_TRACKER);
    }

    @Override
    public int getPollFrequency(TileEntity te) {
        return 1;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        AxisAlignedBB aabb = new AxisAlignedBB(pos.offset(-sensorRange, -sensorRange, -sensorRange), pos.offset(1 + sensorRange, 1 + sensorRange, 1 + sensorRange));
        return getRedstoneValue(world.getEntitiesOfClass(getEntityTracked(), aabb), textBoxText);
    }

    protected abstract Class<? extends Entity> getEntityTracked();

    protected abstract int getRedstoneValue(List<Entity> entities, String textBoxText);

}
