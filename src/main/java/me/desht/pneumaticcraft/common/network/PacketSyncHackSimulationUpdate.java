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

package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController.HackingSide;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 *
 * Sent by the server periodically while a Security Station hack is in progress, and when either side has just won,
 * to keep the client up-to-date.  Client also runs the simulation (dead reckoning), but needs to be updated by the
 * server every so often to keep the display accurate.
 */
public class PacketSyncHackSimulationUpdate extends LocationIntPacket {
    private final List<HackSimulation.ConnectionEntry> playerConns;
    private final List<HackSimulation.ConnectionEntry> aiConns;
    private final List<Pair<Integer, Integer>> fortification;
    private final boolean aiAwake;
    private final boolean aiStopWormed;
    private final boolean aiWon;
    private final boolean playerWon;

    public PacketSyncHackSimulationUpdate(TileEntitySecurityStation te) {
        super(te.getBlockPos());

        HackSimulation aiSim = te.getSimulationController().getSimulation(HackingSide.AI);
        HackSimulation playerSim = te.getSimulationController().getSimulation(HackingSide.PLAYER);

        playerConns = te.getSimulationController().getSimulation(HackingSide.PLAYER).allConnections;
        aiConns = aiSim.allConnections;
        fortification = new ArrayList<>();
        for (int i = 0; i < HackSimulation.GRID_SIZE; i++) {
            if (aiSim.getNodeAt(i) != null && aiSim.getNodeAt(i).getFortification() > 0) {
                fortification.add(Pair.of(i, aiSim.getNodeAt(i).getFortification()));
            }
        }
        aiAwake = aiSim.isAwake();
        aiStopWormed = aiSim.isStopWormed();
        aiWon = aiSim.isHackComplete();
        playerWon = playerSim.isHackComplete();
    }

    public PacketSyncHackSimulationUpdate(FriendlyByteBuf buffer) {
        super(buffer);

        playerConns = new ArrayList<>();
        int np = buffer.readVarInt();
        for (int i = 0; i < np; i++) {
            playerConns.add(HackSimulation.ConnectionEntry.readFromNetwork(buffer));
        }
        aiConns = new ArrayList<>();
        int na = buffer.readVarInt();
        for (int i = 0; i < na; i++) {
            aiConns.add(HackSimulation.ConnectionEntry.readFromNetwork(buffer));
        }
        fortification = new ArrayList<>();
        int nf = buffer.readVarInt();
        for (int i = 0; i < nf; i++) {
            fortification.add(Pair.of(buffer.readVarInt(), buffer.readVarInt()));
        }
        aiAwake = buffer.readBoolean();
        aiStopWormed = buffer.readBoolean();
        aiWon = buffer.readBoolean();
        playerWon = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        super.toBytes(buffer);

        buffer.writeVarInt(playerConns.size());
        playerConns.forEach(conn -> conn.write(buffer));
        buffer.writeVarInt(aiConns.size());
        aiConns.forEach(conn -> conn.write(buffer));
        buffer.writeVarInt(fortification.size());
        fortification.forEach(pair -> {
            buffer.writeVarInt(pair.getLeft());
            buffer.writeVarInt(pair.getRight());
        });
        buffer.writeBoolean(aiAwake);
        buffer.writeBoolean(aiStopWormed);
        buffer.writeBoolean(aiWon);
        buffer.writeBoolean(playerWon);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            BlockEntity te = ClientUtils.getClientTE(pos);
            if (te instanceof TileEntitySecurityStation) {
                ISimulationController controller = ((TileEntitySecurityStation) te).getSimulationController();
                if (controller != null) {
                    HackSimulation aiSim = controller.getSimulation(HackingSide.AI);
                    HackSimulation playerSim = controller.getSimulation(HackingSide.PLAYER);
                    playerSim.syncFromServer(playerConns);
                    aiSim.syncFromServer(aiConns);
                    aiSim.updateFortification(fortification);
                    if (aiAwake) aiSim.wakeUp();
                    aiSim.applyStopWorm(aiStopWormed ? 100 : 0);
                    if (aiWon) aiSim.setHackComplete();
                    if (playerWon) playerSim.setHackComplete();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
