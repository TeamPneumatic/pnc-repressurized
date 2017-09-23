package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import java.util.Comparator;

public class DistanceTileEntitySorter implements Comparator {

    private final Entity entity;

    public DistanceTileEntitySorter(Entity entity) {
        this.entity = entity;
    }

    @Override
    public int compare(Object arg0, Object arg1) {
        TileEntity c1 = (TileEntity) arg0;
        TileEntity c2 = (TileEntity) arg0;
        return Double.compare(
                PneumaticCraftUtils.distBetweenSq(c1.getPos().getX(), c1.getPos().getY(), c1.getPos().getZ(), entity.posX, entity.posY, entity.posZ),
                PneumaticCraftUtils.distBetweenSq(c2.getPos().getX(), c2.getPos().getY(), c2.getPos().getZ(), entity.posX, entity.posY, entity.posZ));
    }

}
