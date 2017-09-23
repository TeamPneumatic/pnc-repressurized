package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.SpecialVariableRetrievalEvent;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DroneSpecialVariableHandler {

    @SubscribeEvent
    public void onSpecialVariableRetrieving(SpecialVariableRetrievalEvent.CoordinateVariable.Drone event) {
        if (event.specialVarName.equalsIgnoreCase("owner")) {
            EntityDrone drone = (EntityDrone) event.drone;
            EntityPlayer player = drone.getOwner();
            if (player != null) event.coordinate = getPosForEntity(player);
        } else if (event.specialVarName.equalsIgnoreCase("drone")) {
            event.coordinate = getPosForEntity(event.drone);
        } else if (event.specialVarName.toLowerCase().startsWith("player=")) {
            EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(event.specialVarName.substring("player=".length()));
            if (player != null) event.coordinate = getPosForEntity(player);
        }
    }

    private BlockPos getPosForEntity(Entity entity) {
        return new BlockPos(entity).offset(EnumFacing.UP); //TODO 1.8 check what's with the offset
    }

    private BlockPos getPosForEntity(IDrone entity) {
        return new BlockPos(entity.getDronePos()).offset(EnumFacing.UP);//TODO 1.8 check what's with the offset
    }
}
