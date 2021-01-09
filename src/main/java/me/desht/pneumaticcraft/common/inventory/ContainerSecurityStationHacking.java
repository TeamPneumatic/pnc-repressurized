package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController.HackingSide;
import me.desht.pneumaticcraft.common.hacking.secstation.SimulationController;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ContainerSecurityStationHacking extends ContainerPneumaticBase<TileEntitySecurityStation> {
    public static final int NODE_SPACING = 31;

    public ContainerSecurityStationHacking(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, fromBytes(playerInventory.player, buffer));
    }

    public ContainerSecurityStationHacking(int windowId, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.SECURITY_STATION_HACKING.get(), windowId, playerInventory, pos);

        //add the network slots
        for (int i = 0; i < TileEntitySecurityStation.INV_ROWS; i++) {
            for (int j = 0; j < TileEntitySecurityStation.INV_COLS; j++) {
                SlotUntouchable slot = (SlotUntouchable) addSlot(new SlotUntouchable(te.getPrimaryInventory(), j + i * 5, 8 + j * NODE_SPACING, 22 + i * NODE_SPACING));
                slot.setEnabled(slot.getHasStack());
            }
        }
    }

    private static BlockPos fromBytes(PlayerEntity player, PacketBuffer buffer) {
        BlockPos tilePos = buffer.readBlockPos();

        HackSimulation playerSimulation = HackSimulation.readFromNetwork(buffer);
        HackSimulation aiSimulation = HackSimulation.readFromNetwork(buffer);

        List<Pair<Integer, ItemStack>> nodes = new ArrayList<>();
        int nNodes = buffer.readVarInt();
        for (int i = 0; i < nNodes; i++) {
            nodes.add(Pair.of(buffer.readVarInt(), buffer.readItemStack()));
        }

        boolean justTesting = buffer.readBoolean();

        return PneumaticCraftUtils.getTileEntityAt(player.world, tilePos, TileEntitySecurityStation.class).map(teSS -> {
            ISimulationController controller = new SimulationController(teSS, player, playerSimulation, aiSimulation, justTesting);
            nodes.forEach(node -> {
                controller.getSimulation(HackingSide.PLAYER).addNode(node.getLeft(), node.getRight());
                controller.getSimulation(HackingSide.AI).addNode(node.getLeft(), node.getRight());
            });
            teSS.setSimulationController(controller);
            return teSS.getPos();
        }).orElse(null);
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity par1EntityPlayer, int par2) {
        return ItemStack.EMPTY;
    }

}
