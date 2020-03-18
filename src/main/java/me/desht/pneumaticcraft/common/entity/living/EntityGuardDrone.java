package me.desht.pneumaticcraft.common.entity.living;

import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.util.DroneProgramBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class EntityGuardDrone extends EntityBasicDrone {
    public EntityGuardDrone(EntityType<? extends EntityDrone> type, World world) {
        super(type, world);
    }

    public EntityGuardDrone(World world, PlayerEntity player) {
        super(ModEntities.GUARD_DRONE.get(), world, player);
    }

    @Override
    protected Item getDroneItem() {
        return ModItems.GUARD_DRONE.get();
    }

    @Override
    public void addProgram(BlockPos clickPos, Direction facing, BlockPos pos, ItemStack droneStack, List<IProgWidget> widgets) {
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        // no item filter because we don't know what type of sword or ammo could be in the inventory
        builder.add(new ProgWidgetInventoryImport(), ProgWidgetArea.fromPosition(clickPos));
        builder.add(new ProgWidgetEntityAttack(), standard16x16x16Area(clickPos), ProgWidgetText.withText("@mob"));
        maybeAddStandbyInstruction(builder, droneStack);
        builder.add(new ProgWidgetWait(), ProgWidgetText.withText("10"));
        widgets.addAll(builder.build());
    }
}
