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

import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.remote.SavedRemoteLayout;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class RemoteMenu extends AbstractPneumaticCraftMenu<AbstractPneumaticCraftBlockEntity> {
    private final List<String> syncedVars;
    private final BlockPos[] lastValues;
    private final InteractionHand hand;
    private final List<String> allKnownGlobalVars;
    private final UUID playerId;

    public RemoteMenu(MenuType<? extends RemoteMenu> type, int windowId, Inventory playerInventory, InteractionHand hand) {
        super(type, windowId, playerInventory);

        this.hand = hand;
        this.allKnownGlobalVars = List.of();
        this.playerId = playerInventory.player.getUUID();
        this.syncedVars = List.copyOf(getRelevantVariableNames(playerInventory.player, playerInventory.player.getItemInHand(hand)));
        this.lastValues = new BlockPos[syncedVars.size()];
    }

    private RemoteMenu(MenuType<RemoteMenu> type, int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        super(type, windowId, playerInventory);

        // see RemoteItem#toBytes for corresponding serialization
        this.hand = buffer.readEnum(InteractionHand.class);
        this.allKnownGlobalVars = buffer.readList(FriendlyByteBuf::readUtf);

        this.playerId = playerInventory.player.getUUID();
        this.syncedVars = List.copyOf(getRelevantVariableNames(playerInventory.player, playerInventory.player.getItemInHand(hand)));
        this.lastValues = new BlockPos[syncedVars.size()];
    }

    public static RemoteMenu createRemoteContainer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new RemoteMenu(ModMenuTypes.REMOTE.get(), windowId, playerInventory, buffer);
    }

    public static RemoteMenu createRemoteEditorContainer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new RemoteMenu(ModMenuTypes.REMOTE_EDITOR.get(), windowId, playerInventory, buffer);
    }

    private Set<String> getRelevantVariableNames(Player player, @Nonnull ItemStack remote) {
        SavedRemoteLayout layout = SavedRemoteLayout.fromItem(remote);

        Set<String> variables = new HashSet<>();
        layout.getWidgets().forEach(w -> w.discoverVariables(variables, playerId));

        Set<String> result = new HashSet<>();
        variables.forEach(varName -> {
            if (!varName.isEmpty()) {
                if (!GlobalVariableHelper.getInstance().hasPrefix(varName)) {
                    if (!player.level().isClientSide) {
                        player.displayClientMessage(xlate("pneumaticcraft.command.globalVariable.prefixReminder", varName).withStyle(ChatFormatting.GOLD), false);
                    }
                    varName = "#" + varName;
                }
                result.add(varName);
            }
        });
        return result;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        for (int i = 0; i < lastValues.length; i++) {
            String varName = syncedVars.get(i);
            if (varName.isEmpty()) continue;
            BlockPos newValue = GlobalVariableHelper.getInstance().getPos(playerId, varName);
            if (newValue != null && !newValue.equals(lastValues[i])) {
                lastValues[i] = newValue;
                ServerPlayer serverPlayer = PneumaticCraftUtils.getPlayerFromId(playerId);
                if (serverPlayer != null) {
                    NetworkHandler.sendToPlayer(PacketSetGlobalVariable.forPos(varName, newValue), serverPlayer);
                }
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(hand).getItem() == ModItems.REMOTE.get();
    }

    public InteractionHand getHand() {
        return hand;
    }

    public List<String> allKnownGlobalVars() {
        return allKnownGlobalVars;
    }
}
