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

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import me.desht.pneumaticcraft.common.variables.TextVariableParser;
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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContainerRemote extends ContainerPneumaticBase<TileEntityBase> {
    private final List<String> syncedVars;
    private final BlockPos[] lastValues;
    private final InteractionHand hand;
    public String[] variables = new String[0];

    public ContainerRemote(MenuType<? extends ContainerRemote> type, int windowId, Inventory playerInventory, InteractionHand hand) {
        super(type, windowId, playerInventory);

        this.hand = hand;
        syncedVars = new ArrayList<>(getRelevantVariableNames(playerInventory.player.getItemInHand(hand)));
        lastValues = new BlockPos[syncedVars.size()];
    }

    private ContainerRemote(MenuType<ContainerRemote> type, int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(type, windowId, playerInventory, getHandFromBuffer(buffer));
    }

    public static ContainerRemote createRemoteContainer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new ContainerRemote(ModContainers.REMOTE.get(), windowId, playerInventory, buffer);
    }

    public static ContainerRemote createRemoteEditorContainer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new ContainerRemote(ModContainers.REMOTE_EDITOR.get(), windowId, playerInventory, buffer);
    }

    private static InteractionHand getHandFromBuffer(FriendlyByteBuf buffer) {
        return buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    private static Set<String> getRelevantVariableNames(@Nonnull ItemStack remote) {
        Set<String> variables = new HashSet<>();
        CompoundTag tag = remote.getTag();
        if (tag != null) {
            ListTag tagList = tag.getList("actionWidgets", Tag.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                CompoundTag widgetTag = tagList.getCompound(i);
                variables.add(widgetTag.getString("variableName"));
                variables.add(widgetTag.getString("enableVariable"));
                TextVariableParser parser = new TextVariableParser(widgetTag.getString("text"));
                parser.parse();
                variables.addAll(parser.getRelevantVariables());
            }
        }
        return variables;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        for (int i = 0; i < lastValues.length; i++) {
            String varName = syncedVars.get(i);
            if (varName.startsWith("#")) varName = varName.substring(1);
            BlockPos newValue = GlobalVariableManager.getInstance().getPos(varName);
            if (!newValue.equals(lastValues[i])) {
                lastValues[i] = newValue;
                for (Object o : containerListeners) {
                    if (o instanceof ServerPlayer)
                        NetworkHandler.sendToPlayer(new PacketSetGlobalVariable(varName, newValue), (ServerPlayer) o);
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
