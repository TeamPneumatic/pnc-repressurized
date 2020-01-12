package me.desht.pneumaticcraft.common.entity.living;

import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetLogistics;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class EntityLogisticsDrone extends EntityBasicDrone {
    public static EntityLogisticsDrone createLogisticsDrone(EntityType<EntityLogisticsDrone> type, World world) {
        return new EntityLogisticsDrone(type, world);
    }

    private EntityLogisticsDrone(EntityType<EntityLogisticsDrone> type, World world) {
        super(type, world);
    }

    public EntityLogisticsDrone(World world, PlayerEntity player) {
        super(ModEntities.LOGISTICS_DRONE.get(), world, player);
    }

    @Override
    protected Item getDroneItem(){
        return ModItems.LOGISTICS_DRONE.get();
    }

    @Override
    public void addProgram(BlockPos clickPos, Direction facing, BlockPos pos, List<IProgWidget> widgets) {
        addBasicProgram(pos, widgets, new ProgWidgetLogistics());
    }
    
}
