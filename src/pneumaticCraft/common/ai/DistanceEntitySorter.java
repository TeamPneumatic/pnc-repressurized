package pneumaticCraft.common.ai;

import java.util.Comparator;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import pneumaticCraft.api.drone.IDrone;

public class DistanceEntitySorter implements Comparator{
    private final IDrone drone;

    public DistanceEntitySorter(IDrone drone){
        this.drone = drone;
    }

    public int compare(Entity entity1, Entity entity2){
        Vec3 vec = drone.getPosition();
        double d0 = vec.squareDistanceTo(entity1.posX, entity1.posY, entity1.posZ);
        double d1 = vec.squareDistanceTo(entity2.posX, entity2.posY, entity2.posZ);
        return d0 < d1 ? -1 : d0 > d1 ? 1 : 0;
    }

    @Override
    public int compare(Object p_compare_1_, Object p_compare_2_){
        return this.compare((Entity)p_compare_1_, (Entity)p_compare_2_);
    }
}
