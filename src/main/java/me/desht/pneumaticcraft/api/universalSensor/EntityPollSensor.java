package me.desht.pneumaticcraft.api.universalSensor;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class EntityPollSensor implements IPollSensorSetting {

    @Override
    public Set<Item> getRequiredUpgrades() {
        Set<Item> upgrades = new HashSet<Item>();
        upgrades.add(Itemss.upgrades.get(EnumUpgrade.ENTITY_TRACKER));
        return upgrades;
    }

    @Override
    public int getPollFrequency(TileEntity te) {
        return 1;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        AxisAlignedBB aabb = new AxisAlignedBB(pos.add(-sensorRange, -sensorRange, -sensorRange), pos.add(1 + sensorRange, 1 + sensorRange, 1 + sensorRange));
        return getRedstoneValue(world.getEntitiesWithinAABB(getEntityTracked(), aabb), textBoxText);
    }

    public abstract Class getEntityTracked();

    public abstract int getRedstoneValue(List<Entity> entities, String textBoxText);

}
