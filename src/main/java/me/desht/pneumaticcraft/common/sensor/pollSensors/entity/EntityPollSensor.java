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
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Set;

abstract class EntityPollSensor implements IPollSensorSetting {

    @Override
    public Set<PNCUpgrade> getRequiredUpgrades() {
        return ImmutableSet.of(ModUpgrades.ENTITY_TRACKER.get());
    }

    @Override
    public int getPollFrequency(BlockEntity te) {
        return 1;
    }

    @Override
    public int getRedstoneValue(Level level, BlockPos pos, int sensorRange, String textBoxText) {
        AABB aabb = new AABB(pos.offset(-sensorRange, -sensorRange, -sensorRange), pos.offset(1 + sensorRange, 1 + sensorRange, 1 + sensorRange));
        return getRedstoneValue(level.getEntities(EntityTypeTest.forClass(getEntityTracked()), aabb, Entity::isAlive), textBoxText);
    }

    protected abstract Class<? extends Entity> getEntityTracked();

    protected abstract int getRedstoneValue(List<? extends Entity> entities, String textBoxText);

}
