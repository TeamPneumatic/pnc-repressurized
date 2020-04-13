package me.desht.pneumaticcraft.common.entity.living;

import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetLogistics;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetStart;
import me.desht.pneumaticcraft.common.util.DroneProgramBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class EntityLogisticsDrone extends EntityBasicDrone {
    public EntityLogisticsDrone(EntityType<EntityLogisticsDrone> type, World world) {
        super(type, world);
    }

    public EntityLogisticsDrone(World world, PlayerEntity player) {
        super(ModEntities.LOGISTICS_DRONE.get(), world, player);
    }

    @Override
    public boolean addProgram(BlockPos clickPos, Direction facing, BlockPos pos, ItemStack droneStack, List<IProgWidget> widgets) {
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        builder.add(new ProgWidgetLogistics(), standard16x16x16Area(pos));
        maybeAddStandbyInstruction(builder, droneStack);
        widgets.addAll(builder.build());
//        addBasicProgram(pos, widgets, new ProgWidgetLogistics());

        return true;
    }
}
