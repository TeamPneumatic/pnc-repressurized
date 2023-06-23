/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.drone;

import me.desht.pneumaticcraft.api.drone.SpecialVariableRetrievalEvent.CoordinateVariable.Drone;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class DroneSpecialVariableHandler {
    private static final Map<String, BiFunction<Drone, String, BlockPos>> DISPATCH_MAP = new HashMap<>();
    private static final BiFunction<Drone, String, BlockPos> MISSING = (event, extraParam) -> BlockPos.ZERO;

    static {
        // new generation of special vars, all end in _pos; recommended to use these in new programs
        register("drone_pos", (event, extra) -> BlockPos.containing(event.drone.getDronePos()));
        register("controller_pos", (event, extra) -> event.drone.getControllerPos());
        register("owner_pos", (event, extra) -> getPosForPlayer(event.drone.getOwner()));
        register("player_pos", (event, extra) -> getPosForOtherPlayer(event.drone.getOwner(), PneumaticCraftUtils.getPlayerFromName(extra)));
        register("deploy_pos", (event, extra) -> event.drone.getDeployPos());
        register("owner_look", (event, extra) -> getPlayerLookVec(event.drone.getOwner()));

        // old generation - they will stay around for backwards compatibility, but prefer to use the new vars above
        register("owner", (event, extra) -> getPosForPlayer(event.drone.getOwner()));
        register("player", (event, extra) -> getPosForOtherPlayer(event.drone.getOwner(), PneumaticCraftUtils.getPlayerFromName(extra)));
        // this method gets the block above the drone's position for historical reasons
        // https://github.com/TeamPneumatic/pnc-repressurized/issues/601 for more discussion
        register("drone", (event, extra) -> BlockPos.containing(event.drone.getDronePos()).relative(Direction.UP));
    }

    private static void register(String var, BiFunction<Drone, String, BlockPos> func) {
        if (DISPATCH_MAP.containsKey(var)) {
            Log.warning("special variable '%s' is already registered! ignoring");
        } else {
            DISPATCH_MAP.put(var, func);
        }
    }

    private static BlockPos getPosForPlayer(Player player) {
        // offset UP because "$owner" and "$player" get the player's head position, not feet position
        return player == null ? BlockPos.ZERO : player.blockPosition().relative(Direction.UP);
    }

    private static BlockPos getPosForOtherPlayer(Player droneOwner, Player otherPlayer) {
        if (!ConfigHelper.common().drones.allowAnyPlayerVarQuery.get() && droneOwner != otherPlayer) {
            return BlockPos.ZERO;
        } else {
            return getPosForPlayer(otherPlayer);
        }
    }

    private static BlockPos getPlayerLookVec(Player player) {
        if (player == null) return BlockPos.ZERO;

        Direction d = player.getDirection();
        float pitch = player.getViewXRot(0f);
        int yDir = Math.abs(pitch) < 45 ? 0 : (int) Math.signum(-pitch);
        return new BlockPos(d.getStepX(), yDir, d.getStepZ());
    }

    @SubscribeEvent
    public void onSpecialVariableRetrieving(Drone event) {
        String[] s = event.specialVarName.split("=", 2);
        String extra = s.length > 1 ? s[1] : "";
        event.setCoordinate(DISPATCH_MAP.getOrDefault(s[0], MISSING).apply(event, extra));
    }
}
