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

import me.desht.pneumaticcraft.common.block.entity.SecurityStationBlockEntity;
import me.desht.pneumaticcraft.common.inventory.SecurityStationHackingMenu;
import me.desht.pneumaticcraft.common.item.NetworkComponentItem;
import me.desht.pneumaticcraft.common.item.NetworkComponentItem.NetworkComponentType;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncHackSimulationUpdate;
import me.desht.pneumaticcraft.lib.BlockEntityConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Runs both the player and AI simulation objects and mediates between them.
 *
 * This is done on both client and server side.  Both sides will be ticked but the server will periodically sync
 * its state to the client, so client is using dead reckoning with periodic updates from the server.  Only the
 * server state actually matters for security station hacking, of course; the client state is purely for display
 * by SecurityStationHackingScreen.
 */
public class SimulationController implements ISimulationController {
    private final SecurityStationBlockEntity te;
    private final Player hacker;
    private final HackSimulation playerSimulation;
    private final HackSimulation aiSimulation;
    private final boolean justTesting;

    /**
     * Called server-side when a hack is started
     *
     * @param te the security station
     * @param hacker the hacking player
     */
    public SimulationController(SecurityStationBlockEntity te, Player hacker, boolean justTesting) {
        this.te = te;
        this.hacker = hacker;

        this.playerSimulation = new HackSimulation(this, te.findComponent(NetworkComponentType.NETWORK_IO_PORT),
                BlockEntityConstants.NETWORK_NORMAL_BRIDGE_SPEED, HackingSide.PLAYER);
        this.aiSimulation = new HackSimulation(this, te.findComponent(NetworkComponentType.DIAGNOSTIC_SUBROUTINE),
                BlockEntityConstants.NETWORK_AI_BRIDGE_SPEED, HackingSide.AI);

        for (int i = 0; i < te.getItemHandler().getSlots(); i++) {
            this.playerSimulation.addNode(i, te.getItemHandler().getStackInSlot(i));
            this.aiSimulation.addNode(i, te.getItemHandler().getStackInSlot(i));
        }

        this.justTesting = justTesting;
    }

    /**
     * Called client-side on start of hack to sync the initial simulation state.
     *
     * @param te the security station
     * @param hacker the hacking player (will be the client player)
     * @param playerSimulation the hacking player's simulation object
     * @param aiSimulation the security station's simulation object
     */
    public SimulationController(SecurityStationBlockEntity te, Player hacker, HackSimulation playerSimulation, HackSimulation aiSimulation, boolean justTesting) {
        this.te = te;
        this.hacker = hacker;
        this.playerSimulation = playerSimulation.setController(this);
        this.aiSimulation = aiSimulation.setController(this);
        this.justTesting = justTesting;
    }

    @Override
    public void onConnectionStarted(HackSimulation hackSimulation, int fromPos, int toPos, float initialProgress) {
    }

    @Override
    public boolean isJustTesting() {
        return justTesting;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(te.getBlockPos());

        playerSimulation.writeToNetwork(buffer);
        aiSimulation.writeToNetwork(buffer);

        List<Pair<Integer, ItemStack>> nodes = new ArrayList<>();
        for (int i = 0; i < te.getItemHandler().getSlots(); i++) {
            ItemStack stack = te.getItemHandler().getStackInSlot(i);
            if (!stack.isEmpty()) {
                nodes.add(Pair.of(i, stack));
            }
        }
        buffer.writeVarInt(nodes.size());
        nodes.forEach(pair -> {
            buffer.writeVarInt(pair.getLeft());
            buffer.writeItem(pair.getRight());
        });

        buffer.writeBoolean(justTesting);
    }


    @Override
    public void onNodeHacked(HackSimulation hackSimulation, int pos) {
        if (hackSimulation.getSide() == HackingSide.PLAYER && !aiSimulation.isAwake()) {
            maybeWakeAI();
        }
    }

    @Override
    public void onNodeFortified(HackSimulation hackSimulation, int pos) {
        if (hackSimulation.getSide() == HackingSide.PLAYER && !aiSimulation.isAwake()) {
            maybeWakeAI();
        }
    }

    private void maybeWakeAI() {
        if (!te.nonNullLevel().isClientSide && aiSimulation.isStarted() && te.nonNullLevel().random.nextInt(100) < te.getDetectionChance()) {
            aiSimulation.wakeUp();
        }
    }

    @Override
    public void tick() {
        if (te.isRemoved() || !hacker.isAlive()) {
            // TODO maybe find a way to catch up with hackers who disconnect just to avoid getting zapped?
            return;
        }

        boolean wasDone = isSimulationDone();

        if (!(hacker.containerMenu instanceof SecurityStationHackingMenu) && !playerSimulation.isHackComplete()) {
            // hacker closed their window before hack complete: AI wins
            for (int slot = 0; slot < HackSimulation.GRID_SIZE; slot++) {
                if (NetworkComponentItem.isType(te.getItemHandler().getStackInSlot(slot), NetworkComponentType.NETWORK_IO_PORT)) {
                    aiSimulation.getNodeAt(slot).setHackProgress(slot, 1F, true);
                    break;
                }
            }
        } else if (!wasDone) {
            playerSimulation.tick();
            aiSimulation.tick();
        }

        boolean syncToClient = (te.nonNullLevel().getGameTime() & 0x7) == 0;
        if (!wasDone && aiSimulation.isHackComplete()) {
            // security station wins
            syncToClient = true;
            if (aiSimulation.isAwake()) {
                // if hack window is closed before AI detects intrustion, hacker gets away with it
                if (te.nonNullLevel().isClientSide) {
                    hacker.playSound(SoundEvents.ENDERMAN_DEATH, 1f, 1f);
                } else {
                    hacker.displayClientMessage(xlate("pneumaticcraft.message.securityStation.hackFailed.1").withStyle(ChatFormatting.RED), false);
                    if (!justTesting) {
                        hacker.displayClientMessage(xlate("pneumaticcraft.message.securityStation.hackFailed.2").withStyle(ChatFormatting.RED), false);
                        te.retaliate(hacker);
                    }
                }
            }
        } else if (!wasDone && playerSimulation.isHackComplete()) {
            // hacker wins
            syncToClient = true;
            if (te.nonNullLevel().isClientSide) {
                hacker.playSound(SoundEvents.PLAYER_LEVELUP, 1f, 1f);
            } else {
                hacker.displayClientMessage(xlate("pneumaticcraft.message.securityStation.hackSucceeded.1").withStyle(ChatFormatting.GREEN), false);
                if (!justTesting) {
                    hacker.displayClientMessage(xlate("pneumaticcraft.message.securityStation.hackSucceeded.2").withStyle(ChatFormatting.GREEN), false);
                    te.addHacker(hacker.getGameProfile());
                }
            }
        }
        if (!te.nonNullLevel().isClientSide() && syncToClient) {
            NetworkHandler.sendToPlayer(PacketSyncHackSimulationUpdate.forSecurityStation(te), (ServerPlayer) hacker);
        }
    }

    @Override
    public boolean isSimulationDone() {
        return te.isRemoved() || !hacker.isAlive() || aiSimulation.isHackComplete() || playerSimulation.isHackComplete();
    }

    @Override
    public HackSimulation getSimulation(HackingSide side) {
        return side == HackingSide.PLAYER ? playerSimulation : aiSimulation;
    }

    @Override
    public Player getHacker() {
        return hacker;
    }
}
