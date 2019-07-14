package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.SpecialVariableRetrievalEvent;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DroneSpecialVariableHandler {

    @SubscribeEvent
    public void onSpecialVariableRetrieving(SpecialVariableRetrievalEvent.CoordinateVariable.Drone event) {
        if (event.specialVarName.equalsIgnoreCase("owner")) {
            PlayerEntity player = event.drone.getOwner();
            if (player != null) event.coordinate = getPosForEntity(player);
        } else if (event.specialVarName.equalsIgnoreCase("drone")) {
            event.coordinate = getPosForEntity(event.drone);
        } else if (event.specialVarName.toLowerCase().startsWith("player=")) {
            PlayerEntity player = PneumaticCraftUtils.getPlayerFromName(event.specialVarName.substring("player=".length()));
            if (player != null) event.coordinate = getPosForEntity(player);
        }
    }

    private BlockPos getPosForEntity(Entity entity) {
        return new BlockPos(entity).offset(Direction.UP); //TODO 1.8 check what's with the offset
    }

    private BlockPos getPosForEntity(IDrone entity) {
        return new BlockPos(entity.getDronePos()).offset(Direction.UP);//TODO 1.8 check what's with the offset
    }
}
