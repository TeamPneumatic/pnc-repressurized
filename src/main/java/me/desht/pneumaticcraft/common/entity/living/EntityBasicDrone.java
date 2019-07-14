package me.desht.pneumaticcraft.common.entity.living;

import me.desht.pneumaticcraft.common.inventory.handler.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetStart;
import me.desht.pneumaticcraft.common.util.DroneProgramBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Base class for all pre-programmed (and not programmable) drones.
 * @author MineMaarten
 *
 */
public abstract class EntityBasicDrone extends EntityDrone {

    EntityBasicDrone(EntityType<? extends EntityDrone> type, World world, PlayerEntity player) {
        super(type, world, player);
    }

    EntityBasicDrone(EntityType<? extends EntityDrone> type, World world) {
        super(type, world);
    }

    @Override
    protected ItemStack getDroppedStack() {
        CompoundNBT tag = new CompoundNBT();
        tag.putFloat("currentAir", currentAir);
        tag.putInt("color", getDroneColor());
        CompoundNBT invTag = new CompoundNBT();
        writeAdditional(invTag);
        tag.put(ChargeableItemHandler.NBT_UPGRADE_TAG, invTag.get(ChargeableItemHandler.NBT_UPGRADE_TAG));
        ItemStack drone = new ItemStack(getDroneItem());
        drone.setTag(tag);
        return drone;
    }
    
    protected abstract Item getDroneItem();
    
    public abstract void addProgram(BlockPos clickPos, Direction facing, BlockPos pos, List<IProgWidget> widgets);
    
    void addBasicProgram(BlockPos pos, List<IProgWidget> widgets, IProgWidget mainProgram) {
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        builder.add(mainProgram, standard16x16x16Area(pos));
        widgets.addAll(builder.build());
    }
    
    private static ProgWidgetArea standard16x16x16Area(BlockPos centerPos){
        return ProgWidgetArea.fromPositions(centerPos.add(-16, -16, -16), centerPos.add(16, 16, 16));
    }
   
}
