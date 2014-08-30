package pneumaticCraft.common.ai;

import java.util.Comparator;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class DistanceTileEntitySorter implements Comparator{

    private final Entity entity;

    public DistanceTileEntitySorter(Entity entity){
        this.entity = entity;
    }

    @Override
    public int compare(Object arg0, Object arg1){
        TileEntity c1 = (TileEntity)arg0;
        TileEntity c2 = (TileEntity)arg0;
        return Double.compare(PneumaticCraftUtils.distBetween(c1.xCoord, c1.yCoord, c1.zCoord, entity.posX, entity.posY, entity.posZ), PneumaticCraftUtils.distBetween(c2.xCoord, c2.yCoord, c2.zCoord, entity.posX, entity.posY, entity.posZ));
    }

}
