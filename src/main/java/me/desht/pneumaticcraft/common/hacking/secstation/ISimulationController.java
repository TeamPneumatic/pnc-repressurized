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

package me.desht.pneumaticcraft.common.hacking.secstation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public interface ISimulationController {
    /**
     * A connection from one node to another has just begun.
     *
     * @param hackSimulation the simulation
     * @param fromPos position of the hacking node
     * @param toPos position of the target node
     * @param initialProgress initial progress of the connection (usually but not necessarily 0)
     */
    void onConnectionStarted(HackSimulation hackSimulation, int fromPos, int toPos, float initialProgress);

    /**
     * A simulation has just had a node hacked (connection completed).
     *
     * @param hackSimulation the simulation
     * @param pos position of the newly hacked node
     */
    void onNodeHacked(HackSimulation hackSimulation, int pos);

    /**
     * A simulation has just had a hacked node fortified by the player
     *
     * @param hackSimulation the simulation
     * @param pos position of the newly fortified node
     */
    void onNodeFortified(HackSimulation hackSimulation, int pos);

    /**
     * Called every game tick (both client and server) by the owning security station block entity.
     */
    void tick();

    /**
     * Are we done with this hack simulation?
     *
     * @return true if finished, false otherwise
     */
    boolean isSimulationDone();

    /**
     * Get the simulation object for the given side (player or AI)
     *
     * @param side the side to get
     * @return the simulation
     */
    HackSimulation getSimulation(HackingSide side);

    /**
     * Get the player currently trying to hack the security station. This does not validate if the player is still
     * online.
     *
     * @return the player
     */
    Player getHacker();

    /**
     * Is this just a test by the station's owner, or other whitelisted player? If just testing, the security
     * station will not retaliate on a failed hack.
     *
     * @return true if just testing, false otherwise
     */
    boolean isJustTesting();

    /**
     * Serialize the simulation controller for sync to client on open of hacking GUI
     *
     * @param buf packet buffer
     */
    void toBytes(RegistryFriendlyByteBuf buf);

    enum HackingSide {
        PLAYER,
        AI;

        public HackingSide other() {
            return this == PLAYER ? AI : PLAYER;
        }
    }
}
