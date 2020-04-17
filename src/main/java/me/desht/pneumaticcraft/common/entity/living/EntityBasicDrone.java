package me.desht.pneumaticcraft.common.entity.living;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetStandby;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetStart;
import me.desht.pneumaticcraft.common.util.DroneProgramBuilder;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Base class for all pre-programmed (and not programmable) drones.
 * @author MineMaarten
 *
 */
abstract class EntityBasicDrone extends EntityDrone {

    EntityBasicDrone(EntityType<? extends EntityDrone> type, World world, PlayerEntity player) {
        super(type, world, player);
    }

    EntityBasicDrone(EntityType<? extends EntityDrone> type, World world) {
        super(type, world);
    }

//    @Override
//    protected ItemStack getDroppedStack() {
//        CompoundNBT tag = new CompoundNBT();
//        tag.putInt("color", getDroneColor());
//        CompoundNBT invTag = new CompoundNBT();
//        writeAdditional(invTag);
//        tag.put(UpgradableItemUtils.NBT_UPGRADE_TAG, invTag.get(UpgradableItemUtils.NBT_UPGRADE_TAG));
//        ItemStack drone = new ItemStack(getDroneItem());
//        drone.setTag(tag);
//        drone.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(h -> h.addAir(getAirHandler().getAir()));
//        return drone;
//    }
//
//    protected abstract Item getDroneItem();
    
//    public abstract boolean addProgram(BlockPos clickPos, Direction facing, BlockPos pos, ItemStack droneStack, List<IProgWidget> widgets);

    void maybeAddStandbyInstruction(DroneProgramBuilder builder, ItemStack droneStack) {
        if (UpgradableItemUtils.getUpgrades(droneStack, EnumUpgrade.STANDBY) > 0) {
            builder.add(new ProgWidgetStandby());
        }
    }

    void addBasicProgram(BlockPos pos, List<IProgWidget> widgets, IProgWidget mainProgram) {
        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        builder.add(mainProgram, standard16x16x16Area(pos));
        widgets.addAll(builder.build());
    }
    
    static ProgWidgetArea standard16x16x16Area(BlockPos centerPos){
        return ProgWidgetArea.fromPositions(centerPos.add(-16, -16, -16), centerPos.add(16, 16, 16));
    }
   
}
