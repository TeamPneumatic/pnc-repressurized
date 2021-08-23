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
