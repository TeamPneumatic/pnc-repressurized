package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.remote.GlobalVariableManager;
import me.desht.pneumaticcraft.common.remote.TextVariableParser;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContainerRemote extends ContainerPneumaticBase<TileEntityBase> {
    private final List<String> syncedVars;
    private final BlockPos[] lastValues;
    private final Hand hand;
    public String[] variables = new String[0];

    public ContainerRemote(ContainerType<? extends ContainerRemote> type, int windowId, PlayerInventory playerInventory, Hand hand) {
        super(type, windowId, playerInventory);

        this.hand = hand;
        syncedVars = new ArrayList<>(getRelevantVariableNames(playerInventory.player.getHeldItem(hand)));
        lastValues = new BlockPos[syncedVars.size()];
    }

    private ContainerRemote(ContainerType<ContainerRemote> type, int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(type, windowId, playerInventory, getHandFromBuffer(buffer));
    }

    public static ContainerRemote createRemoteContainer(int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
        return new ContainerRemote(ModContainerTypes.REMOTE, windowId, playerInventory, buffer);
    }

    public static ContainerRemote createRemoteEditorContainer(int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
        return new ContainerRemote(ModContainerTypes.REMOTE_EDITOR, windowId, playerInventory, buffer);
    }

    private static Hand getHandFromBuffer(PacketBuffer buffer) {
        return buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    private static Set<String> getRelevantVariableNames(@Nonnull ItemStack remote) {
        Set<String> variables = new HashSet<>();
        CompoundNBT tag = remote.getTag();
        if (tag != null) {
            ListNBT tagList = tag.getList("actionWidgets", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++) {
                CompoundNBT widgetTag = tagList.getCompound(i);
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
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (int i = 0; i < lastValues.length; i++) {
            BlockPos newValue = GlobalVariableManager.getInstance().getPos(syncedVars.get(i));
            if (!newValue.equals(lastValues[i])) {
                lastValues[i] = newValue;
                for (Object o : listeners) {
                    if (o instanceof ServerPlayerEntity)
                        NetworkHandler.sendToPlayer(new PacketSetGlobalVariable(syncedVars.get(i), newValue), (ServerPlayerEntity) o);
                }
            }
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return player.getHeldItemMainhand().getItem() == ModItems.REMOTE;
    }

    public Hand getHand() {
        return hand;
    }
}
