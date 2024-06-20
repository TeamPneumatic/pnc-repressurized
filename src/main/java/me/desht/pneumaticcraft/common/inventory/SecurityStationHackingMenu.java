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

package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.block.entity.utility.SecurityStationBlockEntity;
import me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController.HackingSide;
import me.desht.pneumaticcraft.common.hacking.secstation.SimulationController;
import me.desht.pneumaticcraft.common.inventory.slot.UntouchableSlot;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SecurityStationHackingMenu extends AbstractPneumaticCraftMenu<SecurityStationBlockEntity> {
    public static final int NODE_SPACING = 31;

    public SecurityStationHackingMenu(int i, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(i, playerInventory, fromBytes(playerInventory.player, buffer));
    }

    public SecurityStationHackingMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.SECURITY_STATION_HACKING.get(), windowId, playerInventory, pos);

        //add the network slots
        for (int i = 0; i < SecurityStationBlockEntity.INV_ROWS; i++) {
            for (int j = 0; j < SecurityStationBlockEntity.INV_COLS; j++) {
                UntouchableSlot slot = (UntouchableSlot) addSlot(new UntouchableSlot(blockEntity.getItemHandler(), j + i * 5, 8 + j * NODE_SPACING, 22 + i * NODE_SPACING));
                slot.setEnabled(slot.hasItem());
            }
        }
    }

    private static BlockPos fromBytes(Player player, RegistryFriendlyByteBuf buffer) {
        BlockPos tilePos = buffer.readBlockPos();

        HackSimulation playerSimulation = HackSimulation.STREAM_CODEC.decode(buffer);
        HackSimulation aiSimulation = HackSimulation.STREAM_CODEC.decode(buffer);

        List<Pair<Integer, ItemStack>> nodes = new ArrayList<>();
        int nNodes = buffer.readVarInt();
        for (int i = 0; i < nNodes; i++) {
            nodes.add(Pair.of(buffer.readVarInt(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)));
        }

        boolean justTesting = buffer.readBoolean();

        return PneumaticCraftUtils.getBlockEntityAt(player.level(), tilePos, SecurityStationBlockEntity.class).map(teSS -> {
            ISimulationController controller = new SimulationController(teSS, player, playerSimulation, aiSimulation, justTesting);
            nodes.forEach(node -> {
                controller.getSimulation(HackingSide.PLAYER).addNode(node.getLeft(), node.getRight());
                controller.getSimulation(HackingSide.AI).addNode(node.getLeft(), node.getRight());
            });
            teSS.setSimulationController(controller);
            return teSS.getBlockPos();
        }).orElse(null);
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int par2) {
        return ItemStack.EMPTY;
    }

}
