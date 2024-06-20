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
import me.desht.pneumaticcraft.common.item.RemoteItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import me.desht.pneumaticcraft.common.variables.TextVariableParser;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nonnull;
import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class RemoteMenu extends AbstractPneumaticCraftMenu<AbstractPneumaticCraftBlockEntity> {
    private final List<String> syncedVars;
    private final BlockPos[] lastValues;
    private final InteractionHand hand;
    public final String[] variables;
    private final UUID playerId;

    public RemoteMenu(MenuType<? extends RemoteMenu> type, int windowId, Inventory playerInventory, InteractionHand hand) {
        super(type, windowId, playerInventory);

        this.hand = hand;
        this.variables = new String[0];
        this.playerId = playerInventory.player.getUUID();
        this.syncedVars = new ArrayList<>(getRelevantVariableNames(playerInventory.player, playerInventory.player.getItemInHand(hand)));
        this.lastValues = new BlockPos[syncedVars.size()];
    }

    private RemoteMenu(MenuType<RemoteMenu> type, int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        super(type, windowId, playerInventory);

        this.hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        this.variables = new String[buffer.readVarInt()];
        for (int i = 0; i < variables.length; i++) {
            variables[i] = buffer.readUtf();
        }

        this.playerId = playerInventory.player.getUUID();
        this.syncedVars = new ArrayList<>(getRelevantVariableNames(playerInventory.player, playerInventory.player.getItemInHand(hand)));
        this.lastValues = new BlockPos[syncedVars.size()];
    }

    public static RemoteMenu createRemoteContainer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new RemoteMenu(ModMenuTypes.REMOTE.get(), windowId, playerInventory, buffer);
    }

    public static RemoteMenu createRemoteEditorContainer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new RemoteMenu(ModMenuTypes.REMOTE_EDITOR.get(), windowId, playerInventory, buffer);
    }

    private Set<String> getRelevantVariableNames(Player player, @Nonnull ItemStack remote) {
        Set<String> variables = new HashSet<>();
        CompoundTag tag = RemoteItem.getSavedLayout(remote);
        if (!tag.isEmpty()) {
            ListTag tagList = tag.getList("actionWidgets", Tag.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                CompoundTag widgetTag = tagList.getCompound(i);
                if (widgetTag.contains("variableName")) variables.add(widgetTag.getString("variableName"));
                if (widgetTag.contains("enableVariable")) variables.add(widgetTag.getString("enableVariable"));
                TextVariableParser parser = new TextVariableParser(widgetTag.getString("text"), playerId);
                parser.parse(); // discover any ${variable} references in the text
                variables.addAll(parser.getRelevantVariables());
            }
        }

        Set<String> result = new HashSet<>();
        variables.forEach(varName -> {
            if (!varName.isEmpty()) {
                if (!GlobalVariableHelper.hasPrefix(varName)) {
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
            BlockPos newValue = GlobalVariableHelper.getPos(playerId, varName);
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
}
