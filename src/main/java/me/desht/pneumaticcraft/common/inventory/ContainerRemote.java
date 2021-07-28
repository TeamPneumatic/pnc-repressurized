package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import me.desht.pneumaticcraft.common.variables.TextVariableParser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.*;

public class ContainerRemote extends ContainerPneumaticBase<TileEntityBase> {
    private final List<String> syncedVars;
    private final BlockPos[] lastValues;
    private final Hand hand;
    public final String[] variables;
    private final UUID playerId;

    public ContainerRemote(ContainerType<? extends ContainerRemote> type, int windowId, PlayerInventory playerInventory, Hand hand) {
        super(type, windowId, playerInventory);

        this.hand = hand;
        this.variables = new String[0];
        this.syncedVars = new ArrayList<>(getRelevantVariableNames(playerInventory.player.getHeldItem(hand)));
        this.lastValues = new BlockPos[syncedVars.size()];
        this.playerId = playerInventory.player.getUniqueID();
    }

    private ContainerRemote(ContainerType<ContainerRemote> type, int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
        super(type, windowId, playerInventory);

        this.hand = buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;

        this.variables = new String[buffer.readVarInt()];
        for (int i = 0; i < variables.length; i++) {
            variables[i] = buffer.readString();
        }

        this.syncedVars = new ArrayList<>(getRelevantVariableNames(playerInventory.player.getHeldItem(hand)));
        this.lastValues = new BlockPos[syncedVars.size()];
        this.playerId = playerInventory.player.getUniqueID();
    }

    public static ContainerRemote createRemoteContainer(int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
        return new ContainerRemote(ModContainers.REMOTE.get(), windowId, playerInventory, buffer);
    }

    public static ContainerRemote createRemoteEditorContainer(int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
        return new ContainerRemote(ModContainers.REMOTE_EDITOR.get(), windowId, playerInventory, buffer);
    }

    private Set<String> getRelevantVariableNames(@Nonnull ItemStack remote) {
        Set<String> variables = new HashSet<>();
        CompoundNBT tag = remote.getTag();
        if (tag != null) {
            ListNBT tagList = tag.getList("actionWidgets", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                CompoundNBT widgetTag = tagList.getCompound(i);
                variables.add(widgetTag.getString("variableName"));
                variables.add(widgetTag.getString("enableVariable"));
                TextVariableParser parser = new TextVariableParser(widgetTag.getString("text"), playerId);
                parser.parse(); // discover any ${variable} references in the text
                variables.addAll(parser.getRelevantVariables());
            }
        }
        variables.remove("");
        return variables;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (int i = 0; i < syncedVars.size(); i++) {
            String varName = syncedVars.get(i);
            if (varName.isEmpty()) continue;
            BlockPos newValue = GlobalVariableHelper.getPos(playerId, varName);
            if (newValue != null && !newValue.equals(lastValues[i])) {
                lastValues[i] = newValue;
                listeners.stream()
                        .filter(listener -> listener instanceof ServerPlayerEntity)
                        .map(listener -> (ServerPlayerEntity) listener)
                        .forEach(player -> NetworkHandler.sendToPlayer(new PacketSetGlobalVariable(varName, newValue), player));
            }
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return player.getHeldItem(hand).getItem() == ModItems.REMOTE.get();
    }

    public Hand getHand() {
        return hand;
    }
}
