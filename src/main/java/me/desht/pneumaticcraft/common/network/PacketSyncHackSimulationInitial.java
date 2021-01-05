package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController.HackingSide;
import me.desht.pneumaticcraft.common.hacking.secstation.SimulationController;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 *
 * Sent by the server when a security station hack starts to sync the initial simulation state.
 */
public class PacketSyncHackSimulationInitial extends LocationIntPacket {
    private final HackSimulation playerSimulation;
    private final HackSimulation aiSimulation;
    private final List<Pair<Integer, ItemStack>> nodes = new ArrayList<>();
    private final boolean justTesting;

    /**
     * Server-side constructor
     * @param te the security station
     */
    public PacketSyncHackSimulationInitial(TileEntitySecurityStation te) {
        super(te.getPos());

        playerSimulation = te.getSimulationController().getSimulation(HackingSide.PLAYER);
        aiSimulation = te.getSimulationController().getSimulation(HackingSide.AI);

        for (int i = 0; i < te.getPrimaryInventory().getSlots(); i++) {
            ItemStack stack = te.getPrimaryInventory().getStackInSlot(i);
            if (!stack.isEmpty()) {
                nodes.add(Pair.of(i, stack));
            }
        }

        justTesting = te.getSimulationController().isJustTesting();
    }

    public PacketSyncHackSimulationInitial(PacketBuffer buffer) {
        super(buffer);

        playerSimulation = HackSimulation.readFromNetwork(buffer);
        aiSimulation = HackSimulation.readFromNetwork(buffer);

        // TODO might not need this if we can get the nodes from the client container
        int nNodes = buffer.readVarInt();
        for (int i = 0; i < nNodes; i++) {
            nodes.add(Pair.of(buffer.readVarInt(), buffer.readItemStack()));
        }

        justTesting = buffer.readBoolean();
    }

    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);

        playerSimulation.writeToNetwork(buffer);
        aiSimulation.writeToNetwork(buffer);

        // TODO might not need this if we can get the nodes from the client container
        buffer.writeVarInt(nodes.size());
        nodes.forEach(pair -> {
            buffer.writeVarInt(pair.getLeft());
            buffer.writeItemStack(pair.getRight());
        });

        buffer.writeBoolean(justTesting);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = ClientUtils.getClientTE(pos);
            if (te instanceof TileEntitySecurityStation) {
                TileEntitySecurityStation teSS = (TileEntitySecurityStation) te;
                ISimulationController controller = new SimulationController(teSS, ClientUtils.getClientPlayer(), playerSimulation, aiSimulation, justTesting);
                nodes.forEach(node -> {
                    controller.getSimulation(HackingSide.PLAYER).addNode(node.getLeft(), node.getRight());
                    controller.getSimulation(HackingSide.AI).addNode(node.getLeft(), node.getRight());
                });
                teSS.setSimulationController(controller);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

