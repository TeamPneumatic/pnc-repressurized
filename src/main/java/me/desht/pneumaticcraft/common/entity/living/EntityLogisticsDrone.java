package me.desht.pneumaticcraft.common.entity.living;

import me.desht.pneumaticcraft.common.core.ModEntityTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetLogistics;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;

import java.util.List;

public class EntityLogisticsDrone extends EntityBasicDrone {
    public static EntityLogisticsDrone create(EntityType<Entity> entityEntityType, World world) {
        return new EntityLogisticsDrone(world);
    }

    public static Entity createClient(FMLPlayMessages.SpawnEntity spawnEntity, World world) {
        return new EntityLogisticsDrone(world);
    }

    private EntityLogisticsDrone(World world) {
        super(ModEntityTypes.LOGISTIC_DRONE, world);
    }

    public EntityLogisticsDrone(World world, PlayerEntity player) {
        super(ModEntityTypes.LOGISTIC_DRONE, world, player);
    }

    @Override
    protected Item getDroneItem(){
        return ModItems.LOGISTIC_DRONE;
    }

    @Override
    public void addProgram(BlockPos clickPos, Direction facing, BlockPos pos, List<IProgWidget> widgets) {
        addBasicProgram(pos, widgets, new ProgWidgetLogistics());
    }
    
}
