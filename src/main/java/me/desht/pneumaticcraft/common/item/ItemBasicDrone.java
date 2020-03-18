package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.entity.living.EntityBasicDrone;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public class ItemBasicDrone extends ItemDrone {
    private final BiFunction<World, PlayerEntity, EntityBasicDrone> droneCreator;
    
    public ItemBasicDrone(BiFunction<World, PlayerEntity, EntityBasicDrone> droneCreator) {
        this.droneCreator = droneCreator;
    }

    @Override
    public void spawnDrone(PlayerEntity player, World world, BlockPos clickPos, Direction facing, BlockPos placePos, ItemStack iStack) {
        EntityBasicDrone drone = droneCreator.apply(world, player);

        drone.setPosition(placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5);
        drone.initFromItemStack(iStack);
        world.addEntity(drone);

        drone.addProgram(clickPos, facing, placePos, iStack, drone.progWidgets);
        TileEntityProgrammer.updatePuzzleConnections(drone.progWidgets);

        drone.onInitialSpawn(world, world.getDifficultyForLocation(placePos), SpawnReason.TRIGGERED,  null, null);
    }

    @Override
    public boolean canProgram(ItemStack stack) {
        return false;
    }
}
