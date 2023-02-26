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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController.HackingSide;
import me.desht.pneumaticcraft.common.item.NetworkComponentItem;
import me.desht.pneumaticcraft.common.item.NetworkComponentItem.NetworkComponentType;
import me.desht.pneumaticcraft.lib.BlockEntityConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class HackSimulation {
    public static final int GRID_WIDTH = 5;
    public static final int GRID_HEIGHT = 7;
    public static final int GRID_SIZE = GRID_WIDTH * GRID_HEIGHT;

    // lookup table which maps a node index to a list of the nodes it can connect to
    private static final List<IntList> connectionMatrix = new ArrayList<>();
    public static final int NODE_FORTIFICATION_TIME = 100;

    static {
        for (int i = 0; i < GRID_SIZE; i++) {
            int xPos = i % GRID_WIDTH;
            int yPos = i / GRID_WIDTH;
            IntList connections = new IntArrayList();
            if (yPos > 0) {
                connections.add(i - GRID_WIDTH);  // up
                if (xPos > 0) connections.add(i - (GRID_WIDTH + 1));  // up-left
                if (xPos < GRID_WIDTH - 1) connections.add(i - (GRID_WIDTH - 1));  // up-right
            }
            if (yPos < GRID_HEIGHT - 1) {
                connections.add(i + GRID_WIDTH);  // down
                if (xPos > 0) connections.add(i + (GRID_WIDTH - 1));  // down-left
                if (xPos < GRID_WIDTH - 1) connections.add(i + (GRID_WIDTH + 1));  // down-right
            }
            if (xPos > 0) connections.add(i - 1);  // left
            if (xPos < GRID_WIDTH - 1) connections.add(i + 1);  // right
            connectionMatrix.add(connections);
        }
    }

    private final Node[] nodes = new Node[GRID_SIZE];
    private ISimulationController controller;
    private final int startPosition;
    private boolean isStarted = false;
    private boolean hackComplete = false;
    private final float baseBridgeSpeed;
    private final HackingSide side;
    private int pendingNukePos = -1;
    private final List<NetworkComponentType> targets = new ArrayList<>();
    public final List<ConnectionEntry> allConnections = new ArrayList<>();  // synced to client for rendering purposes
    private boolean awake;
    private int stopWormTimer;  // in ticks
    private int nukeVirusCooldown; // in ticks

    /**
     * Create a new simulation object
     *
     * @param controller the simulation controller
     * @param startPosition where the player or AI starts in this simulation
     * @param baseBridgeSpeed base speed for hacking between nodes
     * @param side is this simulation AI controlled?
     */
    public HackSimulation(ISimulationController controller, int startPosition, float baseBridgeSpeed, HackingSide side) {
        this.controller = controller;
        this.startPosition = startPosition;
        this.baseBridgeSpeed = baseBridgeSpeed;
        this.side = side;
        if (side == HackingSide.PLAYER) {
            awake = true;
            targets.add(NetworkComponentType.DIAGNOSTIC_SUBROUTINE);
            targets.add(NetworkComponentType.NETWORK_REGISTRY);
        } else {
            awake = false;
            targets.add(NetworkComponentType.NETWORK_IO_PORT);
        }
        this.stopWormTimer = 0;
    }

    public static HackSimulation dummySimulation() {
        // used for rendering background connection lines
        return new HackSimulation(null, -1, BlockEntityConstants.NETWORK_AI_BRIDGE_SPEED, HackingSide.AI);
    }

    public static HackSimulation readFromNetwork(FriendlyByteBuf buffer) {
        float speed = buffer.readFloat();
        HackingSide side = buffer.readBoolean() ? HackingSide.AI : HackingSide.PLAYER;
        int start = buffer.readVarInt();
        return new HackSimulation(null, start, speed, side);
    }

    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFloat(baseBridgeSpeed);
        buffer.writeBoolean(side == HackingSide.AI);
        buffer.writeVarInt(startPosition);
    }

    public void addNode(int position, NetworkComponentType type, int count) {
        if (type == null || count == 0) return;
        Validate.isTrue(!isStarted, "can't add nodes after simulation has started!");
        Validate.isTrue(nodes[position] == null, "position " + position + " already has a node!");

        nodes[position] = new Node(type, count);
        if (isDummy() || position == startPosition) {
            Validate.isTrue(startPosition == -1 || !targets.contains(type), "start node may not be of target type " + type);
            nodes[position].setHackProgress(position, 1f, false);
        }
    }

    public void addNode(int slot, ItemStack stack) {
        NetworkComponentItem.getType(stack).ifPresent(type -> addNode(slot, type, controller == null ? 1 : stack.getCount()));
    }

    private boolean isDummy() {
        return startPosition < 0;
    }

    public Node getNodeAt(int pos) {
        return nodes[pos];
    }

    public boolean isHackComplete() {
        return hackComplete;
    }

    HackSimulation setController(ISimulationController controller) {
        this.controller = controller;
        return this;
    }

    public boolean isNukeVirusReady() {
        return nukeVirusCooldown == 0;
    }

    public HackingSide getSide() {
        return side;
    }

    public void tick() {
        if (hackComplete) return;

        isStarted = true;

        if (stopWormTimer > 0) {
            stopWormTimer--;
        }
        if (nukeVirusCooldown > 0) {
            nukeVirusCooldown--;
        }

        allConnections.clear();
        int nonNullNodes = 0;
        int completeNodes = 0;
        for (int i = 0; i < nodes.length; i++) {
            Node node = getNodeAt(i);
            if (node == null) continue;
            nonNullNodes++;

            int f = node.fortification;
            node.tick();
            if (controller != null && f < NODE_FORTIFICATION_TIME && node.fortification == NODE_FORTIFICATION_TIME) {
                controller.onNodeFortified(this, i);
            }

            if (side == HackingSide.AI && isAwake() && (isDummy() || node.isHacked()) && node.outGoingHacks.isEmpty()) {
                // start the trace to all neighbouring nodes
                for (int neighbourPos : getNeighbours(i)) {
                    Node neighbourNode = getNodeAt(neighbourPos);
                    if (neighbourNode != null && (isDummy() || !neighbourNode.isHacked())) {
                        // found an unhacked neighbour
                        node.startHacking(i, neighbourPos);
                    }
                }
            }

            if (pendingNukePos == i) {
                activateNukeVirus(i);
            }

            // update all in-progress connections
            int finishedConns = 0;
            for (Pair<Integer, Float> conn : node.outGoingHacks) {
                if (conn.getValue() < 1F) {
                    if (stopWormTimer == 0) {
                        Node targetNode = getNodeAt(conn.getKey());
                        conn.setValue(Math.min(1F, conn.getValue() + targetNode.getProgressPerTick()));
                        targetNode.setHackProgress(conn.getKey(), conn.getValue(), true);
                    }
                } else {
                    finishedConns++;
                }
                allConnections.add(new ConnectionEntry(i, conn.getKey(), conn.getValue()));
            }
            if (finishedConns == node.outGoingHacks.size() && !node.outGoingHacks.isEmpty()) completeNodes++;
        }
        if (completeNodes >= nonNullNodes) {
            hackComplete = true;
        }
    }

    public void activateNukeVirus(int pos) {
        int neighbour = getHackedNeighbour(pendingNukePos);
        if (neighbour >= 0) {
            boolean found = false;
            for (Pair<Integer, Float> conn : getNodeAt(neighbour).outGoingHacks) {
                if (conn.getLeft() == pendingNukePos) {
                    conn.setValue(1f);
                    found = true;
                }
            }
            if (!found) getNodeAt(neighbour).outGoingHacks.add(Pair.of(pendingNukePos, 1f));
            getNodeAt(pos).setHackProgress(pos, 1f, false);
            if (controller != null) controller.getHacker().playSound(SoundEvents.GENERIC_EXPLODE, 1f, 1f);
            nukeVirusCooldown = 60;
        }
        pendingNukePos = -1;
    }

    public IntList getNeighbours(int node) {
        return connectionMatrix.get(node);
    }

    public void startHack(int targetPos) {
        if (getNodeAt(targetPos).isHacked()) return;

        for (int neighbour : getNeighbours(targetPos)) {
            Node attacker = getNodeAt(neighbour);
            if (attacker != null && attacker.isHacked()) {
                attacker.startHacking(neighbour, targetPos);
                return;
            }
        }
    }

    public boolean initiateNukeVirus(int pos) {
        Validate.isTrue(pos >= 0 && pos < GRID_SIZE, "nuke position " + pos + " out of range!");
        if (pendingNukePos < 0 && isNukeVirusReady()
                && getNodeAt(pos).type != NetworkComponentType.DIAGNOSTIC_SUBROUTINE && getNodeAt(pos).type != NetworkComponentType.NETWORK_REGISTRY) {
            pendingNukePos = pos;
            return true;
        } else {
            return false;
        }
    }

    public void applyStopWorm(int duration) {
        stopWormTimer = duration;
    }

    /**
     * Fortify a player-hacked node, making it harder for the AI to hack.
     *
     * @param pos the node position
     */
    public void fortify(int pos) {
        Node node = getNodeAt(pos);
        if (node != null) node.startFortifying();
    }

    public void wakeUp() {
        if (!awake) {
            awake = true;
            if (controller != null) controller.getHacker().playSound(SoundEvents.BLAZE_HURT, 1f, 1f);
        }
    }

    public boolean isAwake() {
        return awake;
    }

    public void setHackComplete() {
        // for client sync only
        this.hackComplete = true;
    }

    public void syncFromServer(List<ConnectionEntry> newConns) {
        // client-side method to sync node connections
        for (int i = 0; i < nodes.length; i++) {
            Node node = getNodeAt(i);
            if (node != null) node.outGoingHacks.clear();
        }
        newConns.forEach(conn -> getNodeAt(conn.from).outGoingHacks.add(MutablePair.of(conn.to, conn.progress)));
    }

    public void updateFortification(List<Pair<Integer, Integer>> fortification) {
        // client-side method to sync node fortification progress
        fortification.forEach(pair -> getNodeAt(pair.getLeft()).fortification = pair.getRight());
    }

    public long getRemainingTraceTime() {
        // TODO remaining time calculation
        return 0;
    }

    public int getHackedNeighbour(int nodePos) {
        for (int n2 : getNeighbours(nodePos)) {
            Node node2 = getNodeAt(n2);
            if (node2 != null && node2.isHacked()) {
                return n2;
            }
        }
        return -1;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public boolean isStopWormed() {
        return stopWormTimer > 0;
    }

    public class Node {
        private final NetworkComponentType type;
        private final int size;
        private float hackProgress;
        private int fortification;
        private final List<Pair<Integer, Float>> outGoingHacks = new ArrayList<>();

        Node(NetworkComponentType type, int size) {
            this.type = type;
            this.size = size;
            this.hackProgress = 0f;
            this.fortification = 0;
        }

        public NetworkComponentType getType() {
            return type;
        }

        public int getSize() {
            return size;
        }

        public void startFortifying() {
            if (fortification == 0) fortification = 1;
        }

        public boolean isFortified() {
            return fortification >= NODE_FORTIFICATION_TIME;
        }

        public float getHackProgress() {
            return hackProgress;
        }

        public void setHackProgress(int pos, float hackProgress, boolean notifyController) {
            boolean wasHacked = isHacked();
            this.hackProgress = hackProgress;
            if (controller != null && isHacked() && !wasHacked) {
                if (isStarted) {
                    if (side == HackingSide.AI && isAwake()) {
                        controller.getHacker().playSound(SoundEvents.NOTE_BLOCK_BASEDRUM.get(), 1f, 1f);
                    } else if (side == HackingSide.PLAYER) {
                        controller.getHacker().playSound(SoundEvents.NOTE_BLOCK_FLUTE.get(), 1f, 1f);
                    }
                }
                if (notifyController) controller.onNodeHacked(HackSimulation.this, pos);
                if (isStarted && targets.contains(this.type)) {
                    hackComplete = true;  // hacked a target node: game over!
                }
            }
        }

        public boolean isHacked() {
            return hackProgress >= 1f;
        }

        void startHacking(int fromPos, int toPos) {
            outGoingHacks.add(MutablePair.of(toPos, 0F));
            if (controller != null) controller.onConnectionStarted(HackSimulation.this, fromPos, toPos, 0F);
        }

        float getProgressPerTick() {
            return baseBridgeSpeed * (1 / (BlockEntityConstants.NETWORK_NODE_RATING_MULTIPLIER * (size + (isFortified() ? 1 : 0))));
        }

        public void tick() {
            if (fortification > 0 && fortification < NODE_FORTIFICATION_TIME) fortification++;
        }

        public int getFortification() {
            return fortification;
        }

        public float getFortificationProgress() {
            return fortification / (float) NODE_FORTIFICATION_TIME;
        }
    }

    public static class ConnectionEntry {
        public final int from;
        public final int to;
        public final float progress;

        ConnectionEntry(int from, int to, float progress) {
            this.from = from;
            this.to = to;
            this.progress = progress;
        }

        public void write(FriendlyByteBuf buffer) {
            buffer.writeVarInt(from);
            buffer.writeVarInt(to);
            buffer.writeFloat(progress);
        }

        public static ConnectionEntry readFromNetwork(FriendlyByteBuf buffer) {
            return new ConnectionEntry(buffer.readVarInt(), buffer.readVarInt(), buffer.readFloat());
        }
    }
}
