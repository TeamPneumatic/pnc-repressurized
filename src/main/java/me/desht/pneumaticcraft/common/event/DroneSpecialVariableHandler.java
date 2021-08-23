package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.api.drone.SpecialVariableRetrievalEvent.CoordinateVariable.Drone;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class DroneSpecialVariableHandler {
    private static final Map<String, BiFunction<Drone, String, BlockPos>> DISPATCH_MAP = new HashMap<>();
    private static final BiFunction<Drone, String, BlockPos> MISSING = (event, extraParam) -> BlockPos.ZERO;

    static {
        // new generation of special vars, all end in _pos; recommended to use these in new programs
        DISPATCH_MAP.put("drone_pos", (event, extra) -> new BlockPos(event.drone.getDronePos()));
        DISPATCH_MAP.put("controller_pos", (event, extra) -> event.drone.getControllerPos());
        DISPATCH_MAP.put("owner_pos", (event, extra) -> getPosForPlayer(event.drone.getOwner()));
        DISPATCH_MAP.put("player_pos", (event, extra) -> getPosForPlayer(PneumaticCraftUtils.getPlayerFromName(extra)));
        DISPATCH_MAP.put("owner_look", (event, extra) -> getPlayerLookVec(event.drone.getOwner()));

        // old generation - they will stay around for backwards compatibility, but prefer to use the new vars above
        DISPATCH_MAP.put("owner", (event, extra) -> getPosForPlayer(event.drone.getOwner()));
        DISPATCH_MAP.put("player", (event, extra) -> getPosForPlayer(PneumaticCraftUtils.getPlayerFromName(extra)));
        // this method gets the block above the drone's position for historical reasons
        // https://github.com/TeamPneumatic/pnc-repressurized/issues/601 for more discussion
        DISPATCH_MAP.put("drone", (event, extra) -> new BlockPos(event.drone.getDronePos()).relative(Direction.UP));
    }

    @SubscribeEvent
    public void onSpecialVariableRetrieving(Drone event) {
        String[] s = event.specialVarName.split("=", 2);
        String extra = s.length > 1 ? s[1] : "";
        event.setCoordinate(DISPATCH_MAP.getOrDefault(s[0], MISSING).apply(event, extra));
    }

    private static BlockPos getPosForPlayer(PlayerEntity player) {
        // offset UP because "$owner" and "$player" get the player's head position, not feet position
        return player == null ? BlockPos.ZERO : player.blockPosition().relative(Direction.UP);
    }

    private static BlockPos getPlayerLookVec(PlayerEntity player) {
        if (player == null) return BlockPos.ZERO;

        Direction d = player.getDirection();
        float pitch = player.getViewXRot(0f);
        int yDir = Math.abs(pitch) < 45 ? 0 : (int) Math.signum(-pitch);
        return new BlockPos(d.getStepX(), yDir, d.getStepZ());
    }
}
